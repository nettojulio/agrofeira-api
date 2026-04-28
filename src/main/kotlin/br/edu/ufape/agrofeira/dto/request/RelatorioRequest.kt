package br.edu.ufape.agrofeira.dto.request

import br.edu.ufape.agrofeira.domain.enums.TipoRelatorio
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "Dados para criação de registro de relatório")
data class RelatorioRequest(
    @field:NotBlank(message = "O título é obrigatório")
    @Schema(description = "Título do relatório", example = "Vendas de Maio 2026")
    val titulo: String,
    @field:NotNull(message = "O tipo é obrigatório")
    @Schema(description = "Tipo do relatório", example = "MENSAL")
    val tipo: TipoRelatorio,
    @Schema(description = "Conteúdo ou parâmetros do relatório", example = "{\"mes\": 5, \"ano\": 2026}")
    val conteudo: String? = null,
)
