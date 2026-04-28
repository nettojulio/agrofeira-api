package br.edu.ufape.agrofeira.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.util.*

@Schema(description = "Dados para cadastro ou atualização de endereço")
data class EnderecoRequest(
    @field:NotBlank(message = "A rua é obrigatória")
    @Schema(description = "Nome da rua/logradouro", example = "Rua das Flores")
    val rua: String,
    @field:NotBlank(message = "O número é obrigatório")
    @Schema(description = "Número da residência", example = "123")
    val numero: String,
    @Schema(description = "Complemento (opcional)", example = "Apto 101")
    val complemento: String? = null,
    @field:NotBlank(message = "A cidade é obrigatória")
    @Schema(description = "Nome da cidade", example = "Garanhuns")
    val cidade: String,
    @field:NotBlank(message = "O estado é obrigatório")
    @field:Pattern(regexp = "^[A-Z]{2}$", message = "O estado deve conter 2 letras maiúsculas (Ex: PE)")
    @Schema(description = "Sigla do estado", example = "PE")
    val estado: String,
    @field:NotBlank(message = "O CEP é obrigatório")
    @field:Pattern(regexp = "^\\d{8}$", message = "O CEP deve conter exatamente 8 dígitos numéricos")
    @Schema(description = "CEP sem traço", example = "55290000")
    val cep: String,
    @Schema(description = "ID da Zona de Entrega vinculada", example = "dddd4444-0000-0000-0000-000000000001")
    val zonaEntregaId: UUID,
)
