package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.entity.OfertaEstoque
import br.edu.ufape.agrofeira.domain.entity.Produto
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.dto.request.OfertaEstoqueRequest
import br.edu.ufape.agrofeira.dto.request.OfertaEstoqueUpdateRequest
import br.edu.ufape.agrofeira.exception.BusinessRuleException
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.OfertaEstoqueRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class OfertaEstoqueServiceTest {
    @Mock
    lateinit var repository: OfertaEstoqueRepository

    @Mock
    lateinit var feiraService: FeiraService

    @Mock
    lateinit var comercianteService: ComercianteService

    @Mock
    lateinit var produtoService: ProdutoService

    @InjectMocks
    lateinit var service: OfertaEstoqueService

    private val id = UUID.randomUUID()
    private val feiraId = UUID.randomUUID()
    private val comercianteId = UUID.randomUUID()
    private val produtoId = UUID.randomUUID()

    @Test
    fun `cadastrar deve salvar nova oferta quando nao duplicada`() {
        val request = OfertaEstoqueRequest(feiraId, comercianteId, produtoId, BigDecimal.TEN)
        val feira = Feira(id = feiraId)
        val comerciante = Usuario(id = comercianteId, nome = "Comerciante")
        val produto = Produto(id = produtoId, nome = "Produto")

        `when`(feiraService.buscarPorId(feiraId)).thenReturn(feira)
        `when`(comercianteService.buscarPorId(comercianteId)).thenReturn(comerciante)
        `when`(produtoService.buscarPorId(produtoId)).thenReturn(produto)
        `when`(repository.existsByFeiraIdAndComercianteIdAndProdutoId(feiraId, comercianteId, produtoId)).thenReturn(
            false,
        )
        `when`(repository.save(any(OfertaEstoque::class.java))).thenAnswer { it.arguments[0] }

        val result = service.cadastrar(request)

        assertNotNull(result)
        assertEquals(BigDecimal.TEN, result.quantidadeOfertada)
        verify(repository).save(any(OfertaEstoque::class.java))
    }

    @Test
    fun `cadastrar deve lancar excecao quando oferta for duplicada`() {
        val request = OfertaEstoqueRequest(feiraId, comercianteId, produtoId, BigDecimal.TEN)
        val feira = Feira(id = feiraId)
        val comerciante = Usuario(id = comercianteId, nome = "Comerciante")
        val produto = Produto(id = produtoId, nome = "Produto")

        `when`(feiraService.buscarPorId(feiraId)).thenReturn(feira)
        `when`(comercianteService.buscarPorId(comercianteId)).thenReturn(comerciante)
        `when`(produtoService.buscarPorId(produtoId)).thenReturn(produto)
        `when`(repository.existsByFeiraIdAndComercianteIdAndProdutoId(feiraId, comercianteId, produtoId)).thenReturn(
            true,
        )

        assertThrows(BusinessRuleException::class.java) {
            service.cadastrar(request)
        }
    }

    @Test
    fun `atualizar deve salvar alteracoes quando quantidade for valida`() {
        val request = OfertaEstoqueUpdateRequest(BigDecimal("15.00"))
        val oferta =
            OfertaEstoque(id = id, quantidadeOfertada = BigDecimal.TEN, quantidadeReservada = BigDecimal("5.00"))

        `when`(repository.findById(id)).thenReturn(Optional.of(oferta))
        `when`(repository.save(any(OfertaEstoque::class.java))).thenAnswer { it.arguments[0] }

        val result = service.atualizar(id, request)

        assertEquals(BigDecimal("15.00"), result.quantidadeOfertada)
    }

    @Test
    fun `atualizar deve lancar excecao quando nova quantidade for menor que reservada`() {
        val request = OfertaEstoqueUpdateRequest(BigDecimal.ONE)
        val oferta =
            OfertaEstoque(id = id, quantidadeOfertada = BigDecimal.TEN, quantidadeReservada = BigDecimal("5.00"))

        `when`(repository.findById(id)).thenReturn(Optional.of(oferta))

        assertThrows(BusinessRuleException::class.java) {
            service.atualizar(id, request)
        }
    }

    @Test
    fun `buscarPorId deve retornar oferta se existir`() {
        val oferta = OfertaEstoque(id = id)
        `when`(repository.findById(id)).thenReturn(Optional.of(oferta))

        val result = service.buscarPorId(id)

        assertEquals(id, result.id)
    }

    @Test
    fun `buscarPorId deve lancar excecao se nao existir`() {
        `when`(repository.findById(id)).thenReturn(Optional.empty())

        assertThrows(ResourceNotFoundException::class.java) {
            service.buscarPorId(id)
        }
    }
}
