package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.RepasseRequest
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.RepasseDTO
import br.edu.ufape.agrofeira.dto.response.RepasseTotaisDTO
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import br.edu.ufape.agrofeira.service.RepasseService
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
@RequestMapping("/api/v1/repasses")
@Tag(name = "Repasses", description = "Pagamento aos Comerciantes")
@IsManagerOrAdmin
class RepasseController(
    private val service: RepasseService,
) {
    @GetMapping
    @Operation(summary = "Listar todos os repasses")
    fun listarTodos(
        @PageableDefault(size = 10, page = 0) pageable: Pageable,
    ): ResponseEntity<ApiResponse<Page<RepasseDTO>>> {
        val repasses = service.listarTodos(pageable).map { it.toDTO() }
        return ResponseEntity.ok(ApiResponse(success = true, message = "Repasses recuperados com sucesso", data = repasses))
    }

    @GetMapping("/comerciante/{comercianteId}")
    @Operation(summary = "Listar repasses por comerciante")
    fun listarPorComerciante(
        @PathVariable comercianteId: UUID,
    ): ResponseEntity<ApiResponse<List<RepasseDTO>>> {
        val repasses = service.listarPorComerciante(comercianteId).map { it.toDTO() }
        return ResponseEntity.ok(ApiResponse(success = true, message = "Repasses do comerciante recuperados com sucesso", data = repasses))
    }

    @GetMapping("/feira/{feiraId}")
    @Operation(summary = "Listar repasses por feira")
    fun listarPorFeira(
        @PathVariable feiraId: UUID,
    ): ResponseEntity<ApiResponse<List<RepasseDTO>>> {
        val repasses = service.listarPorFeira(feiraId).map { it.toDTO() }
        return ResponseEntity.ok(ApiResponse(success = true, message = "Repasses da feira recuperados com sucesso", data = repasses))
    }

    @GetMapping("/feira/{feiraId}/totais")
    @Operation(summary = "Listar totais de repasse por comerciante em uma feira")
    fun listarTotaisPorFeira(
        @PathVariable feiraId: UUID,
    ): ResponseEntity<ApiResponse<List<RepasseTotaisDTO>>> {
        val totais = service.listarTotaisPorFeira(feiraId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Totais de repasse recuperados com sucesso", data = totais))
    }

    @PostMapping
    @Operation(summary = "Registrar repasse para um comerciante")
    fun registrarRepasse(
        @RequestBody @Valid request: RepasseRequest,
    ): ResponseEntity<ApiResponse<RepasseDTO>> {
        val repasse = service.registrar(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(success = true, message = "Repasse registrado com sucesso", data = repasse.toDTO()))
    }
}
