package br.edu.ufape.agrofeira.domain.entity

import br.edu.ufape.agrofeira.domain.enums.StatusPedido
import br.edu.ufape.agrofeira.domain.enums.TipoRetirada
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "pedidos")
@SQLDelete(sql = "UPDATE pedidos SET deletado_em = NOW() WHERE id = ? AND versao = ?")
@SQLRestriction("deletado_em IS NULL")
data class Pedido(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Version
    val versao: Long = 0,
    @ManyToOne
    @JoinColumn(name = "feira_id", nullable = false)
    val feira: Feira = Feira(),
    @ManyToOne
    @JoinColumn(name = "consumidor_id", nullable = false)
    val consumidor: Usuario = Usuario(),
    @OneToMany(mappedBy = "pedido", cascade = [CascadeType.ALL], orphanRemoval = true)
    val itens: MutableList<ItemPedido> = mutableListOf(),
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    val status: StatusPedido = StatusPedido.PENDENTE,
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_retirada", nullable = false)
    val tipoRetirada: TipoRetirada = TipoRetirada.LOCAL,
    @Column(name = "taxa_entrega", nullable = false)
    val taxaEntrega: BigDecimal = BigDecimal.ZERO,
    @Column(name = "valor_produtos", nullable = false)
    val valorProdutos: BigDecimal = BigDecimal.ZERO,
    @Column(name = "valor_total", nullable = false)
    val valorTotal: BigDecimal = BigDecimal.ZERO,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "deletado_em")
    val deletadoEm: LocalDateTime? = null,
)
