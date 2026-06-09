package br.edu.ufape.agrofeira.domain.entity

import br.edu.ufape.agrofeira.domain.enums.StatusPagamento
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "repasses")
data class Repasse(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne
    @JoinColumn(name = "rateio_resultado_id", nullable = false)
    val rateioResultado: RateioResultado = RateioResultado(),
    @ManyToOne
    @JoinColumn(name = "comerciante_id", nullable = false)
    val comerciante: Usuario = Usuario(),
    @Column(name = "valor_bruto", nullable = false)
    val valorBruto: BigDecimal = BigDecimal.ZERO,
    @Column(name = "taxa_associacao", nullable = false)
    val taxaAssociacao: BigDecimal = BigDecimal.ZERO,
    @Column(name = "valor_liquido", nullable = false)
    val valorLiquido: BigDecimal = BigDecimal.ZERO,
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    val status: StatusPagamento = StatusPagamento.PENDENTE,
    @Column(name = "repassado_em")
    val repassadoEm: LocalDateTime? = null,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
)
