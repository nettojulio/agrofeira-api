package br.edu.ufape.agrofeira.dto.request

import br.edu.ufape.agrofeira.domain.enums.CategoriaProduto
import br.edu.ufape.agrofeira.domain.enums.UnidadeMedida
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

@Schema(description = "Dados para cadastro/atualização de produto")
data class ProdutoRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    @Schema(description = "Nome do produto", example = "Tomate Orgânico")
    val nome: String,
    @field:NotNull(message = "A categoria é obrigatória")
    @Schema(description = "Categoria do produto", example = "HORTIFRUTI")
    val categoria: CategoriaProduto,
    @field:NotNull(message = "A unidade de medida é obrigatória")
    @Schema(description = "Unidade de medida", example = "QUILO")
    val unidadeMedida: UnidadeMedida,
    @field:Positive(message = "O preço base deve ser positivo")
    @field:Digits(integer = 8, fraction = 2, message = "O preço base deve ter no máximo 2 casas decimais")
    @Schema(description = "Preço unitário base", example = "6.50")
    val precoBase: BigDecimal,
)
