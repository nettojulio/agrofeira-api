package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.entity.OfertaEstoque
import br.edu.ufape.agrofeira.domain.entity.Produto
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.dto.request.OfertaEstoqueRequest
import br.edu.ufape.agrofeira.dto.request.OfertaEstoqueUpdateRequest
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.service.OfertaEstoqueService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.UUID

@WebMvcTest(OfertaEstoqueController::class)
@AutoConfigureMockMvc(addFilters = false)
class OfertaEstoqueControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @MockitoBean
    lateinit var service: OfertaEstoqueService

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var usuarioRepository: UsuarioRepository

    private val id = UUID.randomUUID()

    @Test
    fun `cadastrar deve retornar 201 quando dados forem validos`() {
        val request = OfertaEstoqueRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN)
        val oferta =
            OfertaEstoque(
                id = id,
                feira = Feira(id = request.feiraId),
                comerciante = Usuario(id = request.comercianteId, nome = "Comerciante"),
                produto = Produto(id = request.produtoId, nome = "Produto"),
                quantidadeOfertada = request.quantidadeOfertada,
            )

        `when`(service.cadastrar(request)).thenReturn(oferta)

        mockMvc
            .perform(
                post("/api/v1/estoque-bancas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(id.toString()))
    }

    @Test
    fun `atualizar deve retornar 200 quando dados forem validos`() {
        val request = OfertaEstoqueUpdateRequest(BigDecimal("20.00"))
        val oferta = OfertaEstoque(id = id, quantidadeOfertada = request.quantidadeOfertada)

        `when`(service.atualizar(id, request)).thenReturn(oferta)

        mockMvc
            .perform(
                put("/api/v1/estoque-bancas/$id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.quantidadeOfertada").value(20.0))
    }

    @Test
    fun `buscarPorId deve retornar oferta quando existir`() {
        val oferta = OfertaEstoque(id = id)
        `when`(service.buscarPorId(id)).thenReturn(oferta)

        mockMvc
            .perform(get("/api/v1/estoque-bancas/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(id.toString()))
    }

    @Test
    fun `listarPorFeira deve retornar lista de ofertas`() {
        val feiraId = UUID.randomUUID()
        val ofertas = listOf(OfertaEstoque(id = id))
        `when`(service.listarPorFeira(feiraId)).thenReturn(ofertas)

        mockMvc
            .perform(get("/api/v1/estoque-bancas/feira/$feiraId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
    }
}
