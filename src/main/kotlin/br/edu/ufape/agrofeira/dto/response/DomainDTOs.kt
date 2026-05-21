package br.edu.ufape.agrofeira.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Schema(description = "Dados resumidos do usuário")
data class UsuarioDTO(
    val id: UUID,
    val nome: String,
    val email: String?,
    val telefone: String?,
    @Schema(description = "Descrição ou biografia do usuário/comerciante")
    val descricao: String?,
    val perfis: Set<String>,
)

@Schema(description = "Dados detalhados do usuário (incluindo endereço)")
data class UsuarioDetalhadoDTO(
    val id: UUID,
    val nome: String,
    val email: String?,
    val telefone: String?,
    @Schema(description = "Descrição ou biografia do usuário/comerciante")
    val descricao: String?,
    val perfis: Set<String>,
    @Schema(description = "Dados do endereço do usuário")
    val endereco: EnderecoDTO?,
)

@Schema(description = "Dados detalhados do produto")
data class ProdutoDTO(
    val id: UUID,
    val nome: String,
    val categoria: String?,
    val unidadeMedida: String,
    val precoBase: BigDecimal,
)

@Schema(description = "Dados da feira")
data class FeiraDTO(
    val id: UUID,
    val dataHora: LocalDateTime,
    val status: String,
    val ativa: Boolean,
)

@Schema(description = "Dados do pedido")
data class PedidoDTO(
    val id: UUID,
    val feiraId: UUID,
    val consumidorNome: String,
    val status: String,
    val tipoRetirada: String,
    val valorProdutos: BigDecimal,
    val taxaEntrega: BigDecimal,
    val valorTotal: BigDecimal,
    val itens: List<ItemPedidoDTO>,
    val criadoEm: LocalDateTime,
)

@Schema(description = "Item detalhado do pedido (Snapshot)")
data class ItemPedidoDTO(
    val produtoId: UUID,
    val nomeItem: String,
    val unidadeMedida: String,
    val quantidade: BigDecimal,
    val valorUnitario: BigDecimal,
    val valorTotal: BigDecimal,
)

@Schema(description = "Dados do pagamento")
data class PagamentoDTO(
    val id: UUID,
    val pedidoId: UUID,
    val valor: BigDecimal,
    val metodo: String?,
    val status: String,
    val pagoEm: LocalDateTime?,
    val criadoEm: LocalDateTime,
)

@Schema(description = "Dados da zona de entrega")
data class ZonaEntregaDTO(
    val id: UUID,
    val nome: String,
    val taxa: BigDecimal,
    val ativo: Boolean,
)

@Schema(description = "Dados do relatório")
data class RelatorioDTO(
    val id: UUID,
    val titulo: String,
    val tipo: String,
    val conteudo: String?,
    val criadoEm: LocalDateTime,
)

@Schema(description = "Dados do repasse a um comerciante")
data class RepasseDTO(
    val id: UUID,
    val rateioResultadoId: UUID,
    val comerciante: UsuarioDTO,
    val feiraId: UUID,
    val valorBruto: BigDecimal,
    val valorLiquido: BigDecimal,
    val status: String,
    val repassadoEm: LocalDateTime?,
    val criadoEm: LocalDateTime,
)

@Schema(description = "Totais de repasse de um comerciante em uma feira")
data class RepasseTotaisDTO(
    val comerciante: UsuarioDTO,
    val totalBruto: BigDecimal,
    val totalLiquido: BigDecimal,
    val quantidadeRepasses: Int,
)

@Schema(description = "Resultado do rateio de um comerciante em uma feira")
data class RateioResultadoDTO(
    val id: UUID,
    val produto: ProdutoDTO,
    val quantidadeSequestrada: BigDecimal,
    val valorBrutoVenda: BigDecimal,
    val statusProcessamento: String,
)

@Schema(description = "Visão geral e balanço financeiro de uma feira")
data class FeiraDetalhesDTO(
    val id: UUID,
    val dataHora: LocalDateTime,
    val status: String,
    val totalPedidos: Int,
    val totalComerciantes: Int,
    val totalProdutos: Int,
    val valorTotalPedidos: BigDecimal,
    val valorTotalProdutos: BigDecimal,
    val totalTaxasEntrega: BigDecimal,
    val pedidosPorStatus: Map<String, Int>,
    val totalRateado: BigDecimal,
)

@Schema(description = "Participação de um comerciante em uma feira")
data class FeiraComercianteDTO(
    val comerciante: UsuarioDTO,
    val ofertas: List<OfertaEstoqueDTO>,
    val rateioResultados: List<RateioResultadoDTO>,
    val totalOfertado: BigDecimal,
    val totalSequestrado: BigDecimal,
    val totalBrutoVenda: BigDecimal,
)

@Schema(description = "Dados da oferta de estoque de um comerciante em uma feira")
data class OfertaEstoqueDTO(
    val id: UUID,
    val feira: FeiraDTO,
    val comerciante: UsuarioDTO,
    val produto: ProdutoDTO,
    val quantidadeOfertada: BigDecimal,
    val quantidadeReservada: BigDecimal,
    val quantidadeDisponivel: BigDecimal,
    val criadoEm: LocalDateTime,
    val atualizadoEm: LocalDateTime,
)

@Schema(description = "Opção de domínio para seleção")
data class OpcaoDTO(
    val value: String,
    val label: String,
)

@Schema(description = "Conjunto de opções para cadastro de produtos")
data class ItensOpcoesDTO(
    val categorias: List<OpcaoDTO>,
    val unidadesMedida: List<OpcaoDTO>,
)
