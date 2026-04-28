package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Pagamento
import br.edu.ufape.agrofeira.domain.entity.Pedido
import br.edu.ufape.agrofeira.domain.enums.StatusPagamento
import br.edu.ufape.agrofeira.dto.request.PagamentoRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.PagamentoRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class PagamentoServiceTest {
    @Mock
    lateinit var repository: PagamentoRepository

    @Mock
    lateinit var pedidoService: PedidoService

    @InjectMocks
    lateinit var service: PagamentoService

    private val id = UUID.randomUUID()
    private val pedidoId = UUID.randomUUID()
    private lateinit var pagamento: Pagamento
    private lateinit var pedido: Pedido

    @BeforeEach
    fun setUp() {
        pedido = Pedido(id = pedidoId)
        pagamento =
            Pagamento(
                id = id,
                pedido = pedido,
                valor = BigDecimal("100.00"),
                metodo = "PIX",
                status = StatusPagamento.PENDENTE,
            )
    }

    @Test
    fun `buscarPorId deve retornar pagamento se existir`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(pagamento))

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
    fun `listarPorPedido deve retornar lista`() {
        `when`(repository.findByPedidoId(pedidoId)).thenReturn(listOf(pagamento))

        val result = service.listarPorPedido(pedidoId)

        assertEquals(1, result.size)
        assertEquals(id, result[0].id)
    }

    @Test
    fun `registrar deve salvar novo pagamento`() {
        val request = PagamentoRequest(pedidoId, BigDecimal("100.00"), "PIX", StatusPagamento.PAGO)
        `when`(pedidoService.buscarPorId(pedidoId)).thenReturn(pedido)
        `when`(repository.save(any(Pagamento::class.java))).thenAnswer { it.arguments[0] }

        val result = service.registrar(request)

        assertNotNull(result)
        assertEquals(BigDecimal("100.00"), result.valor)
        assertEquals(StatusPagamento.PAGO, result.status)
        assertNotNull(result.pagoEm)
    }

    @Test
    fun `deletar deve chamar repository delete`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(pagamento))

        service.deletar(id)

        verify(repository).delete(pagamento)
    }
}
