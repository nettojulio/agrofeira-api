package br.edu.ufape.agrofeira.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "ofertas_estoque")
data class OfertaEstoque(
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
    @Column(name = "quantidade_ofertada", nullable = false)
    val quantidadeOfertada: BigDecimal = BigDecimal.ZERO,
    @Column(name = "quantidade_reservada", nullable = false)
    var quantidadeReservada: BigDecimal = BigDecimal.ZERO,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
)
