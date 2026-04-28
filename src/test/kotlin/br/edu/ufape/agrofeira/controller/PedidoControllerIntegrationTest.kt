package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.*
import br.edu.ufape.agrofeira.domain.enums.TipoRetirada
import br.edu.ufape.agrofeira.dto.request.ItemPedidoRequest
import br.edu.ufape.agrofeira.dto.request.PedidoRequest
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
class PedidoControllerIntegrationTest {
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
    lateinit var produtoRepository: ProdutoRepository

    @Autowired
    lateinit var ofertaEstoqueRepository: OfertaEstoqueRepository

    @Autowired
    lateinit var pedidoRepository: PedidoRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtService: JwtService

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var adminToken: String
    private lateinit var consumidorToken: String
    private lateinit var consumidorId: UUID
    private lateinit var feiraId: UUID
    private lateinit var produtoId: UUID

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute(
            "TRUNCATE TABLE usuario_perfil, usuarios, perfis, feiras, produtos, ofertas_estoque, pedidos, itens_pedido CASCADE",
        )

        val pAdmin = perfilRepository.save(Perfil(nome = "ADMINISTRADOR"))
        val pConsumidor = perfilRepository.save(Perfil(nome = "CONSUMIDOR"))

        val admin =
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

        val comerciante =
            usuarioRepository.save(
                Usuario(
                    nome = "Comerciante Teste",
                    email = "comerciante@email.com",
                    senhaHash = passwordEncoder.encode("senha123")!!,
                    perfis = mutableSetOf(perfilRepository.save(Perfil(nome = "COMERCIANTE"))),
                ),
            )

        val feira = feiraRepository.save(Feira())
        feiraId = feira.id

        val produto = produtoRepository.save(Produto(nome = "Alface", precoBase = BigDecimal("3.50")))
        produtoId = produto.id

        ofertaEstoqueRepository.save(
            OfertaEstoque(
                feira = feira,
                comerciante = comerciante,
                produto = produto,
                quantidadeOfertada = BigDecimal("100"),
                quantidadeReservada = BigDecimal.ZERO,
            ),
        )
    }

    @Test
    fun `criar pedido deve retornar 201 quando feito por Administrador`() {
        val request =
            PedidoRequest(
                feiraId = feiraId,
                tipoRetirada = TipoRetirada.LOCAL,
                itens = listOf(ItemPedidoRequest(produtoId, BigDecimal("2"))),
            )

        mockMvc
            .perform(
                post("/api/v1/pedidos")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.valorTotal").value(7.00))
    }

    @Test
    fun `criar pedido deve retornar 403 quando tentado por Consumidor (conforme regra restrita)`() {
        val request =
            PedidoRequest(
                feiraId = feiraId,
                tipoRetirada = TipoRetirada.LOCAL,
                itens = listOf(ItemPedidoRequest(produtoId, BigDecimal("2"))),
            )

        mockMvc
            .perform(
                post("/api/v1/pedidos")
                    .header("Authorization", "Bearer $consumidorToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `listar deve retornar 200 para Admin`() {
        // Primeiro cria um pedido via service ou repo para ter o que listar
        val p =
            Pedido(
                feira = feiraRepository.findById(feiraId).get(),
                consumidor = usuarioRepository.findById(consumidorId).get(),
            )
        pedidoRepository.save(p)

        mockMvc
            .perform(
                get("/api/v1/pedidos")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.totalElements").value(1))
    }

    @Test
    fun `buscarPorId deve retornar 200 se for o dono do pedido`() {
        val p =
            pedidoRepository.save(
                Pedido(
                    feira = feiraRepository.findById(feiraId).get(),
                    consumidor = usuarioRepository.findById(consumidorId).get(),
                ),
            )

        mockMvc
            .perform(
                get("/api/v1/pedidos/${p.id}")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `deletar deve retornar 200 e aplicar Soft Delete`() {
        val p =
            pedidoRepository.save(
                Pedido(
                    feira = feiraRepository.findById(feiraId).get(),
                    consumidor = usuarioRepository.findById(consumidorId).get(),
                ),
            )

        mockMvc
            .perform(
                delete("/api/v1/pedidos/${p.id}")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)

        val deletadoEm =
            jdbcTemplate.queryForObject(
                "SELECT deletado_em FROM pedidos WHERE id = '${p.id}'",
                java.sql.Timestamp::class.java,
            )
        assert(deletadoEm != null)
    }
}
