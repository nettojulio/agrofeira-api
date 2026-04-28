package br.edu.ufape.agrofeira.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "fila_rateio")
data class FilaRateio(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne
    @JoinColumn(name = "comerciante_id", nullable = false)
    val comerciante: Usuario = Usuario(),
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    val produto: Produto = Produto(),
    @ManyToOne
    @JoinColumn(name = "feira_origem_id", nullable = false)
    val feiraOrigem: Feira = Feira(),
    @Column(name = "quantidade_deficit", nullable = false)
    val quantidadeDeficit: BigDecimal = BigDecimal.ZERO,
    val compensado: Boolean = false,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
)
