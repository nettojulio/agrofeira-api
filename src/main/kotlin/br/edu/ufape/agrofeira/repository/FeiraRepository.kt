package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.Feira
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface FeiraRepository : JpaRepository<Feira, UUID>
