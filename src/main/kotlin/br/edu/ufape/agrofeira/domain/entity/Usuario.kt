package br.edu.ufape.agrofeira.domain.entity

import br.edu.ufape.agrofeira.domain.enums.CategoriaProduto
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "usuarios")
@SQLDelete(sql = "UPDATE usuarios SET deletado_em = NOW(), ativo = false WHERE id = ? AND versao = ?")
@SQLRestriction("deletado_em IS NULL")
data class Usuario(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val nome: String = "",
    @Column(unique = true)
    val email: String? = null,
    @Column(unique = true)
    val telefone: String? = null,
    @Column(columnDefinition = "TEXT")
    val descricao: String? = null,
    @Column(name = "senha_hash", nullable = false)
    val senhaHash: String = "",
    val ativo: Boolean = true,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "deletado_em")
    val deletadoEm: LocalDateTime? = null,
    @Version
    val versao: Long = 0,
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_perfil",
        joinColumns = [JoinColumn(name = "usuario_id")],
        inverseJoinColumns = [JoinColumn(name = "perfil_id")],
    )
    val perfis: MutableSet<Perfil> = mutableSetOf(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_categorias", joinColumns = [JoinColumn(name = "usuario_id")])
    @Column(name = "categoria")
    @Enumerated(EnumType.STRING)
    val categorias: MutableSet<CategoriaProduto> = mutableSetOf(),
)
