package br.edu.ufape.agrofeira.dto.request

import br.edu.ufape.agrofeira.domain.enums.TipoRetirada
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.*

@Schema(description = "Dados para realização de pedido")
data class PedidoRequest(
    @field:NotNull(message = "O ID da feira é obrigatório")
    val feiraId: UUID,
    @field:NotNull(message = "O tipo de retirada é obrigatório")
    val tipoRetirada: TipoRetirada,
    @field:NotEmpty(message = "O pedido deve conter pelo menos um item")
    val itens: List<ItemPedidoRequest>,
)

@Schema(description = "Item de um pedido")
data class ItemPedidoRequest(
    @field:NotNull(message = "O ID do produto é obrigatório")
    val produtoId: UUID,
    @field:Positive(message = "A quantidade deve ser positiva")
    val quantidade: BigDecimal,
)
