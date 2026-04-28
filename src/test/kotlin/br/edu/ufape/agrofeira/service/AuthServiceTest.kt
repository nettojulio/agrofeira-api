package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.PasswordResetToken
import br.edu.ufape.agrofeira.domain.entity.Perfil
import br.edu.ufape.agrofeira.domain.entity.RefreshToken
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.*
import br.edu.ufape.agrofeira.dto.response.UsuarioDTO
import br.edu.ufape.agrofeira.exception.InvalidTokenException
import br.edu.ufape.agrofeira.exception.UnauthorizedActionException
import br.edu.ufape.agrofeira.repository.PasswordResetTokenRepository
import br.edu.ufape.agrofeira.repository.PerfilRepository
import br.edu.ufape.agrofeira.repository.RefreshTokenRepository
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {
    @Mock
    private lateinit var authenticationManager: AuthenticationManager

    @Mock
    private lateinit var jwtService: JwtService

    @Mock
    private lateinit var usuarioRepository: UsuarioRepository

    @Mock
    private lateinit var perfilRepository: PerfilRepository

    @Mock
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Mock
    private lateinit var passwordResetTokenRepository: PasswordResetTokenRepository

    @Mock
    private lateinit var emailService: EmailService

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var authService: AuthService

    private lateinit var usuario: Usuario
    private lateinit var perfilConsumidor: Perfil
    private lateinit var perfilAdmin: Perfil
    private val id = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        perfilConsumidor = Perfil(nome = "CONSUMIDOR")
        perfilAdmin = Perfil(nome = "ADMINISTRADOR")
        usuario =
            Usuario(
                id = id,
                nome = "João",
                email = "joao@email.com",
                telefone = "87999999999",
                senhaHash = "senha_encriptada",
                perfis = mutableSetOf(perfilAdmin),
            )

        authService =
            AuthService(
                authenticationManager,
                jwtService,
                usuarioRepository,
                perfilRepository,
                refreshTokenRepository,
                passwordResetTokenRepository,
                emailService,
                passwordEncoder,
                604800000L,
                3600000L,
            )

        SecurityContextHolder.clearContext()
    }

    private fun mockAuthentication(roles: List<String>) {
        val authorities = roles.map { SimpleGrantedAuthority(it) }
        val authentication = mock(Authentication::class.java)
        `when`(authentication.authorities).thenReturn(authorities)
        val securityContext = mock(SecurityContext::class.java)
        `when`(securityContext.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(securityContext)
    }

    private fun anyUsuario(): Usuario {
        any(Usuario::class.java)
        return Usuario(nome = "", senhaHash = "")
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `login deve autenticar usuario, deletar tokens antigos e retornar access e refresh token`() {
        val request = LoginRequest("joao@email.com", "senha123")
        val refreshToken =
            RefreshToken(token = "refresh-123", usuario = usuario, expiracao = Instant.now().plusSeconds(3600))

        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenReturn(UsernamePasswordAuthenticationToken(request.username, request.password))
        `when`(usuarioRepository.findByIdentificador(request.username)).thenReturn(Optional.of(usuario))
        `when`(jwtService.generateToken(usuario)).thenReturn("access-token-123")
        `when`(refreshTokenRepository.save(any(RefreshToken::class.java))).thenReturn(refreshToken)

        val result = authService.login(request)

        assertNotNull(result)
        assertEquals("access-token-123", result.token)
        assertEquals("refresh-123", result.refreshToken)

        verify(refreshTokenRepository).deleteByUsuario(usuario)
    }

    @Test
    fun `login deve lancar UnauthorizedActionException se usuario for CONSUMIDOR`() {
        val usuarioConsumidor = usuario.copy(perfis = mutableSetOf(perfilConsumidor))
        val request = LoginRequest("consumidor@email.com", "senha123")

        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenReturn(UsernamePasswordAuthenticationToken(request.username, request.password))
        `when`(usuarioRepository.findByIdentificador(request.username)).thenReturn(Optional.of(usuarioConsumidor))

        val exception =
            assertThrows(UnauthorizedActionException::class.java) {
                authService.login(request)
            }

        assertEquals("Acesso restrito a administradores e gerenciadores na versão atual.", exception.message)
    }

    @Test
    fun `refreshToken deve gerar novos tokens se refresh token for valido`() {
        val oldRefreshToken =
            RefreshToken(token = "old-refresh", usuario = usuario, expiracao = Instant.now().plusSeconds(3600))
        val newRefreshToken =
            RefreshToken(token = "new-refresh", usuario = usuario, expiracao = Instant.now().plusSeconds(3600))
        val request = RefreshTokenRequest("old-refresh")

        `when`(refreshTokenRepository.findByToken("old-refresh")).thenReturn(Optional.of(oldRefreshToken))
        `when`(jwtService.generateToken(usuario)).thenReturn("new-access")
        `when`(refreshTokenRepository.save(any(RefreshToken::class.java))).thenReturn(newRefreshToken)

        val result = authService.refreshToken(request)

        assertNotNull(result)
        assertEquals("new-access", result.token)
        assertEquals("new-refresh", result.refreshToken)
        assertTrue(oldRefreshToken.revogado)
    }

    @Test
    fun `refreshToken deve lancar InvalidTokenException se refresh token estiver revogado`() {
        val revokedToken =
            RefreshToken(
                token = "revoked",
                usuario = usuario,
                expiracao = Instant.now().plusSeconds(3600),
                revogado = true,
            )
        val request = RefreshTokenRequest("revoked")

        `when`(refreshTokenRepository.findByToken("revoked")).thenReturn(Optional.of(revokedToken))

        val exception =
            assertThrows(InvalidTokenException::class.java) {
                authService.refreshToken(request)
            }

        assertEquals("Refresh token revogado", exception.message)
    }

    @Test
    fun `refreshToken deve lancar InvalidTokenException se refresh token estiver expirado`() {
        val expiredToken =
            RefreshToken(token = "expired", usuario = usuario, expiracao = Instant.now().minusSeconds(3600))
        val request = RefreshTokenRequest("expired")

        `when`(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expiredToken))

        val exception =
            assertThrows(InvalidTokenException::class.java) {
                authService.refreshToken(request)
            }

        assertEquals("Refresh token expirado", exception.message)
        verify(refreshTokenRepository).delete(expiredToken)
    }

    @Test
    fun `register deve salvar e retornar DTO do usuario com perfil associado`() {
        val request =
            RegisterRequest(
                nome = "Maria",
                email = "maria@email.com",
                telefone = "87888888888",
                senha = "senha_plana",
                perfis = setOf("CONSUMIDOR"),
            )

        val novoUsuario = usuario.copy(nome = "Maria", email = "maria@email.com", telefone = "87888888888")

        `when`(usuarioRepository.existsByEmailIncludingDeleted(request.email!!)).thenReturn(false)
        `when`(usuarioRepository.existsByTelefoneIncludingDeleted(request.telefone!!)).thenReturn(false)
        `when`(perfilRepository.findByNome("CONSUMIDOR")).thenReturn(Optional.of(perfilConsumidor))
        `when`(passwordEncoder.encode(request.senha)).thenReturn("senha_encriptada_maria")
        `when`(usuarioRepository.save(any(Usuario::class.java))).thenReturn(novoUsuario)

        val result = authService.register(request)

        assertNotNull(result)
        assertEquals("Maria", result.nome)
        assertEquals("maria@email.com", result.email)
        verify(usuarioRepository).save(any(Usuario::class.java))
    }

    @Test
    fun `register deve lancar UnauthorizedActionException se GERENCIADOR tentar criar ADMINISTRADOR`() {
        mockAuthentication(listOf("ROLE_GERENCIADOR"))
        val request =
            RegisterRequest(
                nome = "Admin",
                senha = "senha",
                perfis = setOf("ADMINISTRADOR"),
            )

        val exception =
            assertThrows(UnauthorizedActionException::class.java) {
                authService.register(request)
            }

        assertEquals(
            "O perfil GERENCIADOR não possui permissão para cadastrar administradores ou outros gerenciadores.",
            exception.message,
        )
    }

    @Test
    fun `register deve lancar UnauthorizedActionException se GERENCIADOR tentar criar outro GERENCIADOR`() {
        mockAuthentication(listOf("ROLE_GERENCIADOR"))
        val request =
            RegisterRequest(
                nome = "Gerente",
                senha = "senha",
                perfis = setOf("GERENCIADOR"),
            )

        val exception =
            assertThrows(UnauthorizedActionException::class.java) {
                authService.register(request)
            }

        assertEquals(
            "O perfil GERENCIADOR não possui permissão para cadastrar administradores ou outros gerenciadores.",
            exception.message,
        )
    }

    @Test
    fun `register deve lancar excecao se email ja estiver cadastrado`() {
        val request =
            RegisterRequest(
                nome = "Maria",
                email = "maria@email.com",
                senha = "senha",
                perfis = setOf("CONSUMIDOR"),
            )

        `when`(usuarioRepository.existsByEmailIncludingDeleted(request.email!!)).thenReturn(true)

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                authService.register(request)
            }

        assertEquals("E-mail já cadastrado", exception.message)
    }

    @Test
    fun `forgotPassword deve gerar token e disparar email se usuario possuir email e perfil permitido`() {
        val request = ForgotPasswordRequest("joao@email.com")
        `when`(usuarioRepository.findByIdentificador("joao@email.com")).thenReturn(Optional.of(usuario))

        authService.forgotPassword(request)

        verify(passwordResetTokenRepository).deleteByUsuario(usuario)
        verify(passwordResetTokenRepository).save(any(PasswordResetToken::class.java))
        verify(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString())
    }

    @Test
    fun `forgotPassword deve lancar excecao se usuario nao possuir email`() {
        val usuarioSemEmail = usuario.copy(email = null, telefone = "8799999999")
        val request = ForgotPasswordRequest("8799999999")
        `when`(usuarioRepository.findByIdentificador("8799999999")).thenReturn(Optional.of(usuarioSemEmail))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                authService.forgotPassword(request)
            }

        assertEquals("Recuperação de senha disponível apenas para usuários com e-mail cadastrado.", exception.message)
    }

    @Test
    fun `forgotPassword deve lancar UnauthorizedActionException se perfil nao for ADMINISTRADOR ou GERENCIADOR`() {
        val usuarioConsumidor = usuario.copy(perfis = mutableSetOf(perfilConsumidor))
        val request = ForgotPasswordRequest("joao@email.com")
        `when`(usuarioRepository.findByIdentificador("joao@email.com")).thenReturn(Optional.of(usuarioConsumidor))

        val exception =
            assertThrows(UnauthorizedActionException::class.java) {
                authService.forgotPassword(request)
            }

        assertEquals("Recuperação de senha não permitida para este perfil na versão atual.", exception.message)
    }

    @Test
    fun `resetPassword deve atualizar senha e marcar token como usado`() {
        val rawToken = "reset-token"
        val hashedToken = hashToken(rawToken)
        val resetToken =
            PasswordResetToken(token = hashedToken, usuario = usuario, expiracao = Instant.now().plusSeconds(3600))
        val request = ResetPasswordRequest(token = rawToken, novaSenha = "novaSenha123")

        `when`(passwordResetTokenRepository.findByToken(hashedToken)).thenReturn(Optional.of(resetToken))
        `when`(passwordEncoder.encode("novaSenha123")).thenReturn("nova_senha_hash")

        authService.resetPassword(request)

        verify(usuarioRepository).save(any(Usuario::class.java))
        assertTrue(resetToken.usado)
        verify(passwordResetTokenRepository).save(resetToken)
        verify(emailService).sendPasswordChangeNotification(usuario.email!!, usuario.nome)
    }

    @Test
    fun `resetPassword deve lancar InvalidTokenException se token ja foi usado`() {
        val rawToken = "reset-token"
        val hashedToken = hashToken(rawToken)
        val resetToken =
            PasswordResetToken(
                token = hashedToken,
                usuario = usuario,
                expiracao = Instant.now().plusSeconds(3600),
                usado = true,
            )
        val request = ResetPasswordRequest(token = rawToken, novaSenha = "novaSenha123")

        `when`(passwordResetTokenRepository.findByToken(hashedToken)).thenReturn(Optional.of(resetToken))

        val exception =
            assertThrows(InvalidTokenException::class.java) {
                authService.resetPassword(request)
            }

        assertEquals("Este token já foi utilizado", exception.message)
    }

    @Test
    fun `logout deve revogar refresh token`() {
        val refreshToken =
            RefreshToken(token = "refresh-to-revoke", usuario = usuario, expiracao = Instant.now().plusSeconds(3600))
        val request = LogoutRequest("refresh-to-revoke")

        `when`(refreshTokenRepository.findByToken("refresh-to-revoke")).thenReturn(Optional.of(refreshToken))

        authService.logout(request)

        assertTrue(refreshToken.revogado)
        verify(refreshTokenRepository).save(refreshToken)
    }

    @Test
    fun `logout deve lancar InvalidTokenException se refresh token nao existir`() {
        val request = LogoutRequest("token-inexistente")
        `when`(refreshTokenRepository.findByToken("token-inexistente")).thenReturn(Optional.empty())

        val exception =
            assertThrows(InvalidTokenException::class.java) {
                authService.logout(request)
            }

        assertEquals("Refresh token não encontrado", exception.message)
    }

    @Test
    fun `logout deve lancar InvalidTokenException se refresh token ja estiver revogado`() {
        val revokedToken =
            RefreshToken(
                token = "revoked",
                usuario = usuario,
                expiracao = Instant.now().plusSeconds(3600),
                revogado = true,
            )
        val request = LogoutRequest("revoked")

        `when`(refreshTokenRepository.findByToken("revoked")).thenReturn(Optional.of(revokedToken))

        val exception =
            assertThrows(InvalidTokenException::class.java) {
                authService.logout(request)
            }

        assertEquals("Refresh token já está revogado", exception.message)
    }
}
