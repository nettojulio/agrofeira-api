package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.ItemPedido
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ItemPedidoRepository : JpaRepository<ItemPedido, UUID> {
    @Query("SELECT i FROM ItemPedido i WHERE i.pedido.id = :pedidoId")
    fun findByPedidoId(
        @Param("pedidoId") pedidoId: UUID,
    ): List<ItemPedido>
}
