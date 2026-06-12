package br.edu.ufape.agrofeira.domain.entity

import br.edu.ufape.agrofeira.domain.enums.UnidadeMedida
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "itens_pedido")
data class ItemPedido(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    val pedido: Pedido = Pedido(),
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    val produto: Produto = Produto(),
    @ManyToOne
    @JoinColumn(name = "oferta_estoque_id", nullable = true)
    val ofertaEstoque: OfertaEstoque? = null,
    @Column(nullable = false)
    val quantidade: BigDecimal = BigDecimal.ZERO,
    @Column(name = "valor_unitario", nullable = false)
    val valorUnitario: BigDecimal = BigDecimal.ZERO,
    @Column(name = "nome_item", nullable = false)
    val nomeItem: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", nullable = false)
    val unidadeMedida: UnidadeMedida = UnidadeMedida.UNIDADE,
)
