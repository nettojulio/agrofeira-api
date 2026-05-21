package br.edu.ufape.agrofeira.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.*

@Schema(description = "Dados para cadastro de oferta de estoque")
data class OfertaEstoqueRequest(
    @field:NotNull(message = "A feira é obrigatória")
    @Schema(description = "ID da feira")
    val feiraId: UUID,
    @field:NotNull(message = "O comerciante é obrigatório")
    @Schema(description = "ID do comerciante")
    val comercianteId: UUID,
    @field:NotNull(message = "O produto é obrigatório")
    @Schema(description = "ID do produto")
    val produtoId: UUID,
    @field:NotNull(message = "A quantidade ofertada é obrigatória")
    @field:Positive(message = "A quantidade ofertada deve ser positiva")
    @field:Digits(integer = 10, fraction = 3, message = "A quantidade deve ter no máximo 3 casas decimais")
    @Schema(description = "Quantidade ofertada pelo comerciante", example = "50.000")
    val quantidadeOfertada: BigDecimal,
)

@Schema(description = "Dados para atualização de oferta de estoque")
data class OfertaEstoqueUpdateRequest(
    @field:NotNull(message = "A quantidade ofertada é obrigatória")
    @field:Positive(message = "A quantidade ofertada deve ser positiva")
    @field:Digits(integer = 10, fraction = 3, message = "A quantidade deve ter no máximo 3 casas decimais")
    @Schema(description = "Nova quantidade ofertada pelo comerciante", example = "60.000")
    val quantidadeOfertada: BigDecimal,
)
