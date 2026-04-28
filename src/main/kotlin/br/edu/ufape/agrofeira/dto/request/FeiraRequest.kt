package br.edu.ufape.agrofeira.dto.request

import br.edu.ufape.agrofeira.domain.enums.StatusFeira
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Schema(description = "Dados para criação de feira")
data class FeiraRequest(
    @field:NotNull(message = "A data e hora são obrigatórias")
    @Schema(description = "Data e hora de realização", example = "2026-05-20T08:00:00")
    val dataHora: LocalDateTime,
    @Schema(description = "Status inicial da feira", example = "RASCUNHO")
    val status: StatusFeira = StatusFeira.RASCUNHO,
)
