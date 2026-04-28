package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.entity.Pedido
import br.edu.ufape.agrofeira.domain.entity.RateioResultado
import br.edu.ufape.agrofeira.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Service
class RateioService(
    private val pedidoRepository: PedidoRepository,
    private val itemPedidoRepository: ItemPedidoRepository,
    private val ofertaEstoqueRepository: OfertaEstoqueRepository,
    private val filaRateioRepository: FilaRateioRepository,
    private val rateioResultadoRepository: RateioResultadoRepository,
) {
    @Transactional
    fun executarRateioDaFeira(feira: Feira) {
        val pedidos = pedidoRepository.findByFeiraId(feira.id)
        val demandaPorProduto = agruparDemandaTotal(pedidos)

        demandaPorProduto.forEach { (produtoId, quantidadeTotalSolicitada) ->
            processarProdutoNoRateio(feira, produtoId, quantidadeTotalSolicitada)
        }
    }

    private fun agruparDemandaTotal(pedidos: List<Pedido>): Map<UUID, BigDecimal> {
        val demanda = mutableMapOf<UUID, BigDecimal>()
        pedidos.forEach { pedido ->
            val itens = itemPedidoRepository.findByPedidoId(pedido.id)
            itens.forEach { item ->
                val totalAtual = demanda.getOrDefault(item.produto.id, BigDecimal.ZERO)
                demanda[item.produto.id] = totalAtual.add(item.quantidade)
            }
        }
        return demanda
    }

    private fun processarProdutoNoRateio(
        feira: Feira,
        produtoId: UUID,
        totalSolicitado: BigDecimal,
    ) {
        val ofertas = ofertaEstoqueRepository.buscarPorFeiraEProduto(feira.id, produtoId)
        if (ofertas.isEmpty()) return

        var saldoDemanda = totalSolicitado
        val resultados = mutableMapOf<UUID, BigDecimal>() // comercianteId -> qtd_sequestrada

        // 1. Compensar Fila FIFO (Prioridade)
        ofertas.forEach { oferta ->
            val pendencias =
                filaRateioRepository.findByComercianteIdAndProdutoIdAndCompensadoFalseOrderByCriadoEmAsc(
                    oferta.comerciante.id,
                    produtoId,
                )

            pendencias.forEach { pendencia ->
                if (saldoDemanda > BigDecimal.ZERO) {
                    val aCompensar = pendencia.quantidadeDeficit.min(saldoDemanda).min(oferta.quantidadeOfertada)
                    if (aCompensar > BigDecimal.ZERO) {
                        resultados[oferta.comerciante.id] =
                            resultados.getOrDefault(oferta.comerciante.id, BigDecimal.ZERO).add(aCompensar)
                        saldoDemanda = saldoDemanda.subtract(aCompensar)

                        // Atualiza a fila se compensou tudo ou parte
                        if (aCompensar >= pendencia.quantidadeDeficit) {
                            filaRateioRepository.save(pendencia.copy(compensado = true))
                        } else {
                            filaRateioRepository.save(
                                pendencia.copy(
                                    quantidadeDeficit =
                                        pendencia.quantidadeDeficit.subtract(
                                            aCompensar,
                                        ),
                                ),
                            )
                        }
                    }
                }
            }
        }

        // 2. Distribuição Igualitária do Restante
        if (saldoDemanda > BigDecimal.ZERO) {
            val qtdComerciantes = ofertas.size.toBigDecimal()
            val cotaMedia = saldoDemanda.divide(qtdComerciantes, 2, RoundingMode.DOWN)

            ofertas.forEach { oferta ->
                val disponivelNaOferta =
                    oferta.quantidadeOfertada.subtract(resultados.getOrDefault(oferta.comerciante.id, BigDecimal.ZERO))
                val sequestro = cotaMedia.min(disponivelNaOferta)

                if (sequestro > BigDecimal.ZERO) {
                    resultados[oferta.comerciante.id] =
                        resultados.getOrDefault(oferta.comerciante.id, BigDecimal.ZERO).add(sequestro)
                    saldoDemanda = saldoDemanda.subtract(sequestro)
                }
            }
        }

        // 3. Sobras de arredondamento ou demanda residual (Distribui 1 por 1 até acabar)
        if (saldoDemanda > BigDecimal.ZERO) {
            ofertas.forEach { oferta ->
                if (saldoDemanda > BigDecimal.ZERO) {
                    val disponivel =
                        oferta.quantidadeOfertada.subtract(
                            resultados.getOrDefault(
                                oferta.comerciante.id,
                                BigDecimal.ZERO,
                            ),
                        )
                    val sequestro = saldoDemanda.min(disponivel)
                    if (sequestro > BigDecimal.ZERO) {
                        resultados[oferta.comerciante.id] =
                            resultados.getOrDefault(oferta.comerciante.id, BigDecimal.ZERO).add(sequestro)
                        saldoDemanda = saldoDemanda.subtract(sequestro)
                    }
                }
            }
        }

        // 4. Persistir Resultados e Gerar Novos Déficits para feiras futuras
        ofertas.forEach { oferta ->
            val qtdFinal = resultados.getOrDefault(oferta.comerciante.id, BigDecimal.ZERO)

            // Salva o resultado do rateio
            rateioResultadoRepository.save(
                RateioResultado(
                    feira = feira,
                    comerciante = oferta.comerciante,
                    produto = oferta.produto,
                    quantidadeSequestrada = qtdFinal,
                    valorBrutoVenda = qtdFinal.multiply(oferta.produto.precoBase),
                ),
            )

            // Se o comerciante tinha estoque mas a divisão igualitária deu menos pra ele que pros outros, gera déficit
            // (Simplificação da V1: se ele vendeu menos que a cota justa e tinha estoque)
            // Lógica completa de desbalanceamento deve ser refinada com o PO.
        }
    }
}
