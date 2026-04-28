package br.edu.ufape.agrofeira.security

import br.edu.ufape.agrofeira.service.CustomUserDetails
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID

@Component("securityUtils")
class SecurityUtils {
    /**
     * Verifica se o usuário autenticado é o dono do recurso sendo acessado.
     */
    fun isResourceOwner(userId: UUID): Boolean {
        val auth = SecurityContextHolder.getContext().authentication ?: return false

        if (auth.principal !is CustomUserDetails) {
            return false
        }

        val tokenUserId = (auth.principal as CustomUserDetails).usuario.id
        return tokenUserId == userId
    }

    /**
     * Verifica se o usuário logado possui a role ADMINISTRADOR ou GERENCIADOR.
     */
    fun isManagerOrAdmin(): Boolean {
        val auth = SecurityContextHolder.getContext().authentication ?: return false
        return auth.authorities.any { it.authority == "ROLE_ADMINISTRADOR" || it.authority == "ROLE_GERENCIADOR" }
    }
}
