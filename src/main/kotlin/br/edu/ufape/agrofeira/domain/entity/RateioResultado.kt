package br.edu.ufape.agrofeira.domain.entity

import br.edu.ufape.agrofeira.domain.enums.StatusProcessamento
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "rateios_resultado")
data class RateioResultado(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne
    @JoinColumn(name = "feira_id", nullable = false)
    val feira: Feira = Feira(),
    @ManyToOne
    @JoinColumn(name = "comerciante_id", nullable = false)
    val comerciante: Usuario = Usuario(),
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    val produto: Produto = Produto(),
    @Column(name = "quantidade_sequestrada", nullable = false)
    val quantidadeSequestrada: BigDecimal = BigDecimal.ZERO,
    @Column(name = "valor_bruto_venda", nullable = false)
    val valorBrutoVenda: BigDecimal = BigDecimal.ZERO,
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Column(name = "status_processamento", nullable = false)
    val statusProcessamento: StatusProcessamento = StatusProcessamento.CONCLUIDO,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
)
