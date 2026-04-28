package br.edu.ufape.agrofeira.dto.request

import br.edu.ufape.agrofeira.domain.enums.StatusPagamento
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "Dados para registro de pagamento")
data class PagamentoRequest(
    @field:NotNull(message = "O ID do pedido é obrigatório")
    @Schema(description = "ID do pedido associado", example = "550e8400-e29b-41d4-a716-446655440000")
    val pedidoId: UUID,
    @field:NotNull(message = "O valor é obrigatório")
    @field:Positive(message = "O valor deve ser positivo")
    @Schema(description = "Valor pago", example = "150.50")
    val valor: BigDecimal,
    @Schema(description = "Método de pagamento", example = "PIX")
    val metodo: String? = null,
    @Schema(description = "Status do pagamento", example = "PAGO")
    val status: StatusPagamento = StatusPagamento.PAGO,
)
