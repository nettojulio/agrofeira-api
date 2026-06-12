package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.Repasse
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RepasseRepository : JpaRepository<Repasse, UUID> {
    fun findByComercianteId(comercianteId: UUID): List<Repasse>

    fun findByRateioResultadoFeiraId(feiraId: UUID): List<Repasse>
}
