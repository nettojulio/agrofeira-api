package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.Pagamento
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface PagamentoRepository : JpaRepository<Pagamento, UUID> {
    @Query("SELECT p FROM Pagamento p WHERE p.pedido.id = :pedidoId")
    fun findByPedidoId(
        @Param("pedidoId") pedidoId: UUID,
    ): List<Pagamento>
}
