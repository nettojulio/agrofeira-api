package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.FilaRateio
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface FilaRateioRepository : JpaRepository<FilaRateio, UUID> {
    fun findByComercianteIdAndProdutoIdAndCompensadoFalseOrderByCriadoEmAsc(
        comercianteId: UUID,
        produtoId: UUID,
    ): List<FilaRateio>
}
