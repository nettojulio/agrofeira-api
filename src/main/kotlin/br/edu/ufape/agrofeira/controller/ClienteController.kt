package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.ClienteRequest
import br.edu.ufape.agrofeira.dto.request.ClienteUpdateRequest
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.UsuarioDTO
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import br.edu.ufape.agrofeira.service.ClienteService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Gerenciamento de clientes")
class ClienteController(
    private val clienteService: ClienteService,
) {
    @GetMapping
    @IsManagerOrAdmin
    @Operation(summary = "Listar clientes")
    fun listar(
        @PageableDefault(size = 10, page = 0) pageable: Pageable,
    ): ResponseEntity<ApiResponse<Page<UsuarioDTO>>> {
        val clientes = clienteService.listar(pageable).map { it.toDTO() }
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Clientes recuperados com sucesso",
                data = clientes,
            ),
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.isResourceOwner(#id) or hasAnyRole('ADMINISTRADOR', 'GERENCIADOR')")
    @Operation(summary = "Buscar cliente por ID (Perfil)")
    fun buscarPorId(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<UsuarioDTO>> {
        val cliente = clienteService.buscarPorId(id).toDTO()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Cliente recuperado com sucesso",
                data = cliente,
            ),
        )
    }

    @PostMapping
    @IsManagerOrAdmin
    @Operation(summary = "Cadastrar cliente")
    fun criar(
        @RequestBody @Valid request: ClienteRequest,
    ): ResponseEntity<ApiResponse<UsuarioDTO>> {
        val novoCliente = clienteService.criar(request).toDTO()
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(success = true, message = "Cliente cadastrado com sucesso", data = novoCliente))
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityUtils.isResourceOwner(#id) or hasAnyRole('ADMINISTRADOR', 'GERENCIADOR')")
    @Operation(summary = "Atualizar cliente")
    fun atualizar(
        @PathVariable id: UUID,
        @RequestBody @Valid request: ClienteUpdateRequest,
    ): ResponseEntity<ApiResponse<UsuarioDTO>> {
        val clienteAtualizado = clienteService.atualizar(id, request).toDTO()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Cliente atualizado com sucesso",
                data = clienteAtualizado,
            ),
        )
    }

    @DeleteMapping("/{id}")
    @IsManagerOrAdmin
    @Operation(summary = "Deletar cliente")
    fun deletar(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        clienteService.deletar(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Cliente deletado com sucesso"))
    }
}
