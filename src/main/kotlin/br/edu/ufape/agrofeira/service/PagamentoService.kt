package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Pagamento
import br.edu.ufape.agrofeira.domain.enums.StatusPagamento
import br.edu.ufape.agrofeira.dto.request.PagamentoRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.PagamentoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class PagamentoService(
    private val repository: PagamentoRepository,
    private val pedidoService: PedidoService,
) {
    fun buscarPorId(id: UUID): Pagamento =
        repository
            .findById(id)
            .orElseThrow { ResourceNotFoundException("Pagamento", id.toString()) }

    fun listarPorPedido(pedidoId: UUID): List<Pagamento> = repository.findByPedidoId(pedidoId)

    @Transactional
    fun registrar(request: PagamentoRequest): Pagamento {
        val pedido = pedidoService.buscarPorId(request.pedidoId)

        val pagamento =
            Pagamento(
                pedido = pedido,
                valor = request.valor,
                metodo = request.metodo,
                status = request.status,
                pagoEm = if (request.status == StatusPagamento.PAGO) LocalDateTime.now() else null,
            )

        return repository.save(pagamento)
    }

    @Transactional
    fun deletar(id: UUID) {
        val pagamento = buscarPorId(id)
        repository.delete(pagamento)
    }
}
