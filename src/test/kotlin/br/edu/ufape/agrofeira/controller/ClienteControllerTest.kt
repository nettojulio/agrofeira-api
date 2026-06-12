package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.service.ClienteService
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

@WebMvcTest(ClienteController::class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var service: ClienteService

    @MockitoBean
    lateinit var enderecoService: EnderecoService

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var usuarioRepository: UsuarioRepository

    @Test
    fun `buscarPorId deve retornar cliente quando existir`() {
        val id = UUID.randomUUID()
        val cliente = Usuario(id = id, nome = "Cliente Teste")

        `when`(service.buscarPorId(id)).thenReturn(cliente)
        `when`(enderecoService.buscarPorUsuarioIdOuNulo(id)).thenReturn(null)

        mockMvc
            .perform(get("/api/v1/clientes/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.nome").value("Cliente Teste"))
    }
}
