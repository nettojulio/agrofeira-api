package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.OfertaEstoque
import br.edu.ufape.agrofeira.dto.request.OfertaEstoqueRequest
import br.edu.ufape.agrofeira.dto.request.OfertaEstoqueUpdateRequest
import br.edu.ufape.agrofeira.exception.BusinessRuleException
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.OfertaEstoqueRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class OfertaEstoqueService(
    private val repository: OfertaEstoqueRepository,
    private val feiraService: FeiraService,
    private val comercianteService: ComercianteService,
    private val produtoService: ProdutoService,
) {
    @Transactional
    fun cadastrar(request: OfertaEstoqueRequest): OfertaEstoque {
        val feira = feiraService.buscarPorId(request.feiraId)
        val comerciante = comercianteService.buscarPorId(request.comercianteId)
        val produto = produtoService.buscarPorId(request.produtoId)

        if (repository.existsByFeiraIdAndComercianteIdAndProdutoId(feira.id, comerciante.id, produto.id)) {
            throw BusinessRuleException(
                "Já existe uma oferta do comerciante '${comerciante.nome}' para o produto '${produto.nome}' nesta feira",
                "OFERTA_DUPLICADA",
            )
        }

        return repository.save(
            OfertaEstoque(
                feira = feira,
                comerciante = comerciante,
                produto = produto,
                quantidadeOfertada = request.quantidadeOfertada,
            ),
        )
    }

    @Transactional
    fun atualizar(
        id: UUID,
        request: OfertaEstoqueUpdateRequest,
    ): OfertaEstoque {
        val oferta = buscarPorId(id)

        if (request.quantidadeOfertada < oferta.quantidadeReservada) {
            throw BusinessRuleException(
                "A nova quantidade ofertada (${request.quantidadeOfertada}) não pode ser menor que a quantidade já reservada (${oferta.quantidadeReservada})",
                "QUANTIDADE_ABAIXO_RESERVADA",
            )
        }

        return repository.save(
            oferta.copy(
                quantidadeOfertada = request.quantidadeOfertada,
                atualizadoEm = LocalDateTime.now(),
            ),
        )
    }

    fun buscarPorId(id: UUID): OfertaEstoque =
        repository
            .findById(id)
            .orElseThrow { ResourceNotFoundException("OfertaEstoque", id.toString()) }

    fun listarPorFeira(feiraId: UUID): List<OfertaEstoque> {
        feiraService.buscarPorId(feiraId)
        return repository.findByFeiraId(feiraId)
    }

    fun listarPorFeiraEItem(
        feiraId: UUID,
        produtoId: UUID,
    ): List<OfertaEstoque> {
        feiraService.buscarPorId(feiraId)
        produtoService.buscarPorId(produtoId)
        return repository.buscarPorFeiraEProduto(feiraId, produtoId)
    }
}
