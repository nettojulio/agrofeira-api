package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val usuarioRepository: UsuarioRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val usuario =
            usuarioRepository
                .findByIdentificador(username)
                .orElseThrow { UsernameNotFoundException("Usuário não encontrado com o identificador: $username") }

        return CustomUserDetails(usuario)
    }
}

class CustomUserDetails(
    val usuario: Usuario,
) : UserDetails {
    override fun getAuthorities() = usuario.perfis.map { SimpleGrantedAuthority("ROLE_${it.nome}") }

    override fun getPassword() = usuario.senhaHash

    override fun getUsername() = usuario.email ?: usuario.telefone ?: usuario.id.toString()

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = usuario.ativo
}
