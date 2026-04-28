package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.domain.entity.PasswordResetToken
import br.edu.ufape.agrofeira.domain.entity.Perfil
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.dto.request.*
import br.edu.ufape.agrofeira.repository.PasswordResetTokenRepository
import br.edu.ufape.agrofeira.repository.PerfilRepository
import br.edu.ufape.agrofeira.repository.RefreshTokenRepository
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.mail.internet.MimeMessage
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["app.scheduling.enabled=false"],
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AuthControllerIntegrationTest {
    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun javaMailSender(): JavaMailSender {
            val mailSender = mock(JavaMailSender::class.java)
            val mimeMessage = mock(MimeMessage::class.java)
            `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)
            return mailSender
        }
    }

    companion object {
        @Container
        @ServiceConnection
        val postgres =
            PostgreSQLContainer("postgres:18-alpine").apply {
                withDatabaseName("testdb")
                withUsername("test")
                withPassword("test")
            }
    }

    @Autowired
    lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    lateinit var perfilRepository: PerfilRepository

    @Autowired
    lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    lateinit var passwordResetTokenRepository: PasswordResetTokenRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var mailSender: JavaMailSender

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("TRUNCATE TABLE password_reset_tokens, refresh_tokens, usuario_perfil, usuarios, perfis CASCADE")
        perfilRepository.save(Perfil(nome = "CONSUMIDOR"))
        perfilRepository.save(Perfil(nome = "ADMINISTRADOR"))
        perfilRepository.save(Perfil(nome = "GERENCIADOR"))
        perfilRepository.save(Perfil(nome = "COMERCIANTE"))

        usuarioRepository.save(
            Usuario(
                nome = "Usuario Teste",
                email = "teste@email.com",
                telefone = "87999991111",
                senhaHash = passwordEncoder.encode("senha123")!!,
                perfis =
                    mutableSetOf(
                        perfilRepository.findByNome("ADMINISTRADOR").get(),
                    ), // Changed to ADMIN for valid reset tests
            ),
        )
    }

    @Test
    fun `login deve retornar access e refresh token`() {
        val request = LoginRequest("teste@email.com", "senha123")

        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.token").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
    }

    @Test
    fun `refresh deve renovar tokens com sucesso`() {
        val loginRequest = LoginRequest("teste@email.com", "senha123")
        val loginResponse =
            mockMvc
                .perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)),
                ).andReturn()
                .response.contentAsString

        val refreshToken =
            objectMapper
                .readTree(loginResponse)
                .path("data")
                .path("refreshToken")
                .asText()
        val refreshRequest = RefreshTokenRequest(refreshToken)

        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refreshRequest)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.token").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").value(not(refreshToken)))
    }

    @Test
    @WithMockUser(roles = ["ADMINISTRADOR"])
    fun `logout deve invalidar refresh token`() {
        val loginRequest = LoginRequest("teste@email.com", "senha123")
        val loginResponse =
            mockMvc
                .perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)),
                ).andReturn()
                .response.contentAsString

        val refreshToken =
            objectMapper
                .readTree(loginResponse)
                .path("data")
                .path("refreshToken")
                .asText()
        val logoutRequest = LogoutRequest(refreshToken)

        mockMvc
            .perform(
                post("/api/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(logoutRequest)),
            ).andExpect(status().isOk)

        val refreshRequest = RefreshTokenRequest(refreshToken)
        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refreshRequest)),
            ).andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.errors[0].detail").value("Refresh token revogado"))
    }

    @Test
    fun `refresh deve retornar 401 ao usar refresh token inexistente`() {
        val refreshRequest = RefreshTokenRequest("token-inexistente")

        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refreshRequest)),
            ).andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.errors[0].detail").value("Refresh token não encontrado"))
    }

    @Test
    @WithMockUser(roles = ["ADMINISTRADOR"])
    fun `logout deve retornar 400 se token nao for fornecido`() {
        val invalidRequest = mapOf("outrocampo" to "valor")

        mockMvc
            .perform(
                post("/api/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    @WithMockUser(roles = ["ADMINISTRADOR"])
    fun `logout deve retornar 401 para token inexistente`() {
        val logoutRequest = LogoutRequest("token-inexistente")

        mockMvc
            .perform(
                post("/api/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(logoutRequest)),
            ).andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.errors[0].detail").value("Refresh token não encontrado"))
    }

    @Test
    @WithMockUser(roles = ["ADMINISTRADOR"])
    fun `register deve retornar 201 quando acessado por ADMINISTRADOR`() {
        val request =
            RegisterRequest(
                nome = "João Agricultor",
                email = "joao@email.com",
                telefone = "87988887777",
                senha = "senha123",
                perfis = setOf("CONSUMIDOR"),
            )

        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    @WithMockUser(roles = ["GERENCIADOR"])
    fun `register deve retornar 201 quando acessado por GERENCIADOR`() {
        val request =
            RegisterRequest(
                nome = "João Gerente",
                email = "gerente@email.com",
                telefone = "87988886666",
                senha = "senha123",
                perfis = setOf("COMERCIANTE"),
            )

        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `register deve retornar 401 quando acessado sem autenticacao`() {
        val request =
            RegisterRequest(
                nome = "João",
                senha = "senha123",
                perfis = setOf("CONSUMIDOR"),
            )

        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(roles = ["CONSUMIDOR"])
    fun `register deve retornar 403 quando acessado por perfil sem permissao`() {
        val request =
            RegisterRequest(
                nome = "João",
                senha = "senha123",
                perfis = setOf("CONSUMIDOR"),
            )

        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["GERENCIADOR"])
    fun `register deve retornar 403 quando GERENCIADOR tenta criar ADMINISTRADOR`() {
        val request =
            RegisterRequest(
                nome = "Admin",
                senha = "senha123",
                perfis = setOf("ADMINISTRADOR"),
            )

        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isForbidden)
            .andExpect(jsonPath("$.errors[0].rule").value("UNAUTHORIZED_ACTION"))
    }

    @Test
    @WithMockUser(roles = ["ADMINISTRADOR"])
    fun `register deve retornar 400 ao tentar cadastrar mais de um perfil`() {
        val request =
            RegisterRequest(
                nome = "João",
                senha = "senha123",
                perfis = setOf("CONSUMIDOR", "COMERCIANTE"),
            )

        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Erro de validação nos campos"))
    }

    @Test
    fun `forgot-password deve retornar 200 para ADMINISTRADOR`() {
        val request = ForgotPasswordRequest("teste@email.com")

        mockMvc
            .perform(
                post("/api/v1/auth/forgot-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Se o usuário estiver cadastrado, um link de recuperação será enviado."))
    }

    @Test
    fun `forgot-password deve retornar 403 se usuario for CONSUMIDOR`() {
        usuarioRepository.save(
            Usuario(
                nome = "Consumidor",
                email = "consumidor@email.com",
                senhaHash = "hash",
                perfis = mutableSetOf(perfilRepository.findByNome("CONSUMIDOR").get()),
            ),
        )
        val request = ForgotPasswordRequest("consumidor@email.com")

        mockMvc
            .perform(
                post("/api/v1/auth/forgot-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("Recuperação de senha não permitida para este perfil na versão atual."))
    }

    @Test
    fun `reset-password deve retornar 200 e atualizar senha com token valido`() {
        val usuario = usuarioRepository.findByEmail("teste@email.com").get()
        val rawToken = "token-valido"
        // SHA-256 hash of "token-valido"
        val hashedToken =
            MessageDigest
                .getInstance("SHA-256")
                .digest(rawToken.toByteArray())
                .joinToString("") { "%02x".format(it) }

        passwordResetTokenRepository.save(
            PasswordResetToken(
                token = hashedToken,
                usuario = usuario,
                expiracao = Instant.now().plusSeconds(600),
            ),
        )

        val request = ResetPasswordRequest(token = rawToken, novaSenha = "novaSenhaSegura")

        mockMvc
            .perform(
                post("/api/v1/auth/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso."))

        // Verify login works with new password
        val loginRequest = LoginRequest("teste@email.com", "novaSenhaSegura")
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)),
            ).andExpect(status().isOk)
    }

    @Test
    fun `reset-password deve retornar 401 para token ja utilizado`() {
        val usuario = usuarioRepository.findByEmail("teste@email.com").get()
        val rawToken = "token-usado"
        // SHA-256 hash of "token-usado"
        val hashedToken =
            MessageDigest
                .getInstance("SHA-256")
                .digest(rawToken.toByteArray())
                .joinToString("") { "%02x".format(it) }

        passwordResetTokenRepository.save(
            PasswordResetToken(
                token = hashedToken,
                usuario = usuario,
                expiracao = Instant.now().plusSeconds(600),
                usado = true,
            ),
        )

        val request = ResetPasswordRequest(token = rawToken, novaSenha = "novaSenhaSegura")

        mockMvc
            .perform(
                post("/api/v1/auth/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.errors[0].detail").value("Este token já foi utilizado"))
    }

    @Test
    @WithMockUser(roles = ["ADMINISTRADOR"])
    fun `register deve retornar 400 se o email ja existir em usuario deletado logicamente`() {
        val userToDelete =
            usuarioRepository.save(
                Usuario(
                    nome = "Usuario Fantasma",
                    email = "ghost@email.com",
                    senhaHash = "hash",
                    perfis = mutableSetOf(perfilRepository.findByNome("CONSUMIDOR").get()),
                ),
            )
        usuarioRepository.deletarLogicamente(userToDelete.id)

        val request =
            RegisterRequest(
                nome = "Novo João",
                email = "ghost@email.com",
                senha = "senhaSegura123",
                perfis = setOf("CONSUMIDOR"),
            )

        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("E-mail já cadastrado"))
    }
}
