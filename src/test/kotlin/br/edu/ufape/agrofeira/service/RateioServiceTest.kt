package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.entity.FilaRateio
import br.edu.ufape.agrofeira.domain.entity.ItemPedido
import br.edu.ufape.agrofeira.domain.entity.OfertaEstoque
import br.edu.ufape.agrofeira.domain.entity.Pedido
import br.edu.ufape.agrofeira.domain.entity.Produto
import br.edu.ufape.agrofeira.domain.entity.RateioResultado
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.repository.FilaRateioRepository
import br.edu.ufape.agrofeira.repository.ItemPedidoRepository
import br.edu.ufape.agrofeira.repository.OfertaEstoqueRepository
import br.edu.ufape.agrofeira.repository.PedidoRepository
import br.edu.ufape.agrofeira.repository.RateioResultadoRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.argThat
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class RateioServiceTest {
    @Mock
    lateinit var pedidoRepository: PedidoRepository

    @Mock
    lateinit var itemPedidoRepository: ItemPedidoRepository

    @Mock
    lateinit var ofertaEstoqueRepository: OfertaEstoqueRepository

    @Mock
    lateinit var filaRateioRepository: FilaRateioRepository

    @Mock
    lateinit var rateioResultadoRepository: RateioResultadoRepository

    @InjectMocks
    lateinit var service: RateioService

    @Test
    fun `executarRateioDaFeira deve processar demanda e salvar resultados`() {
        val feira = Feira(id = UUID.randomUUID())
        val produtoId = UUID.randomUUID()
        val produto = Produto(id = produtoId, nome = "Alface", precoBase = BigDecimal("2.00"))

        val pedido = Pedido(id = UUID.randomUUID(), feira = feira)
        val itemPedido = ItemPedido(pedido = pedido, produto = produto, quantidade = BigDecimal("10.00"))

        val comerciante = Usuario(id = UUID.randomUUID(), nome = "Vendedor")
        val oferta =
            OfertaEstoque(
                id = UUID.randomUUID(),
                feira = feira,
                comerciante = comerciante,
                produto = produto,
                quantidadeOfertada = BigDecimal("20.00"),
            )

        `when`(pedidoRepository.findByFeiraId(feira.id)).thenReturn(listOf(pedido))
        `when`(itemPedidoRepository.findByPedidoId(pedido.id)).thenReturn(listOf(itemPedido))
        `when`(ofertaEstoqueRepository.buscarPorFeiraEProduto(feira.id, produtoId)).thenReturn(listOf(oferta))
        `when`(
            filaRateioRepository.findByComercianteIdAndProdutoIdAndCompensadoFalseOrderByCriadoEmAsc(
                comerciante.id,
                produtoId,
            ),
        ).thenReturn(emptyList())

        service.executarRateioDaFeira(feira)

        verify(rateioResultadoRepository).save(any(RateioResultado::class.java))
    }

    @Test
    fun `executarRateioDaFeira deve compensar fila FIFO prioritariamente`() {
        val feira = Feira(id = UUID.randomUUID())
        val produtoId = UUID.randomUUID()
        val produto = Produto(id = produtoId, nome = "Tomate", precoBase = BigDecimal("5.00"))

        val pedido = Pedido(id = UUID.randomUUID(), feira = feira)
        val itemPedido = ItemPedido(pedido = pedido, produto = produto, quantidade = BigDecimal("10.00"))

        val comerciante = Usuario(id = UUID.randomUUID(), nome = "Vendedor")
        val oferta =
            OfertaEstoque(
                id = UUID.randomUUID(),
                feira = feira,
                comerciante = comerciante,
                produto = produto,
                quantidadeOfertada = BigDecimal("20.00"),
            )

        val pendencia = FilaRateio(comerciante = comerciante, produto = produto, quantidadeDeficit = BigDecimal("5.00"))

        `when`(pedidoRepository.findByFeiraId(feira.id)).thenReturn(listOf(pedido))
        `when`(itemPedidoRepository.findByPedidoId(pedido.id)).thenReturn(listOf(itemPedido))
        `when`(ofertaEstoqueRepository.buscarPorFeiraEProduto(feira.id, produtoId)).thenReturn(listOf(oferta))
        `when`(
            filaRateioRepository.findByComercianteIdAndProdutoIdAndCompensadoFalseOrderByCriadoEmAsc(
                comerciante.id,
                produtoId,
            ),
        ).thenReturn(listOf(pendencia))

        service.executarRateioDaFeira(feira)

        // Verificamos se salvou a pendencia como compensada
        verify(filaRateioRepository).save(argThat { it.compensado })
        // Verificamos se o resultado final foi salvo (10 sequestrados: 5 da pendencia + 5 da divisao)
        verify(rateioResultadoRepository).save(
            argThat {
                it.quantidadeSequestrada.stripTrailingZeros() ==
                    BigDecimal("10.00").stripTrailingZeros()
            },
        )
    }
}
