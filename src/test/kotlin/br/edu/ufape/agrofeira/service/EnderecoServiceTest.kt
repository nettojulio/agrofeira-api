package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Endereco
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.domain.entity.ZonaEntrega
import br.edu.ufape.agrofeira.dto.request.EnderecoRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.EnderecoRepository
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.repository.ZonaEntregaRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class EnderecoServiceTest {
    @Mock
    private lateinit var repository: EnderecoRepository

    @Mock
    private lateinit var usuarioRepository: UsuarioRepository

    @Mock
    private lateinit var zonaEntregaRepository: ZonaEntregaRepository

    @InjectMocks
    private lateinit var service: EnderecoService

    private lateinit var usuario: Usuario
    private lateinit var zonaEntrega: ZonaEntrega
    private val usuarioId = UUID.randomUUID()
    private val zonaId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        usuario = Usuario(id = usuarioId, nome = "Teste", senhaHash = "123")
        zonaEntrega = ZonaEntrega(id = zonaId, bairro = "Centro", taxa = 5.0.toBigDecimal(), ativo = true)
    }

    @Test
    fun `buscarPorUsuarioId deve retornar endereco quando existir`() {
        val endereco = Endereco(usuario = usuario, zonaEntrega = zonaEntrega)
        endereco.usuarioId = usuarioId
        `when`(repository.findById(usuarioId)).thenReturn(Optional.of(endereco))

        val result = service.buscarPorUsuarioId(usuarioId)

        assertEquals(usuarioId, result.usuarioId)
        verify(repository).findById(usuarioId)
    }

    @Test
    fun `buscarPorUsuarioId deve lancar ResourceNotFoundException quando nao existir`() {
        `when`(repository.findById(usuarioId)).thenReturn(Optional.empty())

        assertThrows(ResourceNotFoundException::class.java) {
            service.buscarPorUsuarioId(usuarioId)
        }
    }

    @Test
    fun `salvarEndereco deve criar novo endereco quando nao existir`() {
        val request = EnderecoRequest("Rua 1", "123", null, "Cidade", "PE", "55290000", zonaId)

        `when`(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario))
        `when`(zonaEntregaRepository.findById(zonaId)).thenReturn(Optional.of(zonaEntrega))
        `when`(repository.findById(usuarioId)).thenReturn(Optional.empty())
        `when`(repository.save(any(Endereco::class.java))).thenAnswer { it.arguments[0] }

        val result = service.salvarEndereco(usuarioId, request)

        assertNotNull(result)
        assertEquals("Rua 1", result.rua)
        assertEquals(zonaEntrega, result.zonaEntrega)
        verify(repository).save(any(Endereco::class.java))
    }

    @Test
    fun `salvarEndereco deve lancar IllegalArgumentException se zona estiver inativa`() {
        val zonaInativa = ZonaEntrega(id = zonaId, bairro = "Centro", taxa = 5.0.toBigDecimal(), ativo = false)
        val request = EnderecoRequest("Rua 1", "123", null, "Cidade", "PE", "55290000", zonaId)

        `when`(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario))
        `when`(zonaEntregaRepository.findById(zonaId)).thenReturn(Optional.of(zonaInativa))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                service.salvarEndereco(usuarioId, request)
            }
        assertEquals("A Zona de Entrega selecionada está inativa", exception.message)
    }
}
