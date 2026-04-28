package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.repository.PerfilRepository
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val perfilRepository: PerfilRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun cadastrar(
        usuario: Usuario,
        nomesPerfis: Set<String>,
    ): Usuario {
        if (usuario.email != null && usuarioRepository.existsByEmailIncludingDeleted(usuario.email!!)) {
            throw IllegalArgumentException("E-mail já cadastrado")
        }
        if (usuario.telefone != null && usuarioRepository.existsByTelefoneIncludingDeleted(usuario.telefone!!)) {
            throw IllegalArgumentException("Telefone já cadastrado")
        }

        val perfis =
            nomesPerfis
                .map { nome ->
                    perfilRepository
                        .findByNome(nome)
                        .orElseThrow { IllegalArgumentException("Perfil $nome não encontrado") }
                }.toMutableSet()

        val usuarioComSenhaEPerfis =
            usuario.copy(
                senhaHash = passwordEncoder.encode(usuario.senhaHash) ?: "",
                perfis = perfis,
            )

        return usuarioRepository.save(usuarioComSenhaEPerfis)
    }

    fun buscarPorId(id: UUID): Usuario =
        usuarioRepository
            .findById(id)
            .orElseThrow { NoSuchElementException("Usuário não encontrado") }

    @Transactional
    fun deletar(id: UUID) {
        buscarPorId(id)
        usuarioRepository.deletarLogicamente(id)
    }
}
