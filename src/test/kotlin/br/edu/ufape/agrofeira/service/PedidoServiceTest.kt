package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.*
import br.edu.ufape.agrofeira.domain.enums.TipoRetirada
import br.edu.ufape.agrofeira.dto.request.ItemPedidoRequest
import br.edu.ufape.agrofeira.dto.request.PedidoRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.ItemPedidoRepository
import br.edu.ufape.agrofeira.repository.OfertaEstoqueRepository
import br.edu.ufape.agrofeira.repository.PedidoRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class PedidoServiceTest {
    @Mock
    lateinit var repository: PedidoRepository

    @Mock
    lateinit var itemPedidoRepository: ItemPedidoRepository

    @Mock
    lateinit var feiraService: FeiraService

    @Mock
    lateinit var produtoService: ProdutoService

    @Mock
    lateinit var usuarioService: UsuarioService

    @Mock
    lateinit var reservaEstoqueService: ReservaEstoqueService

    @Mock
    lateinit var ofertaEstoqueRepository: OfertaEstoqueRepository

    @InjectMocks
    lateinit var pedidoService: PedidoService

    private val pedidoId = UUID.randomUUID()
    private val feiraId = UUID.randomUUID()
    private val consumidorId = UUID.randomUUID()
    private val produtoId = UUID.randomUUID()
    private val ofertaId = UUID.randomUUID()

    @Test
    fun `listar deve retornar pagina de pedidos`() {
        val pageable = PageRequest.of(0, 10)
        `when`(repository.findAll(pageable)).thenReturn(PageImpl(listOf(Pedido(id = pedidoId))))

        val result = pedidoService.listar(pageable)

        assertEquals(1, result.totalElements)
        assertEquals(pedidoId, result.content[0].id)
    }

    @Test
    fun `buscarPorId deve retornar pedido se existir`() {
        val pedido = Pedido(id = pedidoId)
        `when`(repository.findById(pedidoId)).thenReturn(Optional.of(pedido))

        val result = pedidoService.buscarPorId(pedidoId)

        assertEquals(pedidoId, result.id)
    }

    @Test
    fun `buscarPorId deve lancar excecao se pedido nao existir`() {
        `when`(repository.findById(pedidoId)).thenReturn(Optional.empty())

        assertThrows(ResourceNotFoundException::class.java) {
            pedidoService.buscarPorId(pedidoId)
        }
    }

    @Test
    fun `criar deve realizar pedido com sucesso e reservar estoque`() {
        val request =
            PedidoRequest(
                feiraId = feiraId,
                tipoRetirada = TipoRetirada.LOCAL,
                itens = listOf(ItemPedidoRequest(produtoId, BigDecimal.ONE)),
            )

        val feira = Feira(id = feiraId)
        val consumidor = Usuario(id = consumidorId, nome = "Consumidor")
        val produto = Produto(id = produtoId, nome = "Tomate", precoBase = BigDecimal("5.00"))
        val oferta = OfertaEstoque(id = ofertaId, feira = feira, produto = produto)

        `when`(feiraService.buscarPorId(feiraId)).thenReturn(feira)
        `when`(usuarioService.buscarPorId(consumidorId)).thenReturn(consumidor)
        `when`(produtoService.buscarPorId(produtoId)).thenReturn(produto)
        `when`(ofertaEstoqueRepository.buscarPorFeiraEProduto(feiraId, produtoId)).thenReturn(listOf(oferta))
        `when`(reservaEstoqueService.reservar(ofertaId, BigDecimal.ONE)).thenReturn(true)
        `when`(repository.save(any(Pedido::class.java))).thenAnswer { it.arguments[0] }

        val result = pedidoService.criar(request, consumidorId)

        assertNotNull(result)
        assertEquals(BigDecimal("5.00"), result.valorTotal)
        assertEquals(1, result.itens.size)
        verify(reservaEstoqueService).reservar(ofertaId, BigDecimal.ONE)
    }

    @Test
    fun `deletar deve chamar repository delete`() {
        val pedido = Pedido(id = pedidoId)
        `when`(repository.findById(pedidoId)).thenReturn(Optional.of(pedido))

        pedidoService.deletar(pedidoId)

        verify(repository).delete(pedido)
    }
}
