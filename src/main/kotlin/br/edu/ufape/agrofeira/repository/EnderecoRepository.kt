package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.Endereco
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EnderecoRepository : JpaRepository<Endereco, UUID>
