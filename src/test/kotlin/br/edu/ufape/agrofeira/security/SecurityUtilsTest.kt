package br.edu.ufape.agrofeira.security

import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.service.CustomUserDetails
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

class SecurityUtilsTest {
    private val securityUtils = SecurityUtils()
    private val userId = UUID.randomUUID()
    private val otherUserId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    private fun setupMockAuthentication(
        principal: Any,
        authorities: List<String> = emptyList(),
    ) {
        val auth = mock(Authentication::class.java)
        `when`(auth.principal).thenReturn(principal)
        `when`(auth.authorities).thenReturn(authorities.map { SimpleGrantedAuthority(it) })

        val context = mock(SecurityContext::class.java)
        `when`(context.authentication).thenReturn(auth)
        SecurityContextHolder.setContext(context)
    }

    // --- Testes para isResourceOwner ---

    @Test
    fun `isResourceOwner deve retornar false se nao houver autenticacao no contexto`() {
        assertFalse(securityUtils.isResourceOwner(userId))
    }

    @Test
    fun `isResourceOwner deve retornar false se o principal nao for CustomUserDetails`() {
        setupMockAuthentication("anonymousUser")
        assertFalse(securityUtils.isResourceOwner(userId))
    }

    @Test
    fun `isResourceOwner deve retornar false se o ID do usuario logado for diferente do solicitado`() {
        val usuario = Usuario(id = otherUserId, nome = "Outro", senhaHash = "")
        setupMockAuthentication(CustomUserDetails(usuario))

        assertFalse(securityUtils.isResourceOwner(userId))
    }

    @Test
    fun `isResourceOwner deve retornar true se o ID do usuario logado for igual ao solicitado`() {
        val usuario = Usuario(id = userId, nome = "Dono", senhaHash = "")
        setupMockAuthentication(CustomUserDetails(usuario))

        assertTrue(securityUtils.isResourceOwner(userId))
    }

    // --- Testes para isManagerOrAdmin ---

    @Test
    fun `isManagerOrAdmin deve retornar false se nao houver autenticacao no contexto`() {
        assertFalse(securityUtils.isManagerOrAdmin())
    }

    @Test
    fun `isManagerOrAdmin deve retornar false se o usuario nao possuir authorities`() {
        setupMockAuthentication(mock(CustomUserDetails::class.java), emptyList())
        assertFalse(securityUtils.isManagerOrAdmin())
    }

    @Test
    fun `isManagerOrAdmin deve retornar false se o usuario tiver apenas perfil CONSUMIDOR`() {
        setupMockAuthentication(mock(CustomUserDetails::class.java), listOf("ROLE_CONSUMIDOR"))
        assertFalse(securityUtils.isManagerOrAdmin())
    }

    @Test
    fun `isManagerOrAdmin deve retornar false se o usuario tiver apenas perfil COMERCIANTE`() {
        setupMockAuthentication(mock(CustomUserDetails::class.java), listOf("ROLE_COMERCIANTE"))
        assertFalse(securityUtils.isManagerOrAdmin())
    }

    @Test
    fun `isManagerOrAdmin deve retornar true se o usuario tiver perfil ADMINISTRADOR`() {
        setupMockAuthentication(mock(CustomUserDetails::class.java), listOf("ROLE_ADMINISTRADOR"))
        assertTrue(securityUtils.isManagerOrAdmin())
    }

    @Test
    fun `isManagerOrAdmin deve retornar true se o usuario tiver perfil GERENCIADOR`() {
        setupMockAuthentication(mock(CustomUserDetails::class.java), listOf("ROLE_GERENCIADOR"))
        assertTrue(securityUtils.isManagerOrAdmin())
    }

    @Test
    fun `isManagerOrAdmin deve retornar true se o usuario tiver perfil ADMINISTRADOR e outros perfis`() {
        setupMockAuthentication(mock(CustomUserDetails::class.java), listOf("ROLE_ADMINISTRADOR", "ROLE_CONSUMIDOR"))
        assertTrue(securityUtils.isManagerOrAdmin())
    }
}
