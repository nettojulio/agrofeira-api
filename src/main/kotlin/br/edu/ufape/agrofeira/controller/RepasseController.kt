package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/repasses")
@Tag(name = "Repasses", description = "Pagamento aos Comerciantes")
@IsManagerOrAdmin
class RepasseController {
    @GetMapping
    @Operation(summary = "Listar Todos os Repasses")
    fun listarTodos(): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @GetMapping("/comerciante/{comercianteId}")
    @Operation(summary = "Listar Repasses por Comerciante")
    fun listarPorComerciante(
        @PathVariable comercianteId: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @GetMapping("/feira/{feiraId}")
    @Operation(summary = "Listar Repasses por Feira")
    fun listarPorFeira(
        @PathVariable feiraId: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @GetMapping("/feira/{feiraId}/totais")
    @Operation(summary = "Listar Totais por Feira")
    fun listarTotaisPorFeira(
        @PathVariable feiraId: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @PostMapping
    @Operation(summary = "Registrar Repasse")
    fun registrarRepasse(): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))
}
