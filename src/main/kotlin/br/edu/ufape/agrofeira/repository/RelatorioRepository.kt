package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.Relatorio
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RelatorioRepository : JpaRepository<Relatorio, UUID>
