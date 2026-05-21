package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.OfertaEstoqueRequest
import br.edu.ufape.agrofeira.dto.request.OfertaEstoqueUpdateRequest
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.OfertaEstoqueDTO
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import br.edu.ufape.agrofeira.service.OfertaEstoqueService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/estoque-bancas")
@Tag(name = "Estoque", description = "Gerenciamento de ofertas de estoque em feiras")
class OfertaEstoqueController(
    private val service: OfertaEstoqueService,
) {
    @PostMapping
    @IsManagerOrAdmin
    @Operation(summary = "Cadastrar oferta de estoque de um comerciante em uma feira")
    fun cadastrar(
        @RequestBody @Valid request: OfertaEstoqueRequest,
    ): ResponseEntity<ApiResponse<OfertaEstoqueDTO>> {
        val oferta = service.cadastrar(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(success = true, message = "Oferta de estoque cadastrada com sucesso", data = oferta.toDTO()))
    }

    @PutMapping("/{id}")
    @IsManagerOrAdmin
    @Operation(summary = "Atualizar quantidade ofertada de um estoque")
    fun atualizar(
        @PathVariable id: UUID,
        @RequestBody @Valid request: OfertaEstoqueUpdateRequest,
    ): ResponseEntity<ApiResponse<OfertaEstoqueDTO>> {
        val oferta = service.atualizar(id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Oferta de estoque atualizada com sucesso", data = oferta.toDTO()))
    }

    @GetMapping("/{id}")
    @IsManagerOrAdmin
    @Operation(summary = "Buscar oferta de estoque por ID")
    fun buscarPorId(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<OfertaEstoqueDTO>> {
        val oferta = service.buscarPorId(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Oferta de estoque recuperada com sucesso", data = oferta.toDTO()))
    }

    @GetMapping("/feira/{feiraId}")
    @IsManagerOrAdmin
    @Operation(summary = "Listar todas as ofertas de estoque de uma feira")
    fun listarPorFeira(
        @PathVariable feiraId: UUID,
    ): ResponseEntity<ApiResponse<List<OfertaEstoqueDTO>>> {
        val ofertas = service.listarPorFeira(feiraId).map { it.toDTO() }
        return ResponseEntity.ok(ApiResponse(success = true, message = "Ofertas de estoque recuperadas com sucesso", data = ofertas))
    }

    @GetMapping("/feira/{feiraId}/item/{itemId}")
    @IsManagerOrAdmin
    @Operation(summary = "Listar ofertas de estoque de uma feira filtradas por produto")
    fun listarPorFeiraEItem(
        @PathVariable feiraId: UUID,
        @PathVariable itemId: UUID,
    ): ResponseEntity<ApiResponse<List<OfertaEstoqueDTO>>> {
        val ofertas = service.listarPorFeiraEItem(feiraId, itemId).map { it.toDTO() }
        return ResponseEntity.ok(ApiResponse(success = true, message = "Ofertas de estoque recuperadas com sucesso", data = ofertas))
    }
}
