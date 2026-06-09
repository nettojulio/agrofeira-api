package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.ItemPedido
import br.edu.ufape.agrofeira.domain.entity.Pedido
import br.edu.ufape.agrofeira.domain.enums.StatusPedido
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
import java.time.LocalDateTime
import java.util.UUID

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

            // Ordena pelo menor estoque reservado: quem vendeu menos vai primeiro (rotatividade)
            val ofertas =
                ofertaEstoqueRepository
                    .buscarPorFeiraEProduto(feira.id, produto.id)
                    .sortedBy { it.quantidadeReservada }

            if (ofertas.isEmpty()) throw BusinessRuleException("Produto ${produto.nome} não disponível nesta feira")

            var quantidadeRestante = itemReq.quantidade

            for (oferta in ofertas) {
                if (quantidadeRestante <= BigDecimal.ZERO) break

                val disponivel = oferta.quantidadeOfertada - oferta.quantidadeReservada
                if (disponivel <= BigDecimal.ZERO) continue

                // Pega o máximo disponível neste vendedor, até o que ainda falta
                val quantidadeDestaOferta = quantidadeRestante.min(disponivel)

                if (reservaEstoqueService.reservar(oferta.id, quantidadeDestaOferta)) {
                    pedido.itens.add(
                        ItemPedido(
                            pedido = pedido,
                            produto = produto,
                            ofertaEstoque = oferta,
                            quantidade = quantidadeDestaOferta,
                            valorUnitario = produto.precoBase,
                            nomeItem = produto.nome,
                            unidadeMedida = produto.unidadeMedida,
                        ),
                    )
                    totalProdutos = totalProdutos.add(produto.precoBase.multiply(quantidadeDestaOferta))
                    quantidadeRestante = quantidadeRestante.subtract(quantidadeDestaOferta)
                }
            }

            if (quantidadeRestante > BigDecimal.ZERO) {
                throw BusinessRuleException("Estoque insuficiente para o produto ${produto.nome}")
            }
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

    fun buscarPorFeira(feiraId: UUID): List<Pedido> {
        feiraService.buscarPorId(feiraId)
        return repository.findByFeiraId(feiraId)
    }

    fun buscarItens(pedidoId: UUID): List<ItemPedido> = itemPedidoRepository.findByPedidoId(pedidoId)

    @Transactional
    fun atualizarStatus(
        id: UUID,
        novoStatus: StatusPedido,
    ): Pedido {
        val pedido = buscarPorId(id)
        validarTransicao(pedido.status, novoStatus)

        if (novoStatus == StatusPedido.CANCELADO) {
            liberarReservasDoPedido(pedido)
        }

        val atualizado = pedido.copy(status = novoStatus, atualizadoEm = LocalDateTime.now())
        return repository.save(atualizado)
    }

    private fun validarTransicao(
        atual: StatusPedido,
        novo: StatusPedido,
    ) {
        if (atual == novo) {
            throw BusinessRuleException("O pedido já está no status $atual")
        }
        val permitidas =
            when (atual) {
                StatusPedido.PENDENTE -> {
                    setOf(StatusPedido.AGUARDANDO_SEPARACAO, StatusPedido.CANCELADO)
                }

                StatusPedido.AGUARDANDO_SEPARACAO -> {
                    setOf(StatusPedido.PRONTO_RETIRADA, StatusPedido.CANCELADO)
                }

                StatusPedido.PRONTO_RETIRADA -> {
                    setOf(StatusPedido.SAIU_ENTREGA, StatusPedido.ENTREGUE, StatusPedido.CANCELADO)
                }

                StatusPedido.SAIU_ENTREGA -> {
                    setOf(StatusPedido.ENTREGUE)
                }

                StatusPedido.ENTREGUE, StatusPedido.CANCELADO -> {
                    emptySet()
                }
            }
        if (novo !in permitidas) {
            throw BusinessRuleException("Transição de status não permitida: $atual → $novo")
        }
    }

    private fun liberarReservasDoPedido(pedido: Pedido) {
        val itens = itemPedidoRepository.findByPedidoId(pedido.id)
        itens.forEach { item ->
            val ofertaEstoque = item.ofertaEstoque
            if (ofertaEstoque != null) {
                reservaEstoqueService.liberar(ofertaEstoque.id, item.quantidade)
            } else {
                // fallback para pedidos criados antes da migration V2
                val ofertas = ofertaEstoqueRepository.buscarPorFeiraEProduto(pedido.feira.id, item.produto.id)
                for (oferta in ofertas) {
                    if (reservaEstoqueService.liberar(oferta.id, item.quantidade)) break
                }
            }
        }
    }

    @Transactional
    fun deletar(id: UUID) {
        val pedido = buscarPorId(id)
        repository.delete(pedido)
    }
}
