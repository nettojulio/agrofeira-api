package br.edu.ufape.agrofeira.domain.entity

import br.edu.ufape.agrofeira.domain.enums.CategoriaProduto
import br.edu.ufape.agrofeira.domain.enums.UnidadeMedida
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "produtos")
@SQLDelete(sql = "UPDATE produtos SET deletado_em = NOW(), ativo = false WHERE id = ? AND versao = ?")
@SQLRestriction("deletado_em IS NULL")
data class Produto(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val nome: String = "",
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val categoria: CategoriaProduto = CategoriaProduto.OUTROS,
    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", nullable = false)
    val unidadeMedida: UnidadeMedida = UnidadeMedida.UNIDADE,
    @Column(name = "preco_base", nullable = false)
    val precoBase: BigDecimal = BigDecimal.ZERO,
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
