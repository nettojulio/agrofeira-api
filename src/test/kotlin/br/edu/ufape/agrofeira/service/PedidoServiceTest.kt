package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.entity.ItemPedido
import br.edu.ufape.agrofeira.domain.entity.OfertaEstoque
import br.edu.ufape.agrofeira.domain.entity.Pedido
import br.edu.ufape.agrofeira.domain.entity.Produto
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.domain.enums.StatusPedido
import br.edu.ufape.agrofeira.domain.enums.TipoRetirada
import br.edu.ufape.agrofeira.dto.request.ItemPedidoRequest
import br.edu.ufape.agrofeira.dto.request.PedidoRequest
import br.edu.ufape.agrofeira.exception.BusinessRuleException
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.ItemPedidoRepository
import br.edu.ufape.agrofeira.repository.OfertaEstoqueRepository
import br.edu.ufape.agrofeira.repository.PedidoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

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
        val oferta = OfertaEstoque(id = ofertaId, feira = feira, produto = produto, quantidadeOfertada = BigDecimal.TEN)

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
    fun `criar deve respeitar rotatividade usando oferta com menor reserva`() {
        val request = PedidoRequest(feiraId, TipoRetirada.LOCAL, listOf(ItemPedidoRequest(produtoId, BigDecimal.ONE)))
        val feira = Feira(id = feiraId)
        val produto = Produto(id = produtoId, nome = "Tomate", precoBase = BigDecimal("5.00"))

        val oferta1 =
            OfertaEstoque(
                id = UUID.randomUUID(),
                feira = feira,
                produto = produto,
                quantidadeOfertada = BigDecimal.TEN,
                quantidadeReservada = BigDecimal("5.00"),
            )
        val oferta2 =
            OfertaEstoque(
                id = UUID.randomUUID(),
                feira = feira,
                produto = produto,
                quantidadeOfertada = BigDecimal.TEN,
                quantidadeReservada = BigDecimal.ZERO,
            )

        `when`(feiraService.buscarPorId(feiraId)).thenReturn(feira)
        `when`(usuarioService.buscarPorId(consumidorId)).thenReturn(Usuario(id = consumidorId))
        `when`(produtoService.buscarPorId(produtoId)).thenReturn(produto)
        // O mock deve retornar em qualquer ordem, o service que deve ordenar
        `when`(ofertaEstoqueRepository.buscarPorFeiraEProduto(feiraId, produtoId)).thenReturn(listOf(oferta1, oferta2))
        `when`(reservaEstoqueService.reservar(oferta2.id, BigDecimal.ONE)).thenReturn(true)
        `when`(repository.save(any(Pedido::class.java))).thenAnswer { it.arguments[0] }

        pedidoService.criar(request, consumidorId)

        // Verifica que reservou na oferta2 (que tinha 0 reservado) em vez da oferta1 (que tinha 5)
        verify(reservaEstoqueService).reservar(oferta2.id, BigDecimal.ONE)
    }

    @Test
    fun `criar deve lancar excecao quando estoque for insuficiente em todos os vendedores`() {
        val request = PedidoRequest(feiraId, TipoRetirada.LOCAL, listOf(ItemPedidoRequest(produtoId, BigDecimal.TEN)))
        val feira = Feira(id = feiraId)
        val produto = Produto(id = produtoId, nome = "Tomate")
        val oferta =
            OfertaEstoque(
                id = ofertaId,
                feira = feira,
                produto = produto,
                quantidadeOfertada = BigDecimal("5.00"),
                quantidadeReservada = BigDecimal.ZERO,
            )

        `when`(feiraService.buscarPorId(feiraId)).thenReturn(feira)
        `when`(usuarioService.buscarPorId(consumidorId)).thenReturn(Usuario(id = consumidorId))
        `when`(produtoService.buscarPorId(produtoId)).thenReturn(produto)
        `when`(ofertaEstoqueRepository.buscarPorFeiraEProduto(feiraId, produtoId)).thenReturn(listOf(oferta))

        val ex =
            assertThrows(BusinessRuleException::class.java) {
                pedidoService.criar(request, consumidorId)
            }
        assertTrue(ex.message!!.contains("Estoque insuficiente"))
    }

    @Test
    fun `atualizarStatus deve permitir transicao valida`() {
        val pedido = Pedido(id = pedidoId, status = StatusPedido.PENDENTE)
        `when`(repository.findById(pedidoId)).thenReturn(Optional.of(pedido))
        `when`(repository.save(any(Pedido::class.java))).thenAnswer { it.arguments[0] }

        val result = pedidoService.atualizarStatus(pedidoId, StatusPedido.AGUARDANDO_SEPARACAO)

        assertEquals(StatusPedido.AGUARDANDO_SEPARACAO, result.status)
    }

    @Test
    fun `atualizarStatus deve lancar excecao para transicao invalida`() {
        val pedido = Pedido(id = pedidoId, status = StatusPedido.ENTREGUE)
        `when`(repository.findById(pedidoId)).thenReturn(Optional.of(pedido))

        assertThrows(BusinessRuleException::class.java) {
            pedidoService.atualizarStatus(pedidoId, StatusPedido.PENDENTE)
        }
    }

    @Test
    fun `atualizarStatus para CANCELADO deve liberar reservas`() {
        val feira = Feira(id = feiraId)
        val pedido = Pedido(id = pedidoId, status = StatusPedido.PENDENTE, feira = feira)
        val produto = Produto(id = produtoId)
        val oferta = OfertaEstoque(id = ofertaId)
        val item = ItemPedido(pedido = pedido, produto = produto, ofertaEstoque = oferta, quantidade = BigDecimal.ONE)

        `when`(repository.findById(pedidoId)).thenReturn(Optional.of(pedido))
        `when`(itemPedidoRepository.findByPedidoId(pedidoId)).thenReturn(listOf(item))
        `when`(repository.save(any(Pedido::class.java))).thenAnswer { it.arguments[0] }

        pedidoService.atualizarStatus(pedidoId, StatusPedido.CANCELADO)

        verify(reservaEstoqueService).liberar(ofertaId, BigDecimal.ONE)
    }

    @Test
    fun `deletar deve chamar repository delete`() {
        val pedido = Pedido(id = pedidoId)
        `when`(repository.findById(pedidoId)).thenReturn(Optional.of(pedido))

        pedidoService.deletar(pedidoId)

        verify(repository).delete(pedido)
    }
}
