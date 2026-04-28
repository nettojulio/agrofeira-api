package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.Repasse
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RepasseRepository : JpaRepository<Repasse, UUID>
