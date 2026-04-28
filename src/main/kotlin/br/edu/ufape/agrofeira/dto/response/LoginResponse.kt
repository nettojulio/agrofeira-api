package br.edu.ufape.agrofeira.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Resposta de login com token de acesso")
data class LoginResponse(
    @Schema(description = "Token JWT para autenticação", example = "eyJhbGciOiJIUzI1NiJ9...")
    val token: String,
    @Schema(description = "Token para renovar o acesso", example = "a1b2c3d4...")
    val refreshToken: String,
)
