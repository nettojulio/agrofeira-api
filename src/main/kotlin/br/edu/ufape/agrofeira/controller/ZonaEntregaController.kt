package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.ZonaEntregaRequest
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.ZonaEntregaDTO
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import br.edu.ufape.agrofeira.service.ZonaEntregaService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/zonas-entrega")
@Tag(name = "Zonas de Entrega", description = "Gerenciamento de taxas e regiões de entrega")
class ZonaEntregaController(
    private val service: ZonaEntregaService,
) {
    @GetMapping
    @Operation(summary = "Listar Zonas de Entrega")
    fun listar(): ResponseEntity<ApiResponse<List<ZonaEntregaDTO>>> {
        val zonas = service.listar().map { it.toDTO() }
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Zonas de entrega recuperadas com sucesso",
                data = zonas,
            ),
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Zona de Entrega Específica")
    fun buscarPorId(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<ZonaEntregaDTO>> {
        val zona = service.buscarPorId(id).toDTO()
        return ResponseEntity.ok(ApiResponse(success = true, message = "Zona de entrega encontrada", data = zona))
    }

    @PostMapping
    @IsManagerOrAdmin
    @Operation(summary = "Cadastrar Zona de Entrega (Apenas Gerência)")
    fun criar(
        @RequestBody @Valid request: ZonaEntregaRequest,
    ): ResponseEntity<ApiResponse<ZonaEntregaDTO>> {
        val novaZona = service.criar(request).toDTO()
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(success = true, message = "Zona de entrega cadastrada com sucesso", data = novaZona),
        )
    }

    @PutMapping("/{id}")
    @IsManagerOrAdmin
    @Operation(summary = "Atualizar Zona de Entrega Específica (Apenas Gerência)")
    fun atualizar(
        @PathVariable id: UUID,
        @RequestBody @Valid request: ZonaEntregaRequest,
    ): ResponseEntity<ApiResponse<ZonaEntregaDTO>> {
        val zonaAtualizada = service.atualizar(id, request).toDTO()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Zona de entrega atualizada com sucesso",
                data = zonaAtualizada,
            ),
        )
    }

    @DeleteMapping("/{id}")
    @IsManagerOrAdmin
    @Operation(summary = "Deletar Zona de Entrega (Soft Delete) - Apenas Gerência")
    fun deletar(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        service.deletar(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Zona de entrega removida com sucesso"))
    }
}
