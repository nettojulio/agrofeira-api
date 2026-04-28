package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.*
import br.edu.ufape.agrofeira.domain.enums.StatusPagamento
import br.edu.ufape.agrofeira.dto.request.PagamentoRequest
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
import java.math.BigDecimal
import java.util.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["app.scheduling.enabled=false"],
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class PagamentoControllerIntegrationTest {
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
    lateinit var feiraRepository: FeiraRepository

    @Autowired
    lateinit var pedidoRepository: PedidoRepository

    @Autowired
    lateinit var pagamentoRepository: PagamentoRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtService: JwtService

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var adminToken: String
    private lateinit var consumidorToken: String
    private lateinit var consumidorId: UUID
    private lateinit var pedidoId: UUID

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("TRUNCATE TABLE usuario_perfil, usuarios, perfis, feiras, pedidos, pagamentos CASCADE")

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

        val consumidor =
            usuarioRepository.save(
                Usuario(
                    nome = "João",
                    email = "joao@email.com",
                    senhaHash = passwordEncoder.encode("senha123")!!,
                    perfis = mutableSetOf(pConsumidor),
                ),
            )
        consumidorId = consumidor.id
        consumidorToken = jwtService.generateToken("joao@email.com")

        val feira = feiraRepository.save(Feira())
        val pedido = pedidoRepository.save(Pedido(feira = feira, consumidor = consumidor))
        pedidoId = pedido.id
    }

    @Test
    fun `registrar pagamento deve retornar 201 para Admin`() {
        val request = PagamentoRequest(pedidoId, BigDecimal("150.00"), "DINHEIRO", StatusPagamento.PAGO)

        mockMvc
            .perform(
                post("/api/v1/pagamentos")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.valor").value(150.00))
            .andExpect(jsonPath("$.data.status").value("PAGO"))
    }

    @Test
    fun `registrar pagamento deve retornar 403 para Consumidor`() {
        val request = PagamentoRequest(pedidoId, BigDecimal("10.00"), "PIX")

        mockMvc
            .perform(
                post("/api/v1/pagamentos")
                    .header("Authorization", "Bearer $consumidorToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `listarPorPedido deve retornar 200 se for o dono do pedido`() {
        val p =
            pagamentoRepository.save(
                Pagamento(
                    pedido = pedidoRepository.findById(pedidoId).get(),
                    valor = BigDecimal("50"),
                ),
            )

        mockMvc
            .perform(
                get("/api/v1/pagamentos/pedido/$pedidoId")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
    }

    @Test
    fun `deletar deve retornar 200 para Admin e marcar como inativo`() {
        val p =
            pagamentoRepository.save(
                Pagamento(
                    pedido = pedidoRepository.findById(pedidoId).get(),
                    valor = BigDecimal("50"),
                ),
            )

        mockMvc
            .perform(
                delete("/api/v1/pagamentos/${p.id}")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)

        val ativo =
            jdbcTemplate.queryForObject("SELECT ativo FROM pagamentos WHERE id = '${p.id}'", Boolean::class.java)
        assert(ativo == false)
    }
}
