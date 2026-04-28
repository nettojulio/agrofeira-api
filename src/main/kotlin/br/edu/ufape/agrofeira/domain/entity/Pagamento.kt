package br.edu.ufape.agrofeira.domain.entity

import br.edu.ufape.agrofeira.domain.enums.StatusPagamento
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "pagamentos")
@SQLDelete(sql = "UPDATE pagamentos SET deletado_em = NOW(), ativo = false WHERE id = ? AND versao = ?")
@SQLRestriction("deletado_em IS NULL")
data class Pagamento(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Version
    val versao: Long = 0,
    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    val pedido: Pedido = Pedido(),
    @Column(nullable = false)
    val valor: BigDecimal = BigDecimal.ZERO,
    val metodo: String? = null,
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    val status: StatusPagamento = StatusPagamento.PENDENTE,
    @Column(name = "pago_em")
    val pagoEm: LocalDateTime? = null,
    val ativo: Boolean = true,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "deletado_em")
    val deletadoEm: LocalDateTime? = null,
)
