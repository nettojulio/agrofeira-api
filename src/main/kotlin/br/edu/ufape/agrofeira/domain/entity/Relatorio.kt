package br.edu.ufape.agrofeira.domain.entity

import br.edu.ufape.agrofeira.domain.enums.TipoRelatorio
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "relatorios")
@SQLDelete(sql = "UPDATE relatorios SET deletado_em = NOW(), ativo = false WHERE id = ? AND versao = ?")
@SQLRestriction("deletado_em IS NULL")
data class Relatorio(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Version
    val versao: Long = 0,
    @Column(nullable = false)
    val titulo: String = "",
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    val tipo: TipoRelatorio = TipoRelatorio.GERAL,
    @Column(columnDefinition = "TEXT")
    val conteudo: String? = null,
    val ativo: Boolean = true,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "deletado_em")
    val deletadoEm: LocalDateTime? = null,
)
