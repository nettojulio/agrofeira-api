package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.ItemPedido
import br.edu.ufape.agrofeira.domain.entity.Pedido
import br.edu.ufape.agrofeira.dto.request.PedidoRequest
import br.edu.ufape.agrofeira.exception.BusinessRuleException
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.ItemPedidoRepository
import br.edu.ufape.agrofeira.repository.OfertaEstoqueRepository
import br.edu.ufape.agrofeira.repository.PedidoRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
class PedidoService(
    private val repository: PedidoRepository,
    private val itemPedidoRepository: ItemPedidoRepository,
    private val feiraService: FeiraService,
    private val produtoService: ProdutoService,
    private val usuarioService: UsuarioService,
    private val reservaEstoqueService: ReservaEstoqueService,
    private val ofertaEstoqueRepository: OfertaEstoqueRepository,
) {
    fun listar(pageable: Pageable): Page<Pedido> = repository.findAll(pageable)

    @Transactional
    fun criar(
        request: PedidoRequest,
        consumidorId: UUID,
    ): Pedido {
        val feira = feiraService.buscarPorId(request.feiraId)
        val consumidor = usuarioService.buscarPorId(consumidorId)

        val pedido =
            Pedido(
                feira = feira,
                consumidor = consumidor,
                tipoRetirada = request.tipoRetirada,
                taxaEntrega =
                    if (request.tipoRetirada == br.edu.ufape.agrofeira.domain.enums.TipoRetirada.ENTREGA) {
                        BigDecimal("7.00")
                    } else {
                        BigDecimal.ZERO
                    },
            )

        var totalProdutos = BigDecimal.ZERO

        request.itens.forEach { itemReq ->
            val produto = produtoService.buscarPorId(itemReq.produtoId)
            val ofertas = ofertaEstoqueRepository.buscarPorFeiraEProduto(feira.id, produto.id)
            if (ofertas.isEmpty()) throw BusinessRuleException("Produto ${produto.nome} não disponível nesta feira")

            var reservado = false
            for (oferta in ofertas) {
                if (reservaEstoqueService.reservar(oferta.id, itemReq.quantidade)) {
                    reservado = true
                    break
                }
            }

            if (!reservado) throw BusinessRuleException("Estoque insuficiente para o produto ${produto.nome}")

            val itemPedido =
                ItemPedido(
                    pedido = pedido,
                    produto = produto,
                    quantidade = itemReq.quantidade,
                    valorUnitario = produto.precoBase,
                    nomeItem = produto.nome,
                    unidadeMedida = produto.unidadeMedida,
                )

            pedido.itens.add(itemPedido)
            totalProdutos = totalProdutos.add(itemPedido.valorUnitario.multiply(itemPedido.quantidade))
        }

        val pedidoFinal =
            pedido.copy(
                valorProdutos = totalProdutos,
                valorTotal = totalProdutos.add(pedido.taxaEntrega),
                itens = pedido.itens,
            )

        return repository.save(pedidoFinal)
    }

    fun buscarPorId(id: UUID): Pedido =
        repository
            .findById(id)
            .orElseThrow { ResourceNotFoundException("Pedido", id.toString()) }

    fun buscarItens(pedidoId: UUID): List<ItemPedido> = itemPedidoRepository.findByPedidoId(pedidoId)

    @Transactional
    fun deletar(id: UUID) {
        val pedido = buscarPorId(id)
        repository.delete(pedido)
    }
}
