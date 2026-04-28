package br.edu.ufape.agrofeira.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detalhes de um erro na resposta da API")
data class ApiErrorDetail(
    @Schema(description = "Campo que gerou o erro", example = "nome")
    val field: String? = null,
    @Schema(description = "Regra de validação violada", example = "NotBlank")
    val rule: String? = null,
    @Schema(description = "Mensagem detalhada do erro", example = "O nome é obrigatório")
    val detail: String,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Envelope padrão de resposta da API")
data class ApiResponse<T>(
    @Schema(description = "Timestamp da resposta no formato ISO-8601", example = "2023-10-27T10:00:00Z")
    val timestamp: String = Instant.now().toString(),
    @Schema(description = "Indica se a operação foi bem-sucedida")
    val success: Boolean,
    @Schema(description = "Mensagem resumida sobre o resultado da operação")
    val message: String,
    @Schema(description = "Dados de retorno da operação")
    val data: T? = null,
    @Schema(description = "Lista de erros, caso a operação tenha falhado")
    val errors: List<ApiErrorDetail>? = null,
)
