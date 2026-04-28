package br.edu.ufape.agrofeira.domain.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*

@Embeddable
data class FeiraComercianteId(
    @Column(name = "feira_id")
    val feiraId: UUID,
    @Column(name = "comerciante_id")
    val comercianteId: UUID,
) : Serializable

@Entity
@Table(name = "feira_comerciantes_elegiveis")
data class FeiraComercianteElegivel(
    @EmbeddedId
    val id: FeiraComercianteId,
    @ManyToOne
    @MapsId("feiraId")
    @JoinColumn(name = "feira_id")
    val feira: Feira,
    @ManyToOne
    @MapsId("comercianteId")
    @JoinColumn(name = "comerciante_id")
    val comerciante: Usuario,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
)
