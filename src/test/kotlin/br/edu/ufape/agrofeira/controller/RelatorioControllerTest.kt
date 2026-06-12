package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.service.RelatorioService
import br.edu.ufape.agrofeira.service.RepasseService
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

@WebMvcTest(RelatorioController::class)
@AutoConfigureMockMvc(addFilters = false)
class RelatorioControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @MockitoBean
    lateinit var service: RelatorioService

    @MockitoBean
    lateinit var repasseService: RepasseService

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var usuarioRepository: UsuarioRepository

    @Test
    fun `relatorioPorComerciante deve retornar lista`() {
        `when`(repasseService.relatorioGeralPorComerciante()).thenReturn(emptyList())

        mockMvc
            .perform(get("/api/v1/relatorios/por-comerciante"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
    }
}
