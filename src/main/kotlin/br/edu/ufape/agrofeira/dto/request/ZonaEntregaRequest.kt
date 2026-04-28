package br.edu.ufape.agrofeira.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

@Schema(description = "Dados para cadastro/atualização de zona de entrega")
data class ZonaEntregaRequest(
    @field:NotBlank(message = "O bairro é obrigatório")
    @Schema(description = "Nome do bairro", example = "Cohab I")
    val bairro: String,
    @Schema(description = "Região da cidade", example = "Zona Norte")
    val regiao: String? = null,
    @field:NotNull(message = "A taxa é obrigatória")
    @field:PositiveOrZero(message = "A taxa não pode ser negativa")
    @Schema(description = "Taxa de entrega para esta zona", example = "5.00")
    val taxa: BigDecimal,
)
