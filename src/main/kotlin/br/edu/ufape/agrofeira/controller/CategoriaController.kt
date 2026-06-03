package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.domain.enums.CategoriaProduto
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.CategoriaDTO
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorias", description = "Categorias de produtos disponíveis para comerciantes")
class CategoriaController {
    @GetMapping
    @IsManagerOrAdmin
    @Operation(summary = "Listar todas as categorias disponíveis")
    fun listar(): ResponseEntity<ApiResponse<List<CategoriaDTO>>> {
        val categorias = CategoriaProduto.entries.map { CategoriaDTO(id = it.name, nome = it.descricao) }
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Categorias recuperadas com sucesso", data = categorias),
        )
    }
}
