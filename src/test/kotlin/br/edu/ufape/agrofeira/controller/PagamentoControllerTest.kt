package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.Pagamento
import br.edu.ufape.agrofeira.domain.entity.Pedido
import br.edu.ufape.agrofeira.domain.enums.StatusPagamento
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.service.PagamentoService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.UUID

@WebMvcTest(PagamentoController::class)
@AutoConfigureMockMvc(addFilters = false)
class PagamentoControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @MockitoBean
    lateinit var service: PagamentoService

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var usuarioRepository: UsuarioRepository

    @Test
    fun `buscarPorId deve retornar pagamento quando existir`() {
        val id = UUID.randomUUID()
        val pagamento =
            Pagamento(
                id = id,
                pedido = Pedido(id = UUID.randomUUID()),
                valor = BigDecimal("100.00"),
                status = StatusPagamento.PAGO,
            )

        `when`(service.buscarPorId(id)).thenReturn(pagamento)

        mockMvc
            .perform(get("/api/v1/pagamentos/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(id.toString()))
    }
}
