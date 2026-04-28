package br.edu.ufape.agrofeira.config

import br.edu.ufape.agrofeira.domain.enums.StatusPedido
import br.edu.ufape.agrofeira.repository.OfertaEstoqueRepository
import br.edu.ufape.agrofeira.repository.PedidoRepository
import br.edu.ufape.agrofeira.service.ReservaEstoqueService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Configuration
@EnableScheduling
@ConditionalOnProperty(value = ["app.scheduling.enabled"], havingValue = "true", matchIfMissing = true)
class SchedulingConfig

@Component
@ConditionalOnProperty(value = ["app.scheduling.enabled"], havingValue = "true", matchIfMissing = true)
class ReservationReleaseScheduler(
    private val pedidoRepository: PedidoRepository,
    private val reservaEstoqueService: ReservaEstoqueService,
    private val ofertaEstoqueRepository: OfertaEstoqueRepository,
) {
    private val logger = LoggerFactory.getLogger(ReservationReleaseScheduler::class.java)
    private val TTL_MINUTES = 15L

    @Scheduled(fixedDelay = 60000) // Every 1 minute
    @Transactional
    fun releaseExpiredReservations() {
        // Encontra todos os pedidos pendentes
        val pedidosPendentes = pedidoRepository.findAll().filter { it.status == StatusPedido.PENDENTE }
        val expirationTime = LocalDateTime.now().minusMinutes(TTL_MINUTES)

        val expiredPedidos = pedidosPendentes.filter { it.criadoEm.isBefore(expirationTime) }

        if (expiredPedidos.isNotEmpty()) {
            logger.info("Encontrados ${expiredPedidos.size} pedidos pendentes expirados. Liberando reservas...")

            expiredPedidos.forEach { pedido ->
                val itens =
                    ApplicationContextProvider
                        .getBean<br.edu.ufape.agrofeira.repository.ItemPedidoRepository>()
                        .findByPedidoId(pedido.id)

                itens.forEach { itemPedido ->
                    // Procura as ofertas da feira e produto para liberar a quantidade reservada
                    val ofertas =
                        ofertaEstoqueRepository.buscarPorFeiraEProduto(pedido.feira.id, itemPedido.produto.id)
                    var liberado = false
                    for (oferta in ofertas) {
                        if (oferta.quantidadeReservada >= itemPedido.quantidade) {
                            if (reservaEstoqueService.liberar(oferta.id, itemPedido.quantidade)) {
                                liberado = true
                                break
                            }
                        }
                    }
                    if (!liberado) {
                        logger.warn("Nenhuma oferta com reserva suficiente encontrada para liberar o itemPedido ${itemPedido.id}.")
                    }
                }
                pedido.atualizadoEm = LocalDateTime.now()
                pedidoRepository.save(pedido.copy(status = StatusPedido.CANCELADO))
                logger.info("Pedido ${pedido.id} cancelado por expiração de reserva.")
            }
        }
    }
}
