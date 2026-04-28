package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.entity.Perfil
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.domain.enums.StatusFeira
import br.edu.ufape.agrofeira.dto.request.FeiraRequest
import br.edu.ufape.agrofeira.repository.FeiraRepository
import br.edu.ufape.agrofeira.repository.PerfilRepository
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["app.scheduling.enabled=false"],
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class FeiraControllerIntegrationTest {
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
    lateinit var feiraRepository: FeiraRepository

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    lateinit var perfilRepository: PerfilRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtService: JwtService

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var adminToken: String
    private lateinit var feiraId: UUID

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("TRUNCATE TABLE usuario_perfil, usuarios, perfis, feiras CASCADE")

        val pAdmin = perfilRepository.save(Perfil(nome = "ADMINISTRADOR"))

        usuarioRepository.save(
            Usuario(
                nome = "Admin",
                email = "admin@email.com",
                senhaHash = passwordEncoder.encode("senha123")!!,
                perfis = mutableSetOf(pAdmin),
            ),
        )
        adminToken = jwtService.generateToken("admin@email.com")

        val feira = feiraRepository.save(Feira(dataHora = LocalDateTime.now().plusDays(1)))
        feiraId = feira.id
    }

    @Test
    fun `listar feiras deve retornar 200`() {
        mockMvc
            .perform(get("/api/v1/feiras"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.totalElements").value(1))
    }

    @Test
    fun `buscar por id deve retornar 200 se existir`() {
        mockMvc
            .perform(get("/api/v1/feiras/$feiraId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `criar feira deve retornar 201 para Admin`() {
        val request = FeiraRequest(dataHora = LocalDateTime.now().plusWeeks(1), status = StatusFeira.RASCUNHO)

        mockMvc
            .perform(
                post("/api/v1/feiras")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.status").value("RASCUNHO"))
    }

    @Test
    fun `atualizar status deve retornar 200`() {
        mockMvc
            .perform(
                patch("/api/v1/feiras/$feiraId/status")
                    .header("Authorization", "Bearer $adminToken")
                    .param("novoStatus", "ABERTA"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.status").value("ABERTA"))
    }

    @Test
    fun `deletar feira deve retornar 200 e aplicar Soft Delete`() {
        mockMvc
            .perform(
                delete("/api/v1/feiras/$feiraId")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)

        val ativo = jdbcTemplate.queryForObject("SELECT ativo FROM feiras WHERE id = '$feiraId'", Boolean::class.java)
        assert(ativo == false)
    }
}
