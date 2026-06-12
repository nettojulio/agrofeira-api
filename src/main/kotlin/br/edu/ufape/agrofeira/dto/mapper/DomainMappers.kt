package br.edu.ufape.agrofeira.dto.mapper

import br.edu.ufape.agrofeira.domain.entity.Endereco
import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.entity.ItemPedido
import br.edu.ufape.agrofeira.domain.entity.OfertaEstoque
import br.edu.ufape.agrofeira.domain.entity.Pagamento
import br.edu.ufape.agrofeira.domain.entity.Pedido
import br.edu.ufape.agrofeira.domain.entity.Produto
import br.edu.ufape.agrofeira.domain.entity.RateioResultado
import br.edu.ufape.agrofeira.domain.entity.Relatorio
import br.edu.ufape.agrofeira.domain.entity.Repasse
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.domain.entity.ZonaEntrega
import br.edu.ufape.agrofeira.dto.response.EnderecoDTO
import br.edu.ufape.agrofeira.dto.response.FeiraDTO
import br.edu.ufape.agrofeira.dto.response.ItemPedidoDTO
import br.edu.ufape.agrofeira.dto.response.OfertaEstoqueDTO
import br.edu.ufape.agrofeira.dto.response.PagamentoDTO
import br.edu.ufape.agrofeira.dto.response.PedidoDTO
import br.edu.ufape.agrofeira.dto.response.ProdutoDTO
import br.edu.ufape.agrofeira.dto.response.RateioResultadoDTO
import br.edu.ufape.agrofeira.dto.response.RelatorioDTO
import br.edu.ufape.agrofeira.dto.response.RepasseDTO
import br.edu.ufape.agrofeira.dto.response.UsuarioDTO
import br.edu.ufape.agrofeira.dto.response.UsuarioDetalhadoDTO
import br.edu.ufape.agrofeira.dto.response.ZonaEntregaDTO

fun Usuario.toDTO() =
    UsuarioDTO(
        id = id,
        nome = nome,
        email = email,
        telefone = telefone,
        descricao = descricao,
        perfis = perfis.map { it.nome }.toSet(),
        categorias = categorias.map { it.name }.toSet(),
    )

fun Usuario.toDetalhadoDTO(endereco: Endereco? = null) =
    UsuarioDetalhadoDTO(
        id = id,
        nome = nome,
        email = email,
        telefone = telefone,
        descricao = descricao,
        perfis = perfis.map { it.nome }.toSet(),
        categorias = categorias.map { it.name }.toSet(),
        endereco = endereco?.toDTO(),
    )

fun Produto.toDTO() =
    ProdutoDTO(
        id = id,
        nome = nome,
        categoria = categoria.name,
        unidadeMedida = unidadeMedida.name,
        precoBase = precoBase,
    )

fun Feira.toDTO() =
    FeiraDTO(
        id = id,
        dataHora = dataHora,
        status = status.name,
        ativa = ativo,
    )

fun Pedido.toDTO(itens: List<ItemPedido>) =
    PedidoDTO(
        id = id,
        feiraId = feira.id,
        consumidorNome = consumidor.nome,
        status = status.name,
        tipoRetirada = tipoRetirada.name,
        valorProdutos = valorProdutos,
        taxaEntrega = taxaEntrega,
        valorTotal = valorTotal,
        itens = itens.map { it.toDTO() },
        criadoEm = criadoEm,
    )

fun ItemPedido.toDTO() =
    ItemPedidoDTO(
        produtoId = produto.id,
        nomeItem = nomeItem,
        unidadeMedida = unidadeMedida.name,
        quantidade = quantidade,
        valorUnitario = valorUnitario,
        valorTotal = valorUnitario.multiply(quantidade),
    )

fun Pagamento.toDTO() =
    PagamentoDTO(
        id = id,
        pedidoId = pedido.id,
        valor = valor,
        metodo = metodo,
        status = status.name,
        pagoEm = pagoEm,
        criadoEm = criadoEm,
    )

fun ZonaEntrega.toDTO() =
    ZonaEntregaDTO(
        id = id,
        nome = nome,
        taxa = taxa,
        ativo = ativo,
    )

fun Endereco.toDTO() =
    EnderecoDTO(
        usuarioId = usuarioId!!,
        rua = rua,
        numero = numero,
        complemento = complemento,
        bairro = bairro,
        cidade = cidade,
        estado = estado,
        cep = cep,
        zonaEntrega = zonaEntrega?.toDTO(),
    )

fun Repasse.toDTO() =
    RepasseDTO(
        id = id,
        rateioResultadoId = rateioResultado.id,
        comerciante = comerciante.toDTO(),
        feiraId = rateioResultado.feira.id,
        produtoNome = rateioResultado.produto.nome,
        produtoUnidade = rateioResultado.produto.unidadeMedida.name,
        quantidadeVendida = rateioResultado.quantidadeSequestrada,
        valorBruto = valorBruto,
        valorLiquido = valorLiquido,
        status = status.name,
        repassadoEm = repassadoEm,
        criadoEm = criadoEm,
    )

fun RateioResultado.toDTO() =
    RateioResultadoDTO(
        id = id,
        produto = produto.toDTO(),
        quantidadeSequestrada = quantidadeSequestrada,
        valorBrutoVenda = valorBrutoVenda,
        statusProcessamento = statusProcessamento.name,
    )

fun OfertaEstoque.toDTO() =
    OfertaEstoqueDTO(
        id = id,
        feira = feira.toDTO(),
        comerciante = comerciante.toDTO(),
        produto = produto.toDTO(),
        quantidadeOfertada = quantidadeOfertada,
        quantidadeReservada = quantidadeReservada,
        quantidadeDisponivel = quantidadeOfertada.subtract(quantidadeReservada),
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm,
    )

fun Relatorio.toDTO() =
    RelatorioDTO(
        id = id,
        titulo = titulo,
        tipo = tipo.name,
        conteudo = conteudo,
        criadoEm = criadoEm,
    )
