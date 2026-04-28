package br.edu.ufape.agrofeira.security.annotations

import org.springframework.security.access.prepost.PreAuthorize

/**
 * Permite o acesso a usuários com o perfil ADMINISTRADOR ou GERENCIADOR.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENCIADOR')")
annotation class IsManagerOrAdmin
