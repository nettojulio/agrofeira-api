package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.PedidoRequest
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.PedidoDTO
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import br.edu.ufape.agrofeira.service.CustomUserDetails
import br.edu.ufape.agrofeira.service.PedidoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Realização e consulta de pedidos")
class PedidoController(
    private val service: PedidoService,
) {
    @GetMapping
    @IsManagerOrAdmin
    @Operation(summary = "Listar todos os pedidos")
    fun listar(
        @PageableDefault(size = 10, page = 0) pageable: Pageable,
    ): ResponseEntity<ApiResponse<Page<PedidoDTO>>> {
        val pedidos = service.listar(pageable).map { it.toDTO(service.buscarItens(it.id)) }
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Pedidos recuperados com sucesso",
                data = pedidos,
            ),
        )
    }

    @PostMapping
    @IsManagerOrAdmin
    @Operation(
        summary = "Criar um novo pedido (Apenas Gerência)",
        description = "Realiza o snapshot dos produtos e a reserva atômica de estoque. Restrito a ADMIN/GERENCIADOR por regra de negócio.",
    )
    fun criar(
        @RequestBody @Valid request: PedidoRequest,
    ): ResponseEntity<ApiResponse<PedidoDTO>> {
        val auth =
            SecurityContextHolder.getContext().authentication
                ?: throw br.edu.ufape.agrofeira.exception
                    .UnauthorizedActionException("Usuário não autenticado")

        val userDetails = auth.principal as CustomUserDetails

        val pedido = service.criar(request, userDetails.usuario.id)
        val itens = service.buscarItens(pedido.id)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                message = "Pedido realizado com sucesso",
                data = pedido.toDTO(itens),
            ),
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONSUMIDOR', 'ADMINISTRADOR', 'GERENCIADOR')")
    @Operation(summary = "Consultar detalhes de um pedido")
    fun buscarPorId(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<PedidoDTO>> {
        val pedido = service.buscarPorId(id)
        val itens = service.buscarItens(pedido.id)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Pedido encontrado",
                data = pedido.toDTO(itens),
            ),
        )
    }

    @DeleteMapping("/{id}")
    @IsManagerOrAdmin
    @Operation(summary = "Deletar pedido (Soft Delete)")
    fun deletar(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        service.deletar(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Pedido deletado com sucesso"))
    }

    @PatchMapping("/{id}/status")
    @IsManagerOrAdmin
    @Operation(summary = "Atualizar Status do Pedido")
    fun atualizarStatus(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @GetMapping("/feira/{feiraId}")
    @Operation(summary = "Buscar pedidos de uma feira especifica")
    fun buscarPorFeira(
        @PathVariable feiraId: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @GetMapping("/{id}/rateio")
    @Operation(summary = "Consultar Rateio")
    fun consultarRateio(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @PostMapping("/{id}/rateio/confirmar")
    @Operation(summary = "Confirmar Rateio")
    fun confirmarRateio(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @GetMapping("/{id}/rateio/disponibilidade")
    @Operation(summary = "Consultar Disponibilidade Rateio")
    fun consultarDisponibilidadeRateio(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))

    @PostMapping("/{id}/rateio/confirmar-disponibilidade")
    @Operation(summary = "Confirmar Disponibilidade de Rateio")
    fun confirmarDisponibilidadeRateio(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse(success = false, message = "Not Implemented"))
}
