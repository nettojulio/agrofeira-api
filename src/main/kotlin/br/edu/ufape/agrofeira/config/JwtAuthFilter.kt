package br.edu.ufape.agrofeira.config

import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.service.CustomUserDetails
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.ObjectProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val usuarioRepositoryProvider: ObjectProvider<UsuarioRepository>,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response)
            return
        }

        val username = jwtService.extractUsername(token)

        val usuario =
            usuarioRepositoryProvider.ifAvailable?.findByIdentificador(username)?.orElse(null)
                ?: run {
                    filterChain.doFilter(request, response)
                    return
                }

        val authorities = usuario.perfis.map { SimpleGrantedAuthority("ROLE_${it.nome}") }
        val userDetails =
            CustomUserDetails(usuario)

        val auth =
            UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities,
            )
        SecurityContextHolder.getContext().authentication = auth
        filterChain.doFilter(request, response)
    }
}
