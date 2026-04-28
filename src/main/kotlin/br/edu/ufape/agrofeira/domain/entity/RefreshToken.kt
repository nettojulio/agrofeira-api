package br.edu.ufape.agrofeira.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "refresh_tokens")
data class RefreshToken(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true)
    val token: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    val usuario: Usuario,
    @Column(nullable = false)
    val expiracao: Instant,
    @Column(nullable = false)
    var revogado: Boolean = false,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: Instant = Instant.now(),
)
