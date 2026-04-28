package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.Produto
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ProdutoRepository : JpaRepository<Produto, UUID>
