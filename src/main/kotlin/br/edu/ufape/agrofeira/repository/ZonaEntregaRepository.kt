package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.ZonaEntrega
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ZonaEntregaRepository : JpaRepository<ZonaEntrega, UUID>
