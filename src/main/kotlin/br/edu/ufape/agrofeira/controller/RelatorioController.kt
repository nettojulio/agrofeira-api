package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.RelatorioRequest
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.RelatorioDTO
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import br.edu.ufape.agrofeira.service.RelatorioService
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
@RequestMapping("/api/v1/relatorios")
@Tag(name = "Relatórios Financeiros", description = "Faturamento mensal e geral")
@IsManagerOrAdmin
class RelatorioController(
    private val service: RelatorioService,
) {
    @GetMapping
    @Operation(summary = "Listar registros de relatórios")
    fun listar(
        @PageableDefault(size = 10, page = 0) pageable: Pageable,
    ): ResponseEntity<ApiResponse<Page<RelatorioDTO>>> {
        val relatorios = service.listar(pageable).map { it.toDTO() }
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Relatórios recuperados com sucesso",
                data = relatorios,
            ),
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar relatório por ID")
    fun buscarPorId(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<RelatorioDTO>> {
        val relatorio = service.buscarPorId(id).toDTO()
        return ResponseEntity.ok(ApiResponse(success = true, message = "Relatório encontrado", data = relatorio))
    }

    @PostMapping
    @Operation(summary = "Criar/Gerar registro de relatório")
    fun criar(
        @RequestBody @Valid request: RelatorioRequest,
    ): ResponseEntity<ApiResponse<RelatorioDTO>> {
        val relatorio = service.criar(request).toDTO()
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(success = true, message = "Relatório gerado com sucesso", data = relatorio),
        )
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar registro de relatório")
    fun atualizar(
        @PathVariable id: UUID,
        @RequestBody @Valid request: RelatorioRequest,
    ): ResponseEntity<ApiResponse<RelatorioDTO>> {
        val relatorio = service.atualizar(id, request).toDTO()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Relatório atualizado com sucesso",
                data = relatorio,
            ),
        )
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover registro de relatório (Soft Delete)")
    fun deletar(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        service.deletar(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Relatório removido com sucesso"))
    }

    @GetMapping("/por-mes")
    @Operation(summary = "Relatório por Mês (Legado/Atalho)")
    fun relatorioMensal(): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Utilize o POST para gerar relatórios detalhados"))

    @GetMapping("/por-comerciante")
    @Operation(summary = "Relatório por Comerciante (Legado/Atalho)")
    fun relatorioComerciante(
        @RequestParam(required = false) ano: Int?,
        @RequestParam(required = false) mes: Int?,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Utilize o POST para gerar relatórios detalhados"))
}
