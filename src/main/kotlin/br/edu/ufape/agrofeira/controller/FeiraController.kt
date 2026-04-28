package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.domain.enums.StatusFeira
import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.FeiraRequest
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.FeiraDTO
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import br.edu.ufape.agrofeira.service.FeiraService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/feiras")
@Tag(name = "Feiras", description = "Gerenciamento de feiras e detalhamento")
class FeiraController(
    private val service: FeiraService,
) {
    @GetMapping
    @Operation(summary = "Listar feiras")
    fun listar(
        @PageableDefault(size = 10, page = 0) pageable: Pageable,
    ): ResponseEntity<ApiResponse<Page<FeiraDTO>>> {
        val feiras = service.listar(pageable).map { it.toDTO() }
        return ResponseEntity.ok(ApiResponse(success = true, message = "Feiras recuperadas com sucesso", data = feiras))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar feira por ID")
    fun buscarPorId(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<FeiraDTO>> {
        val feira = service.buscarPorId(id).toDTO()
        return ResponseEntity.ok(ApiResponse(success = true, message = "Feira recuperada com sucesso", data = feira))
    }

    @PostMapping
    @IsManagerOrAdmin
    @Operation(summary = "Cadastrar feira (incluindo participantes e itens elegíveis)")
    fun criar(
        @RequestBody @Valid request: FeiraRequest,
    ): ResponseEntity<ApiResponse<FeiraDTO>> {
        val novaFeira = service.criar(request).toDTO()
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(success = true, message = "Feira cadastrada com sucesso", data = novaFeira))
    }

    @PutMapping("/{id}")
    @IsManagerOrAdmin
    @Operation(summary = "Atualizar feira")
    fun atualizar(
        @PathVariable id: UUID,
        @RequestBody @Valid request: FeiraRequest,
    ): ResponseEntity<ApiResponse<FeiraDTO>> {
        val feiraAtualizada = service.atualizar(id, request).toDTO()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Feira atualizada com sucesso",
                data = feiraAtualizada,
            ),
        )
    }

    @PatchMapping("/{id}/status")
    @IsManagerOrAdmin
    @Operation(summary = "Atualizar status da feira")
    fun atualizarStatus(
        @PathVariable id: UUID,
        @RequestParam novoStatus: StatusFeira,
    ): ResponseEntity<ApiResponse<FeiraDTO>> {
        val feiraAtualizada = service.atualizarStatus(id, novoStatus).toDTO()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Status da feira atualizado com sucesso",
                data = feiraAtualizada,
            ),
        )
    }

    @DeleteMapping("/{id}")
    @IsManagerOrAdmin
    @Operation(summary = "Deletar feira (Soft Delete)")
    fun deletar(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        service.deletar(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Feira deletada com sucesso"))
    }

    @GetMapping("/{id}/detalhes")
    @Operation(summary = "Detalhamento da feira (Visão Geral, Balanço Financeiro)")
    fun detalhesDaFeira(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @GetMapping("/{id}/comerciante/{comercianteId}")
    @Operation(summary = "Buscar Feira Comerciante")
    fun buscarPorComerciante(
        @PathVariable id: UUID,
        @PathVariable comercianteId: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))
}
