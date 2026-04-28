package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.RateioResultado
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RateioResultadoRepository : JpaRepository<RateioResultado, UUID> {
    fun findByFeiraId(feiraId: UUID): List<RateioResultado>
}
