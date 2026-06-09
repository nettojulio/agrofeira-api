package br.edu.ufape.agrofeira.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Embeddable
data class FeiraProdutoId(
    @Column(name = "feira_id")
    val feiraId: UUID,
    @Column(name = "produto_id")
    val produtoId: UUID,
) : Serializable

@Entity
@Table(name = "feira_produtos_elegiveis")
data class FeiraProdutoElegivel(
    @EmbeddedId
    val id: FeiraProdutoId,
    @ManyToOne
    @MapsId("feiraId")
    @JoinColumn(name = "feira_id")
    val feira: Feira,
    @ManyToOne
    @MapsId("produtoId")
    @JoinColumn(name = "produto_id")
    val produto: Produto,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
)
