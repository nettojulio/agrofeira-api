package br.edu.ufape.agrofeira.security.annotations

import org.springframework.security.access.prepost.PreAuthorize

/**
 * Permite o acesso apenas a usuários com o perfil GERENCIADOR.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('GERENCIADOR')")
annotation class IsManager
