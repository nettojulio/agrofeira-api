package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Repasse
import br.edu.ufape.agrofeira.domain.enums.StatusPagamento
import br.edu.ufape.agrofeira.dto.request.RepasseRequest
import br.edu.ufape.agrofeira.dto.response.FaturamentoMensalDTO
import br.edu.ufape.agrofeira.dto.response.RepasseTotaisDTO
import br.edu.ufape.agrofeira.exception.BusinessRuleException
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.RateioResultadoRepository
import br.edu.ufape.agrofeira.repository.RepasseRepository
import br.edu.ufape.agrofeira.dto.mapper.toDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class RepasseService(
    private val repository: RepasseRepository,
    private val rateioResultadoRepository: RateioResultadoRepository,
    private val comercianteService: ComercianteService,
    private val feiraService: FeiraService,
) {
    fun listarTodos(pageable: Pageable): Page<Repasse> = repository.findAll(pageable)

    fun listarPorComerciante(comercianteId: UUID): List<Repasse> {
        comercianteService.buscarPorId(comercianteId)
        return repository.findByComercianteId(comercianteId)
    }

    fun listarPorFeira(feiraId: UUID): List<Repasse> {
        feiraService.buscarPorId(feiraId)
        return repository.findByRateioResultadoFeiraId(feiraId)
    }

    fun listarTotaisPorFeira(feiraId: UUID): List<RepasseTotaisDTO> {
        feiraService.buscarPorId(feiraId)
        val repasses = repository.findByRateioResultadoFeiraId(feiraId)
        return repasses
            .groupBy { it.comerciante.id }
            .map { (_, lista) ->
                RepasseTotaisDTO(
                    comerciante = lista.first().comerciante.toDTO(),
                    totalBruto = lista.sumOf { it.valorBruto },
                    totalLiquido = lista.sumOf { it.valorLiquido },
                    quantidadeRepasses = lista.size,
                )
            }
    }

    fun relatorioMensal(ano: Int): List<FaturamentoMensalDTO> {
        val mesesLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
                                 "Jul", "Ago", "Set", "Out", "Nov", "Dez")
        return repository.findAll()
            .filter { r ->
                val dataRef = r.repassadoEm ?: r.criadoEm
                dataRef.year == ano
            }
            .groupBy { r ->
                val dataRef = r.repassadoEm ?: r.criadoEm
                dataRef.monthValue
            }
            .map { (mes, lista) ->
                FaturamentoMensalDTO(
                    ano = ano,
                    mes = mes,
                    mesLabel = mesesLabels[mes - 1],
                    totalBruto = lista.sumOf { it.valorBruto },
                    totalLiquido = lista.sumOf { it.valorLiquido },
                    quantidadeRepasses = lista.size,
                )
            }
            .sortedBy { it.mes }
    }

    fun relatorioGeralPorComerciante(): List<RepasseTotaisDTO> =
        repository.findAll()
            .groupBy { it.comerciante.id }
            .map { (_, lista) ->
                RepasseTotaisDTO(
                    comerciante = lista.first().comerciante.toDTO(),
                    totalBruto = lista.sumOf { it.valorBruto },
                    totalLiquido = lista.sumOf { it.valorLiquido },
                    quantidadeRepasses = lista.size,
                )
            }
            .sortedByDescending { it.totalBruto }

    @Transactional
    fun registrar(request: RepasseRequest): Repasse {
        val rateioResultado =
            rateioResultadoRepository
                .findById(request.rateioResultadoId)
                .orElseThrow { ResourceNotFoundException("RateioResultado", request.rateioResultadoId.toString()) }

        if (repository.findByComercianteId(rateioResultado.comerciante.id)
                .any { it.rateioResultado.id == rateioResultado.id }
        ) {
            throw BusinessRuleException(
                "Já existe um repasse registrado para este resultado de rateio",
                "REPASSE_DUPLICADO",
            )
        }

        return repository.save(
            Repasse(
                rateioResultado = rateioResultado,
                comerciante = rateioResultado.comerciante,
                valorBruto = rateioResultado.valorBrutoVenda,
                taxaAssociacao = BigDecimal.ZERO,
                valorLiquido = rateioResultado.valorBrutoVenda,
                status = StatusPagamento.PAGO,
                repassadoEm = LocalDateTime.now(),
            ),
        )
    }
}
