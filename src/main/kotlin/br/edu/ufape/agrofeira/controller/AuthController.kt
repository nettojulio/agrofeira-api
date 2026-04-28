package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.request.*
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.LoginResponse
import br.edu.ufape.agrofeira.dto.response.UsuarioDTO
import br.edu.ufape.agrofeira.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
    name = "Autenticação",
    description = "Endpoints de login, registro, renovação de token, recuperação de senha e logout",
)
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/login")
    @Operation(summary = "Realizar login")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses
                .ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Requisição inválida (Ex: campos vazios)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Credenciais inválidas (Não autorizado)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "Acesso negado (Perfil não autorizado na versão atual)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "422",
                description = "Erro de validação semântica",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
        ],
    )
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = authService.login(request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Login realizado com sucesso", data = response))
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token usando refresh token")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Token renovado com sucesso",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Requisição inválida",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Refresh token inválido, expirado ou revogado",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
        ],
    )
    fun refresh(
        @Valid @RequestBody request: RefreshTokenRequest,
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = authService.refreshToken(request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Token renovado com sucesso", data = response))
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENCIADOR')")
    @PostMapping("/register")
    @Operation(summary = "Cadastrar novo usuário (Apenas Administrador e Gerenciador)")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "Usuário cadastrado com sucesso",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Requisição inválida (Ex: telefone inválido, múltiplos perfis)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Acesso não autorizado",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "Acesso negado (Apenas Administrador e Gerenciador, ou tentativa de criar perfil superior)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
        ],
    )
    fun register(
        @Valid @RequestBody request: RegisterRequest,
    ): ResponseEntity<ApiResponse<UsuarioDTO>> {
        val response = authService.register(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(success = true, message = "Usuário cadastrado com sucesso", data = response))
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperação de senha")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Se o usuário estiver cadastrado, um token de recuperação será gerado",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Requisição inválida (Ex: campo vazio, usuário sem e-mail)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "Recuperação não permitida para o perfil do usuário",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
        ],
    )
    fun forgotPassword(
        @Valid @RequestBody request: ForgotPasswordRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        authService.forgotPassword(request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Se o usuário estiver cadastrado, um link de recuperação será enviado.",
            ),
        )
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha com token")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Senha redefinida com sucesso",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Requisição inválida (Ex: nova senha muito curta)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Token de recuperação inválido, expirado ou já utilizado",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
        ],
    )
    fun resetPassword(
        @Valid @RequestBody request: ResetPasswordRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        authService.resetPassword(request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Senha redefinida com sucesso."))
    }

    @PostMapping("/logout")
    @Operation(summary = "Realizar logout (invalida o refresh token)")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Logout realizado com sucesso",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Requisição inválida (Ex: token não fornecido)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Refresh token inválido ou já revogado",
                content = [Content(schema = Schema(implementation = ApiResponse::class))],
            ),
        ],
    )
    fun logout(
        @Valid @RequestBody request: LogoutRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        authService.logout(request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Logout realizado com sucesso."))
    }
}
