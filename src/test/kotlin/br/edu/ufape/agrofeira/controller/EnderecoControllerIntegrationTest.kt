package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.Perfil
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.domain.entity.ZonaEntrega
import br.edu.ufape.agrofeira.dto.request.EnderecoRequest
import br.edu.ufape.agrofeira.repository.EnderecoRepository
import br.edu.ufape.agrofeira.repository.PerfilRepository
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.repository.ZonaEntregaRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
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
class EnderecoControllerIntegrationTest {
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
    lateinit var zonaEntregaRepository: ZonaEntregaRepository

    @Autowired
    lateinit var enderecoRepository: EnderecoRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtService: JwtService

    @Autowired
    lateinit var jdbcTemplate: org.springframework.jdbc.core.JdbcTemplate

    private lateinit var consumidorToken: String
    private lateinit var adminToken: String
    private lateinit var consumidorId: UUID
    private lateinit var zonaId: UUID

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("TRUNCATE TABLE enderecos, usuario_perfil, usuarios, perfis, zonas_entrega CASCADE")

        val perfilAdmin = perfilRepository.save(Perfil(nome = "ADMINISTRADOR"))
        val perfilConsumidor = perfilRepository.save(Perfil(nome = "CONSUMIDOR"))

        val admin =
            usuarioRepository.save(
                Usuario(
                    nome = "Admin",
                    email = "admin@email.com",
                    senhaHash = passwordEncoder.encode("123")!!,
                    perfis = mutableSetOf(perfilAdmin),
                ),
            )
        adminToken = jwtService.generateToken(admin)

        val consumidor =
            usuarioRepository.save(
                Usuario(
                    nome = "João",
                    email = "joao@email.com",
                    senhaHash = passwordEncoder.encode("123")!!,
                    perfis = mutableSetOf(perfilConsumidor),
                ),
            )
        consumidorId = consumidor.id
        consumidorToken = jwtService.generateToken(consumidor)

        val zona =
            zonaEntregaRepository.save(
                ZonaEntrega(bairro = "Centro", taxa = 5.0.toBigDecimal(), ativo = true),
            )
        zonaId = zona.id
    }

    @Test
    fun `salvarMeuEndereco deve criar endereco com sucesso`() {
        val request = EnderecoRequest("Rua das Flores", "123", "Apto 1", "Garanhuns", "PE", "55290000", zonaId)

        mockMvc
            .perform(
                put("/api/v1/enderecos/me")
                    .header("Authorization", "Bearer $consumidorToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.rua").value("Rua das Flores"))
            .andExpect(jsonPath("$.data.zonaEntrega.bairro").value("Centro"))
    }

    @Test
    fun `buscarMeuEndereco deve retornar 404 se nao possuir endereco`() {
        mockMvc
            .perform(
                get("/api/v1/enderecos/me")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `fluxo completo de endereco para consumidor`() {
        val request = EnderecoRequest("Rua Nova", "456", null, "Garanhuns", "PE", "55290111", zonaId)

        // 1. Salva
        mockMvc
            .perform(
                put("/api/v1/enderecos/me")
                    .header("Authorization", "Bearer $consumidorToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)

        // 2. Busca
        mockMvc
            .perform(
                get("/api/v1/enderecos/me")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.rua").value("Rua Nova"))
            .andExpect(jsonPath("$.data.cep").value("55290111"))

        // 3. Deleta
        mockMvc
            .perform(
                delete("/api/v1/enderecos/me")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isOk)

        // 4. Busca novamente (404)
        mockMvc
            .perform(
                get("/api/v1/enderecos/me")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `admin deve conseguir gerenciar endereco de terceiros`() {
        val request = EnderecoRequest("Rua do Admin", "0", null, "Garanhuns", "PE", "55290000", zonaId)

        // Admin salva para o consumidor
        mockMvc
            .perform(
                put("/api/v1/enderecos/usuario/$consumidorId")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)

        // Admin busca do consumidor
        mockMvc
            .perform(
                get("/api/v1/enderecos/usuario/$consumidorId")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.rua").value("Rua do Admin"))
    }

    @Test
    fun `consumidor nao deve acessar endereco de terceiros`() {
        mockMvc
            .perform(
                get("/api/v1/enderecos/usuario/${UUID.randomUUID()}")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isForbidden)
    }
}
