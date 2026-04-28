package br.edu.ufape.agrofeira.domain.entity

import br.edu.ufape.agrofeira.domain.enums.UnidadeMedida
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

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
