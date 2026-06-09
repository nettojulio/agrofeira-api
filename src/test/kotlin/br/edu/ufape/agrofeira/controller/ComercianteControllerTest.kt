package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.service.ComercianteService
import br.edu.ufape.agrofeira.service.EnderecoService
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
import java.util.UUID

@WebMvcTest(ComercianteController::class)
@AutoConfigureMockMvc(addFilters = false)
class ComercianteControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var service: ComercianteService

    @MockitoBean
    lateinit var enderecoService: EnderecoService

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var usuarioRepository: UsuarioRepository

    @Test
    fun `buscarPorId deve retornar comerciante quando existir`() {
        val id = UUID.randomUUID()
        val comerciante = Usuario(id = id, nome = "Comerciante Teste")

        `when`(service.buscarPorId(id)).thenReturn(comerciante)
        `when`(enderecoService.buscarPorUsuarioIdOuNulo(id)).thenReturn(null)

        mockMvc
            .perform(get("/api/v1/comerciantes/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.nome").value("Comerciante Teste"))
    }
}
