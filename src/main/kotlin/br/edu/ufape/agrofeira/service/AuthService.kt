package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.PasswordResetToken
import br.edu.ufape.agrofeira.domain.entity.RefreshToken
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.*
import br.edu.ufape.agrofeira.dto.response.LoginResponse
import br.edu.ufape.agrofeira.dto.response.UsuarioDTO
import br.edu.ufape.agrofeira.exception.InvalidTokenException
import br.edu.ufape.agrofeira.exception.UnauthorizedActionException
import br.edu.ufape.agrofeira.repository.PasswordResetTokenRepository
import br.edu.ufape.agrofeira.repository.PerfilRepository
import br.edu.ufape.agrofeira.repository.RefreshTokenRepository
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val usuarioRepository: UsuarioRepository,
    private val perfilRepository: PerfilRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${jwt.refresh.expiration:604800000}") // Default 7d
    private val refreshExpirationMs: Long,
    @Value("\${jwt.reset.expiration:3600000}") // Default 1h
    private val resetExpirationMs: Long,
) {
    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.username,
                request.password,
            ),
        )

        val usuario =
            usuarioRepository
                .findByIdentificador(request.username)
                .orElseThrow { RuntimeException("Usuário não encontrado") }

        // MVP 1.x.x - Restrição de Login
        val isAllowed = usuario.perfis.any { it.nome == "ADMINISTRADOR" || it.nome == "GERENCIADOR" }
        if (!isAllowed) {
            throw UnauthorizedActionException("Acesso restrito a administradores e gerenciadores na versão atual.")
        }

        // Enforce single session: remove all old tokens for this user
        refreshTokenRepository.deleteByUsuario(usuario)

        val token = jwtService.generateToken(usuario)
        val refreshToken = createRefreshToken(usuario)

        return LoginResponse(
            token = token,
            refreshToken = refreshToken.token,
        )
    }

    @Transactional
    fun refreshToken(request: RefreshTokenRequest): LoginResponse {
        val refreshToken =
            refreshTokenRepository
                .findByToken(request.refreshToken)
                .orElseThrow { InvalidTokenException("Refresh token não encontrado") }

        if (refreshToken.revogado) {
            throw InvalidTokenException("Refresh token revogado")
        }

        if (refreshToken.expiracao.isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken)
            throw InvalidTokenException("Refresh token expirado")
        }

        val usuario = refreshToken.usuario
        val newToken = jwtService.generateToken(usuario)

        // Rotation: Revoke old token and create a new one
        refreshToken.revogado = true
        refreshTokenRepository.save(refreshToken)

        val newRefreshToken = createRefreshToken(usuario)

        return LoginResponse(
            token = newToken,
            refreshToken = newRefreshToken.token,
        )
    }

    private fun createRefreshToken(usuario: Usuario): RefreshToken {
        val refreshToken =
            RefreshToken(
                token = UUID.randomUUID().toString(),
                usuario = usuario,
                expiracao = Instant.now().plusMillis(refreshExpirationMs),
            )
        return refreshTokenRepository.save(refreshToken)
    }

    @Transactional
    fun register(request: RegisterRequest): UsuarioDTO {
        // RF 0002 - Role Hierarchy Check
        val authentication = SecurityContextHolder.getContext().authentication
        val isGerenciador = authentication?.authorities?.any { it.authority == "ROLE_GERENCIADOR" } ?: false

        if (isGerenciador) {
            val forbiddenProfiles = setOf("ADMINISTRADOR", "GERENCIADOR")
            val hasForbiddenProfile = request.perfis.any { forbiddenProfiles.contains(it) }

            if (hasForbiddenProfile) {
                throw UnauthorizedActionException(
                    "O perfil GERENCIADOR não possui permissão para cadastrar administradores ou outros gerenciadores.",
                )
            }
        }

        if (request.email != null && usuarioRepository.existsByEmailIncludingDeleted(request.email!!)) {
            throw IllegalArgumentException("E-mail já cadastrado")
        }
        if (request.telefone != null && usuarioRepository.existsByTelefoneIncludingDeleted(request.telefone!!)) {
            throw IllegalArgumentException("Telefone já cadastrado")
        }

        val perfis =
            request.perfis
                .map { nomePerfil ->
                    perfilRepository
                        .findByNome(nomePerfil)
                        .orElseThrow { IllegalArgumentException("Perfil não encontrado: $nomePerfil") }
                }.toMutableSet()

        val usuario =
            Usuario(
                nome = request.nome,
                email = request.email,
                telefone = request.telefone,
                senhaHash = passwordEncoder.encode(request.senha)!!,
                perfis = perfis,
            )

        val savedUsuario = usuarioRepository.save(usuario)
        return savedUsuario.toDTO()
    }

    @Transactional
    fun forgotPassword(request: ForgotPasswordRequest) {
        val usuario =
            usuarioRepository
                .findByIdentificador(request.identifier)
                .orElseThrow { IllegalArgumentException("Usuário não encontrado") }

        val isAllowed = usuario.perfis.any { it.nome == "ADMINISTRADOR" || it.nome == "GERENCIADOR" }
        if (!isAllowed) {
            throw UnauthorizedActionException("Recuperação de senha não permitida para este perfil na versão atual.")
        }

        if (usuario.email == null) {
            throw IllegalArgumentException("Recuperação de senha disponível apenas para usuários com e-mail cadastrado.")
        }

        // Limpa tokens antigos para evitar inchaço no banco e garantir um único token ativo por tentativa
        passwordResetTokenRepository.deleteByUsuario(usuario)

        val rawToken = UUID.randomUUID().toString()
        val hashedToken = hashToken(rawToken)

        val resetToken =
            PasswordResetToken(
                token = hashedToken,
                usuario = usuario,
                expiracao = Instant.now().plusMillis(resetExpirationMs),
            )
        passwordResetTokenRepository.save(resetToken)

        emailService.sendPasswordResetEmail(usuario.email!!, rawToken, usuario.nome)
        println("E-mail de recuperação enviado para: ${usuario.email}. Token raw: $rawToken (hash no banco: $hashedToken)")
    }

    @Transactional
    fun resetPassword(request: ResetPasswordRequest) {
        val hashedToken = hashToken(request.token)
        val resetToken =
            passwordResetTokenRepository
                .findByToken(hashedToken)
                .orElseThrow { InvalidTokenException("Token de recuperação não encontrado") }

        if (resetToken.usado) {
            throw InvalidTokenException("Este token já foi utilizado")
        }

        if (resetToken.expiracao.isBefore(Instant.now())) {
            throw InvalidTokenException("Token de recuperação expirado")
        }

        val usuario = resetToken.usuario
        usuarioRepository.save(
            usuario.copy(
                senhaHash = passwordEncoder.encode(request.novaSenha)!!,
                atualizadoEm = LocalDateTime.now(),
            ),
        )

        resetToken.usado = true
        passwordResetTokenRepository.save(resetToken)

        // Notificação de segurança
        usuario.email?.let {
            emailService.sendPasswordChangeNotification(it, usuario.nome)
        }
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    @Transactional
    fun logout(request: LogoutRequest) {
        val refreshToken =
            refreshTokenRepository
                .findByToken(request.refreshToken)
                .orElseThrow { InvalidTokenException("Refresh token não encontrado") }

        if (refreshToken.revogado) {
            throw InvalidTokenException("Refresh token já está revogado")
        }

        refreshToken.revogado = true
        refreshTokenRepository.save(refreshToken)
        println("Logout efetuado com sucesso (token: ${request.refreshToken.take(15)}...)")
    }
}
