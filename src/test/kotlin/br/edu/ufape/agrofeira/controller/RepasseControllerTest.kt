package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.Repasse
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.dto.request.RepasseRequest
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.service.RepasseService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.UUID

@WebMvcTest(RepasseController::class)
@AutoConfigureMockMvc(addFilters = false)
class RepasseControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @MockitoBean
    lateinit var service: RepasseService

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var usuarioRepository: UsuarioRepository

    @Test
    fun `listarTodos deve retornar pagina de repasses`() {
        `when`(
            service.listarTodos(
                org.springframework.data.domain.PageRequest
                    .of(0, 10),
            ),
        ).thenReturn(PageImpl(emptyList()))

        mockMvc
            .perform(get("/api/v1/repasses"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.content").isArray)
    }

    @Test
    fun `listarPorComerciante deve retornar lista`() {
        val id = UUID.randomUUID()
        `when`(service.listarPorComerciante(id)).thenReturn(emptyList())

        mockMvc
            .perform(get("/api/v1/repasses/comerciante/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
    }

    @Test
    fun `registrarRepasse deve retornar 201 quando criado`() {
        val request = RepasseRequest(UUID.randomUUID())
        val repasse =
            Repasse(
                id = UUID.randomUUID(),
                comerciante = Usuario(id = UUID.randomUUID(), nome = "Comerciante"),
                valorBruto = BigDecimal("100.00"),
                valorLiquido = BigDecimal("100.00"),
            )

        `when`(service.registrar(request)).thenReturn(repasse)

        mockMvc
            .perform(
                post("/api/v1/repasses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
    }
}
