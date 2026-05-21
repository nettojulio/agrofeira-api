package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.enums.StatusFeira
import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.FeiraRequest
import br.edu.ufape.agrofeira.dto.response.FeiraComercianteDTO
import br.edu.ufape.agrofeira.dto.response.FeiraDetalhesDTO
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.FeiraRepository
import br.edu.ufape.agrofeira.repository.OfertaEstoqueRepository
import br.edu.ufape.agrofeira.repository.PedidoRepository
import br.edu.ufape.agrofeira.repository.RateioResultadoRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class FeiraService(
    private val repository: FeiraRepository,
    private val rateioService: RateioService,
    private val pedidoRepository: PedidoRepository,
    private val ofertaEstoqueRepository: OfertaEstoqueRepository,
    private val rateioResultadoRepository: RateioResultadoRepository,
    private val comercianteService: ComercianteService,
) {
    fun listar(pageable: Pageable): Page<Feira> = repository.findAll(pageable)

    fun buscarPorId(id: UUID): Feira =
        repository
            .findById(id)
            .orElseThrow { ResourceNotFoundException("Feira", id.toString()) }

    @Transactional
    fun criar(request: FeiraRequest): Feira {
        val feira =
            Feira(
                dataHora = request.dataHora,
                status = request.status,
            )
        return repository.save(feira)
    }

    @Transactional
    fun atualizar(
        id: UUID,
        request: FeiraRequest,
    ): Feira {
        val feira = buscarPorId(id)
        val feiraAtualizada =
            feira.copy(
                dataHora = request.dataHora,
                status = request.status,
                atualizadoEm = LocalDateTime.now(),
            )
        return repository.save(feiraAtualizada)
    }

    @Transactional
    fun atualizarStatus(
        id: UUID,
        novoStatus: StatusFeira,
    ): Feira {
        val feira = buscarPorId(id)

        // RF 0008: Executar o rateio automaticamente ao encerrar a feira para pedidos
        if (novoStatus == StatusFeira.ENCERRADA && feira.status != StatusFeira.ENCERRADA) {
            rateioService.executarRateioDaFeira(feira)
        }

        val feiraAtualizada = feira.copy(status = novoStatus)
        return repository.save(feiraAtualizada)
    }

    @Transactional
    fun deletar(id: UUID) {
        val feira = buscarPorId(id)
        repository.delete(feira)
    }

    fun detalharFeira(id: UUID): FeiraDetalhesDTO {
        val feira = buscarPorId(id)
        val pedidos = pedidoRepository.findByFeiraId(id)
        val ofertas = ofertaEstoqueRepository.findByFeiraId(id)
        val rateios = rateioResultadoRepository.findByFeiraId(id)

        return FeiraDetalhesDTO(
            id = feira.id,
            dataHora = feira.dataHora,
            status = feira.status.name,
            totalPedidos = pedidos.size,
            totalComerciantes = ofertas.map { it.comerciante.id }.distinct().size,
            totalProdutos = ofertas.map { it.produto.id }.distinct().size,
            valorTotalPedidos = pedidos.sumOf { it.valorTotal },
            valorTotalProdutos = pedidos.sumOf { it.valorProdutos },
            totalTaxasEntrega = pedidos.sumOf { it.taxaEntrega },
            pedidosPorStatus = pedidos.groupingBy { it.status.name }.eachCount(),
            totalRateado = rateios.sumOf { it.valorBrutoVenda },
        )
    }

    fun buscarParticipacaoComerciante(
        feiraId: UUID,
        comercianteId: UUID,
    ): FeiraComercianteDTO {
        buscarPorId(feiraId)
        val comerciante = comercianteService.buscarPorId(comercianteId)
        val ofertas = ofertaEstoqueRepository.findByFeiraIdAndComercianteId(feiraId, comercianteId)
        val rateios = rateioResultadoRepository.findByFeiraIdAndComercianteId(feiraId, comercianteId)

        return FeiraComercianteDTO(
            comerciante = comerciante.toDTO(),
            ofertas = ofertas.map { it.toDTO() },
            rateioResultados = rateios.map { it.toDTO() },
            totalOfertado = ofertas.sumOf { it.quantidadeOfertada },
            totalSequestrado = rateios.sumOf { it.quantidadeSequestrada },
            totalBrutoVenda = rateios.sumOf { it.valorBrutoVenda },
        )
    }
}
