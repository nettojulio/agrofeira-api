package br.edu.ufape.agrofeira.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(description = "Dados para registro de repasse")
data class RepasseRequest(
    @field:NotNull(message = "O resultado de rateio é obrigatório")
    @Schema(description = "ID do resultado de rateio a ser repassado")
    val rateioResultadoId: UUID,
)
