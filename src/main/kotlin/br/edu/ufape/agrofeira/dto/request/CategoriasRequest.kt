package br.edu.ufape.agrofeira.dto.request

data class CategoriasRequest(
    val categorias: Set<String> = emptySet(),
)
