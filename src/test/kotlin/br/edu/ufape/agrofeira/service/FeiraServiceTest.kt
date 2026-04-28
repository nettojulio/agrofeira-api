package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.enums.StatusFeira
import br.edu.ufape.agrofeira.dto.request.FeiraRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.FeiraRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class FeiraServiceTest {
    @Mock
    lateinit var repository: FeiraRepository

    @Mock
    lateinit var rateioService: RateioService

    @InjectMocks
    lateinit var service: FeiraService

    private val id = UUID.randomUUID()
    private lateinit var feira: Feira

    @BeforeEach
    fun setUp() {
        feira = Feira(id = id, dataHora = LocalDateTime.now(), status = StatusFeira.ABERTA)
    }

    @Test
    fun `listar deve retornar pagina de feiras`() {
        val pageable = PageRequest.of(0, 10)
        `when`(repository.findAll(pageable)).thenReturn(PageImpl(listOf(feira)))

        val result = service.listar(pageable)

        assertEquals(1, result.totalElements)
        verify(repository).findAll(pageable)
    }

    @Test
    fun `buscarPorId deve retornar feira se existir`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(feira))

        val result = service.buscarPorId(id)

        assertEquals(id, result.id)
    }

    @Test
    fun `buscarPorId deve lancar excecao se nao existir`() {
        `when`(repository.findById(id)).thenReturn(Optional.empty())

        assertThrows(ResourceNotFoundException::class.java) {
            service.buscarPorId(id)
        }
    }

    @Test
    fun `criar deve salvar nova feira`() {
        val now = LocalDateTime.now()
        val request = FeiraRequest(dataHora = now, status = StatusFeira.RASCUNHO)
        `when`(repository.save(any(Feira::class.java))).thenAnswer { it.arguments[0] }

        val result = service.criar(request)

        assertNotNull(result)
        assertEquals(now, result.dataHora)
        assertEquals(StatusFeira.RASCUNHO, result.status)
    }

    @Test
    fun `atualizar deve salvar alteracoes`() {
        val nextMonth = LocalDateTime.now().plusMonths(1)
        val request = FeiraRequest(dataHora = nextMonth, status = StatusFeira.ABERTA)
        `when`(repository.findById(id)).thenReturn(Optional.of(feira))
        `when`(repository.save(any(Feira::class.java))).thenAnswer { it.arguments[0] }

        val result = service.atualizar(id, request)

        assertEquals(nextMonth, result.dataHora)
        assertEquals(StatusFeira.ABERTA, result.status)
    }

    @Test
    fun `atualizarStatus para ENCERRADA deve disparar rateio`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(feira))
        `when`(repository.save(any(Feira::class.java))).thenAnswer { it.arguments[0] }

        service.atualizarStatus(id, StatusFeira.ENCERRADA)

        verify(rateioService).executarRateioDaFeira(feira)
        verify(repository).save(any(Feira::class.java))
    }

    @Test
    fun `deletar deve chamar repository delete`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(feira))

        service.deletar(id)

        verify(repository).delete(feira)
    }
}
