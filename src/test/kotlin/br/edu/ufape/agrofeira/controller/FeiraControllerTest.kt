package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.enums.StatusFeira
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.service.FeiraService
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
import java.time.LocalDateTime
import java.util.UUID

@WebMvcTest(FeiraController::class)
@AutoConfigureMockMvc(addFilters = false)
class FeiraControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @MockitoBean
    lateinit var service: FeiraService

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var usuarioRepository: UsuarioRepository

    @Test
    fun `buscarPorId deve retornar feira quando existir`() {
        val id = UUID.randomUUID()
        val feira = Feira(id = id, dataHora = LocalDateTime.now(), status = StatusFeira.ABERTA)

        `when`(service.buscarPorId(id)).thenReturn(feira)

        mockMvc
            .perform(get("/api/v1/feiras/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(id.toString()))
    }
}
