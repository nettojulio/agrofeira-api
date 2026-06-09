package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.RelatorioRequest
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.FaturamentoMensalDTO
import br.edu.ufape.agrofeira.dto.response.RelatorioDTO
import br.edu.ufape.agrofeira.dto.response.RepasseTotaisDTO
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import br.edu.ufape.agrofeira.service.RelatorioService
import br.edu.ufape.agrofeira.service.RepasseService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/v1/relatorios")
@Tag(name = "Relatórios Financeiros", description = "Faturamento mensal e geral")
@IsManagerOrAdmin
class RelatorioController(
    private val service: RelatorioService,
    private val repasseService: RepasseService,
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
    @Operation(summary = "Faturamento mensal agregado dos repasses")
    fun relatorioMensal(
        @RequestParam(required = false) ano: Int?,
    ): ResponseEntity<ApiResponse<List<FaturamentoMensalDTO>>> {
        val anoRef = ano ?: LocalDate.now().year
        val dados = repasseService.relatorioMensal(anoRef)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Relatório mensal gerado com sucesso", data = dados),
        )
    }

    @GetMapping("/por-comerciante")
    @Operation(summary = "Faturamento total por comerciante (todos os períodos)")
    fun relatorioComerciante(
        @RequestParam(required = false) ano: Int?,
        @RequestParam(required = false) mes: Int?,
    ): ResponseEntity<ApiResponse<List<RepasseTotaisDTO>>> {
        val dados = repasseService.relatorioGeralPorComerciante()
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Relatório por comerciante gerado com sucesso", data = dados),
        )
    }
}
