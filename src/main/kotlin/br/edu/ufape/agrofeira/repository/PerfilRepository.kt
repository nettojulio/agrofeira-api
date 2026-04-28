package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.Perfil
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PerfilRepository : JpaRepository<Perfil, Int> {
    fun findByNome(nome: String): Optional<Perfil>
}
