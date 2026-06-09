package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.FeiraProdutoElegivel
import br.edu.ufape.agrofeira.domain.entity.FeiraProdutoId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FeiraProdutoElegivelRepository : JpaRepository<FeiraProdutoElegivel, FeiraProdutoId> {
    fun findByFeiraId(feiraId: UUID): List<FeiraProdutoElegivel>

    fun deleteByFeiraId(feiraId: UUID)
}
