package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(CategoriaController::class)
@AutoConfigureMockMvc(addFilters = false)
class CategoriaControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var usuarioRepository: UsuarioRepository

    @Test
    fun `listar deve retornar todas as categorias`() {
        mockMvc
            .perform(get("/api/v1/categorias"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Categorias recuperadas com sucesso"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)))
    }
}
