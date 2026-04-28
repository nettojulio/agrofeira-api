package br.edu.ufape.agrofeira.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Dados para autenticação de usuário")
data class LoginRequest(
    @field:NotBlank(message = "O identificador (email ou telefone) é obrigatório")
    @Schema(description = "E-mail ou Telefone do usuário", example = "joao@email.com")
    val username: String,
    @field:NotBlank(message = "A senha é obrigatória")
    @Schema(description = "Senha do usuário", example = "123456")
    val password: String,
)

@Schema(description = "Dados para cadastro de novo usuário")
data class RegisterRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    @Schema(description = "Nome completo", example = "João Agricultor")
    val nome: String,
    @field:Email(message = "E-mail inválido")
    @Schema(description = "E-mail único", example = "joao@email.com")
    val email: String? = null,
    @field:Pattern(
        regexp = "^\\d{10,11}$",
        message = "Telefone inválido, deve conter apenas 10 ou 11 dígitos numéricos",
    )
    @Schema(description = "Telefone único", example = "87987654321")
    val telefone: String? = null,
    @field:NotBlank(message = "A senha é obrigatória")
    @field:Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    @Schema(description = "Senha de acesso", example = "123456")
    val senha: String,
    @field:NotEmpty(message = "Pelo menos um perfil deve ser informado")
    @field:Size(min = 1, max = 1, message = "Nesta versão, o usuário deve ter exatamente 1 perfil")
    @Schema(description = "Nomes dos perfis do usuário", example = "[\"COMERCIANTE\"]")
    val perfis: Set<String>,
)

@Schema(description = "Dados para solicitação de recuperação de senha")
data class ForgotPasswordRequest(
    @field:NotBlank(message = "O identificador (email ou telefone) é obrigatório")
    @Schema(description = "E-mail ou Telefone do usuário", example = "joao@email.com")
    val identifier: String,
)

@Schema(description = "Dados para renovação de token")
data class RefreshTokenRequest(
    @field:NotBlank(message = "O refresh token é obrigatório")
    @Schema(description = "Token de renovação", example = "a1b2c3d4...")
    val refreshToken: String,
)

@Schema(description = "Dados para redefinição de senha")
data class ResetPasswordRequest(
    @field:NotBlank(message = "O token é obrigatório")
    @Schema(description = "Token de recuperação enviado por email/sms", example = "a1b2c3d4...")
    val token: String,
    @field:NotBlank(message = "A nova senha é obrigatória")
    @field:Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    @Schema(description = "Nova senha de acesso", example = "novasenha123456")
    val novaSenha: String,
)

@Schema(description = "Dados para realizar logout")
data class LogoutRequest(
    @field:NotBlank(message = "O refresh token é obrigatório")
    @Schema(description = "Token de renovação para ser invalidado", example = "a1b2c3d4...")
    val refreshToken: String,
)
