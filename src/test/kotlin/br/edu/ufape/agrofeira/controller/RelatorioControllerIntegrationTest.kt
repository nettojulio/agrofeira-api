package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.*
import br.edu.ufape.agrofeira.domain.enums.TipoRelatorio
import br.edu.ufape.agrofeira.dto.request.RelatorioRequest
import br.edu.ufape.agrofeira.repository.*
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
import java.util.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["app.scheduling.enabled=false"],
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class RelatorioControllerIntegrationTest {
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
    lateinit var relatorioRepository: RelatorioRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtService: JwtService

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var adminToken: String
    private lateinit var consumidorToken: String
    private lateinit var relatorioId: UUID

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("TRUNCATE TABLE usuario_perfil, usuarios, perfis, relatorios CASCADE")

        val pAdmin = perfilRepository.save(Perfil(nome = "ADMINISTRADOR"))
        val pConsumidor = perfilRepository.save(Perfil(nome = "CONSUMIDOR"))

        usuarioRepository.save(
            Usuario(
                nome = "Admin",
                email = "admin@email.com",
                senhaHash = passwordEncoder.encode("senha123")!!,
                perfis = mutableSetOf(pAdmin),
            ),
        )
        adminToken = jwtService.generateToken("admin@email.com")

        usuarioRepository.save(
            Usuario(
                nome = "João",
                email = "joao@email.com",
                senhaHash = passwordEncoder.encode("senha123")!!,
                perfis = mutableSetOf(pConsumidor),
            ),
        )
        consumidorToken = jwtService.generateToken("joao@email.com")

        val relatorio =
            relatorioRepository.save(
                Relatorio(titulo = "Faturamento Maio", tipo = TipoRelatorio.MENSAL),
            )
        relatorioId = relatorio.id
    }

    @Test
    fun `listar relatorios deve retornar 200 para Admin`() {
        mockMvc
            .perform(
                get("/api/v1/relatorios")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.totalElements").value(1))
    }

    @Test
    fun `listar relatorios deve retornar 403 para Consumidor`() {
        mockMvc
            .perform(
                get("/api/v1/relatorios")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `buscar por id deve retornar 200 para Admin`() {
        mockMvc
            .perform(
                get("/api/v1/relatorios/$relatorioId")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.titulo").value("Faturamento Maio"))
    }

    @Test
    fun `criar relatorio deve retornar 201 para Admin`() {
        val request = RelatorioRequest("Novo Relatório", TipoRelatorio.GERAL, "{\"par\": 1}")

        mockMvc
            .perform(
                post("/api/v1/relatorios")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.titulo").value("Novo Relatório"))
    }

    @Test
    fun `deletar deve retornar 200 e aplicar Soft Delete`() {
        mockMvc
            .perform(
                delete("/api/v1/relatorios/$relatorioId")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)

        val ativo =
            jdbcTemplate.queryForObject("SELECT ativo FROM relatorios WHERE id = '$relatorioId'", Boolean::class.java)
        assert(ativo == false)
    }
}
