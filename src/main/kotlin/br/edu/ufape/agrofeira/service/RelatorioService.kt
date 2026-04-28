package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Relatorio
import br.edu.ufape.agrofeira.domain.enums.TipoRelatorio
import br.edu.ufape.agrofeira.dto.request.RelatorioRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.RelatorioRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class RelatorioService(
    private val repository: RelatorioRepository,
) {
    fun listar(pageable: Pageable): Page<Relatorio> = repository.findAll(pageable)

    fun buscarPorId(id: UUID): Relatorio =
        repository
            .findById(id)
            .orElseThrow { ResourceNotFoundException("Relatório", id.toString()) }

    @Transactional
    fun criar(request: RelatorioRequest): Relatorio {
        // Aqui poderia haver lógica para gerar o conteúdo real baseado no tipo
        val relatorio =
            Relatorio(
                titulo = request.titulo,
                tipo = request.tipo,
                conteudo = request.conteudo ?: "Conteúdo gerado automaticamente em ${LocalDateTime.now()}",
            )
        return repository.save(relatorio)
    }

    @Transactional
    fun atualizar(
        id: UUID,
        request: RelatorioRequest,
    ): Relatorio {
        val relatorio = buscarPorId(id)
        val relatorioAtualizado =
            relatorio.copy(
                titulo = request.titulo,
                tipo = request.tipo,
                conteudo = request.conteudo ?: relatorio.conteudo,
                atualizadoEm = LocalDateTime.now(),
            )
        return repository.save(relatorioAtualizado)
    }

    @Transactional
    fun deletar(id: UUID) {
        val relatorio = buscarPorId(id)
        repository.delete(relatorio)
    }
}
