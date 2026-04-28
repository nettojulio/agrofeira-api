package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.enums.StatusFeira
import br.edu.ufape.agrofeira.dto.request.FeiraRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.FeiraRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class FeiraService(
    private val repository: FeiraRepository,
    private val rateioService: RateioService,
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
}
