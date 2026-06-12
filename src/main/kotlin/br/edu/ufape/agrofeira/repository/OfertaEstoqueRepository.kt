package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.OfertaEstoque
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal
import java.util.UUID

interface OfertaEstoqueRepository : JpaRepository<OfertaEstoque, UUID> {
    fun findByFeiraId(feiraId: UUID): List<OfertaEstoque>

    fun findByFeiraIdAndComercianteId(
        feiraId: UUID,
        comercianteId: UUID,
    ): List<OfertaEstoque>

    fun existsByFeiraIdAndComercianteIdAndProdutoId(
        feiraId: UUID,
        comercianteId: UUID,
        produtoId: UUID,
    ): Boolean

    @Query("SELECT o FROM OfertaEstoque o WHERE o.feira.id = ?1 AND o.produto.id = ?2")
    fun buscarPorFeiraEProduto(
        feiraId: UUID,
        produtoId: UUID,
    ): List<OfertaEstoque>

    @Modifying
    @Query(
        value = """
        UPDATE ofertas_estoque 
        SET quantidade_reservada = quantidade_reservada + :quantidade 
        WHERE id = :ofertaId 
        AND (quantidade_ofertada - quantidade_reservada) >= :quantidade
    """,
        nativeQuery = true,
    )
    fun reservarEstoqueAtomicamente(
        ofertaId: UUID,
        quantidade: BigDecimal,
    ): Int

    @Modifying
    @Query(
        value = """
        UPDATE ofertas_estoque 
        SET quantidade_reservada = quantidade_reservada - :quantidade 
        WHERE id = :ofertaId 
        AND quantidade_reservada >= :quantidade
    """,
        nativeQuery = true,
    )
    fun liberarReservaAtomicamente(
        ofertaId: UUID,
        quantidade: BigDecimal,
    ): Int
}
