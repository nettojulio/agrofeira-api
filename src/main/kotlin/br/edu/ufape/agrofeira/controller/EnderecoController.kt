package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.EnderecoRequest
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.EnderecoDTO
import br.edu.ufape.agrofeira.exception.UnauthorizedActionException
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import br.edu.ufape.agrofeira.service.CustomUserDetails
import br.edu.ufape.agrofeira.service.EnderecoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/enderecos")
@Tag(name = "Endereços", description = "Gerenciamento de endereços de usuários")
class EnderecoController(
    private val service: EnderecoService,
) {
    @GetMapping("/me")
    @Operation(summary = "Buscar meu endereço")
    fun buscarMeuEndereco(): ResponseEntity<ApiResponse<EnderecoDTO>> {
        val usuarioId = getAuthenticatedUserId()
        val endereco = service.buscarPorUsuarioId(usuarioId).toDTO()
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Endereço recuperado com sucesso", data = endereco),
        )
    }

    @PutMapping("/me")
    @Operation(summary = "Cadastrar ou atualizar meu endereço")
    fun salvarMeuEndereco(
        @RequestBody @Valid request: EnderecoRequest,
    ): ResponseEntity<ApiResponse<EnderecoDTO>> {
        val usuarioId = getAuthenticatedUserId()
        val endereco = service.salvarEndereco(usuarioId, request).toDTO()
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Endereço salvo com sucesso", data = endereco),
        )
    }

    @GetMapping("/usuario/{usuarioId}")
    @IsManagerOrAdmin
    @Operation(summary = "Buscar endereço de um usuário específico (Admin/Gerente)")
    fun buscarPorUsuarioId(
        @PathVariable usuarioId: UUID,
    ): ResponseEntity<ApiResponse<EnderecoDTO>> {
        val endereco = service.buscarPorUsuarioId(usuarioId).toDTO()
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Endereço recuperado com sucesso", data = endereco),
        )
    }

    @PutMapping("/usuario/{usuarioId}")
    @IsManagerOrAdmin
    @Operation(summary = "Cadastrar ou atualizar endereço de um usuário específico (Admin/Gerente)")
    fun salvarPorUsuarioId(
        @PathVariable usuarioId: UUID,
        @RequestBody @Valid request: EnderecoRequest,
    ): ResponseEntity<ApiResponse<EnderecoDTO>> {
        val endereco = service.salvarEndereco(usuarioId, request).toDTO()
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Endereço salvo com sucesso", data = endereco),
        )
    }

    @DeleteMapping("/me")
    @Operation(summary = "Remover meu endereço")
    fun deletarMeuEndereco(): ResponseEntity<ApiResponse<Unit>> {
        val usuarioId = getAuthenticatedUserId()
        service.deletarEndereco(usuarioId)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Endereço removido com sucesso"),
        )
    }

    private fun getAuthenticatedUserId(): UUID {
        val auth =
            SecurityContextHolder.getContext().authentication
                ?: throw UnauthorizedActionException("Usuário não autenticado")
        val principal =
            auth.principal as? CustomUserDetails
                ?: throw UnauthorizedActionException("Principal inválido")
        return principal.usuario.id
    }
}
