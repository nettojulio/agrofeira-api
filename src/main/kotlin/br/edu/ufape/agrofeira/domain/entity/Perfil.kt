package br.edu.ufape.agrofeira.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "perfis")
data class Perfil(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(nullable = false, unique = true)
    val nome: String = "",
)
