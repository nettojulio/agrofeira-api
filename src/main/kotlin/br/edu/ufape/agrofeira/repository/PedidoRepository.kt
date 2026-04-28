package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.Pedido
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PedidoRepository : JpaRepository<Pedido, UUID> {
    fun findByFeiraId(feiraId: UUID): List<Pedido>
}
