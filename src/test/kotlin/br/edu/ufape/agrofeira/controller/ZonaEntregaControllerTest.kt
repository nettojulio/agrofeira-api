package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.ZonaEntrega
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.service.ZonaEntregaService
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

@WebMvcTest(ZonaEntregaController::class)
@AutoConfigureMockMvc(addFilters = false)
class ZonaEntregaControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @MockitoBean
    lateinit var service: ZonaEntregaService

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var usuarioRepository: UsuarioRepository

    @Test
    fun `buscarPorId deve retornar zona quando existir`() {
        val id = UUID.randomUUID()
        val zona = ZonaEntrega(id = id, nome = "Zona Teste", taxa = BigDecimal("7.00"))

        `when`(service.buscarPorId(id)).thenReturn(zona)

        mockMvc
            .perform(get("/api/v1/zonas-entrega/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.nome").value("Zona Teste"))
    }
}
