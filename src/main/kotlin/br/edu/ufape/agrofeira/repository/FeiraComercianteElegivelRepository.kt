package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.FeiraComercianteElegivel
import br.edu.ufape.agrofeira.domain.entity.FeiraComercianteId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FeiraComercianteElegivelRepository : JpaRepository<FeiraComercianteElegivel, FeiraComercianteId> {
    fun findByFeiraId(feiraId: UUID): List<FeiraComercianteElegivel>

    fun deleteByFeiraId(feiraId: UUID)
}
