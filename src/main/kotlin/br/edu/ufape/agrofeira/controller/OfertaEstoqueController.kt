package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/estoque-bancas")
@Tag(name = "Estoque", description = "Gerenciamento de ofertas de estoque em feiras")
@PreAuthorize("hasAnyRole('COMERCIANTE', 'ADMINISTRADOR', 'GERENCIADOR')")
class OfertaEstoqueController {
    @PostMapping
    @Operation(summary = "Cadastrar Estoque (Oferta)")
    fun cadastrar(): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Estoque por ID")
    fun atualizar(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Estoque por ID")
    fun buscarPorId(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @GetMapping("/feira/{feiraId}")
    @Operation(summary = "Listar Estoque por Feira")
    fun listarPorFeira(
        @PathVariable feiraId: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @GetMapping("/feira/{feiraId}/item/{itemId}")
    @Operation(summary = "Listar Estoque por Feira e Item")
    fun listarPorFeiraEItem(
        @PathVariable feiraId: UUID,
        @PathVariable itemId: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))
}
