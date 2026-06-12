package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.entity.Produto
import br.edu.ufape.agrofeira.domain.entity.RateioResultado
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.domain.enums.StatusFeira
import br.edu.ufape.agrofeira.dto.request.FeiraRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.FeiraComercianteElegivelRepository
import br.edu.ufape.agrofeira.repository.FeiraProdutoElegivelRepository
import br.edu.ufape.agrofeira.repository.FeiraRepository
import br.edu.ufape.agrofeira.repository.OfertaEstoqueRepository
import br.edu.ufape.agrofeira.repository.PedidoRepository
import br.edu.ufape.agrofeira.repository.ProdutoRepository
import br.edu.ufape.agrofeira.repository.RateioResultadoRepository
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyList
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class FeiraServiceTest {
    @Mock
    lateinit var repository: FeiraRepository

    @Mock
    lateinit var rateioService: RateioService

    @Mock
    lateinit var pedidoRepository: PedidoRepository

    @Mock
    lateinit var ofertaEstoqueRepository: OfertaEstoqueRepository

    @Mock
    lateinit var rateioResultadoRepository: RateioResultadoRepository

    @Mock
    lateinit var comercianteService: ComercianteService

    @Mock
    lateinit var feiraComercianteElegivelRepository: FeiraComercianteElegivelRepository

    @Mock
    lateinit var feiraProdutoElegivelRepository: FeiraProdutoElegivelRepository

    @Mock
    lateinit var usuarioRepository: UsuarioRepository

    @Mock
    lateinit var produtoRepository: ProdutoRepository

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
    fun `criar deve salvar nova feira e associar elegiveis`() {
        val now = LocalDateTime.now()
        val comercianteId = UUID.randomUUID()
        val produtoId = UUID.randomUUID()
        val request =
            FeiraRequest(
                dataHora = now,
                status = StatusFeira.RASCUNHO,
                comercianteIds = listOf(comercianteId),
                produtoIds = listOf(produtoId),
            )

        val feiraSalva = Feira(id = id, dataHora = now, status = StatusFeira.RASCUNHO)
        val comerciante = Usuario(id = comercianteId, nome = "Elegivel")
        val produto = Produto(id = produtoId, nome = "Produto Elegivel")

        `when`(repository.save(any(Feira::class.java))).thenReturn(feiraSalva)
        `when`(usuarioRepository.findAllById(listOf(comercianteId))).thenReturn(listOf(comerciante))
        `when`(produtoRepository.findAllById(listOf(produtoId))).thenReturn(listOf(produto))

        val result = service.criar(request)

        assertNotNull(result)
        verify(feiraComercianteElegivelRepository).saveAll(anyList())
        verify(feiraProdutoElegivelRepository).saveAll(anyList())
    }

    @Test
    fun `detalharFeira deve retornar DTO com totais agregados`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(feira))
        `when`(pedidoRepository.findByFeiraId(id)).thenReturn(emptyList())
        `when`(ofertaEstoqueRepository.findByFeiraId(id)).thenReturn(emptyList())
        `when`(rateioResultadoRepository.findByFeiraId(id)).thenReturn(emptyList())

        val result = service.detalharFeira(id)

        assertEquals(id, result.id)
        assertEquals(0, result.totalPedidos)
        assertEquals(BigDecimal.ZERO, result.totalRateado)
    }

    @Test
    fun `buscarRateioDaFeira deve agrupar resultados por comerciante`() {
        val comerciante = Usuario(id = UUID.randomUUID(), nome = "Comerciante")
        val rateio =
            RateioResultado(
                id = UUID.randomUUID(),
                feira = feira,
                comerciante = comerciante,
                produto = Produto(id = UUID.randomUUID()),
                quantidadeSequestrada = BigDecimal.TEN,
                valorBrutoVenda = BigDecimal("50.00"),
            )

        `when`(repository.findById(id)).thenReturn(Optional.of(feira))
        `when`(rateioResultadoRepository.findByFeiraId(id)).thenReturn(listOf(rateio))

        val result = service.buscarRateioDaFeira(id)

        assertEquals(1, result.comerciantes.size)
        assertEquals(BigDecimal("50.00"), result.totalRateado)
        assertEquals("Comerciante", result.comerciantes[0].comerciante.nome)
    }

    @Test
    fun `buscarParticipacaoComerciante deve retornar dados do comerciante na feira`() {
        val comercianteId = UUID.randomUUID()
        val comerciante = Usuario(id = comercianteId, nome = "Comerciante")

        `when`(repository.findById(id)).thenReturn(Optional.of(feira))
        `when`(comercianteService.buscarPorId(comercianteId)).thenReturn(comerciante)
        `when`(ofertaEstoqueRepository.findByFeiraIdAndComercianteId(id, comercianteId)).thenReturn(emptyList())
        `when`(rateioResultadoRepository.findByFeiraIdAndComercianteId(id, comercianteId)).thenReturn(emptyList())

        val result = service.buscarParticipacaoComerciante(id, comercianteId)

        assertEquals("Comerciante", result.comerciante.nome)
        assertTrue(result.ofertas.isEmpty())
        assertEquals(BigDecimal.ZERO, result.totalOfertado)
    }

    @Test
    fun `deletar deve chamar repository delete`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(feira))

        service.deletar(id)

        verify(repository).delete(feira)
    }
}
