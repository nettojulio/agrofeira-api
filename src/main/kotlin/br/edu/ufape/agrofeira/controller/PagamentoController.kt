package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.PagamentoRequest
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.PagamentoDTO
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import br.edu.ufape.agrofeira.service.PagamentoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/pagamentos")
@Tag(name = "Pagamentos", description = "Acompanhamento e detalhes de pagamentos dos pedidos")
class PagamentoController(
    private val service: PagamentoService,
) {
    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasAnyRole('CONSUMIDOR', 'ADMINISTRADOR', 'GERENCIADOR')")
    @Operation(summary = "Listar Pagamentos por Pedido")
    fun listarPorPedido(
        @PathVariable pedidoId: UUID,
    ): ResponseEntity<ApiResponse<List<PagamentoDTO>>> {
        val pagamentos = service.listarPorPedido(pedidoId).map { it.toDTO() }
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Pagamentos recuperados com sucesso",
                data = pagamentos,
            ),
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONSUMIDOR', 'ADMINISTRADOR', 'GERENCIADOR')")
    @Operation(summary = "Buscar Pagamento por ID")
    fun buscarPorId(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<PagamentoDTO>> {
        val pagamento = service.buscarPorId(id).toDTO()
        return ResponseEntity.ok(ApiResponse(success = true, message = "Pagamento encontrado", data = pagamento))
    }

    @PostMapping
    @IsManagerOrAdmin
    @Operation(summary = "Registrar Pagamento (Apenas Gerência)")
    fun registrar(
        @RequestBody @Valid request: PagamentoRequest,
    ): ResponseEntity<ApiResponse<PagamentoDTO>> {
        val pagamento = service.registrar(request).toDTO()
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(success = true, message = "Pagamento registrado com sucesso", data = pagamento),
        )
    }

    @DeleteMapping("/{id}")
    @IsManagerOrAdmin
    @Operation(summary = "Deletar Pagamento (Soft Delete) - Apenas Gerência")
    fun deletar(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        service.deletar(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Pagamento removido com sucesso"))
    }
}
