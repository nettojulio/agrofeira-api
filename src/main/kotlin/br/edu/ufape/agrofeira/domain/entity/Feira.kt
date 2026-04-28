package br.edu.ufape.agrofeira.domain.entity

import br.edu.ufape.agrofeira.domain.enums.StatusFeira
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "feiras")
@SQLDelete(sql = "UPDATE feiras SET deletado_em = NOW(), ativo = false WHERE id = ? AND versao = ?")
@SQLRestriction("deletado_em IS NULL")
data class Feira(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "data_hora", nullable = false)
    val dataHora: LocalDateTime = LocalDateTime.now(),
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    val status: StatusFeira = StatusFeira.RASCUNHO,
    val ativo: Boolean = true,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "deletado_em")
    val deletadoEm: LocalDateTime? = null,
    @Version
    val versao: Long = 0,
)
