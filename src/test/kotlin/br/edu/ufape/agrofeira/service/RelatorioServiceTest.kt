package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Relatorio
import br.edu.ufape.agrofeira.domain.enums.TipoRelatorio
import br.edu.ufape.agrofeira.dto.request.RelatorioRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.RelatorioRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

@ExtendWith(MockitoExtension::class)
class RelatorioServiceTest {
    @Mock
    lateinit var repository: RelatorioRepository

    @InjectMocks
    lateinit var service: RelatorioService

    private val id = UUID.randomUUID()
    private lateinit var relatorio: Relatorio

    @BeforeEach
    fun setUp() {
        relatorio = Relatorio(id = id, titulo = "Relatório de Teste", tipo = TipoRelatorio.MENSAL)
    }

    @Test
    fun `listar deve retornar pagina de relatorios`() {
        val pageable = PageRequest.of(0, 10)
        `when`(repository.findAll(pageable)).thenReturn(PageImpl(listOf(relatorio)))

        val result = service.listar(pageable)

        assertEquals(1, result.totalElements)
        assertEquals("Relatório de Teste", result.content[0].titulo)
    }

    @Test
    fun `buscarPorId deve retornar relatorio se existir`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(relatorio))

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

    @Test
    fun `criar deve salvar novo relatorio`() {
        val request = RelatorioRequest("Novo Relatório", TipoRelatorio.GERAL, "Conteudo")
        `when`(repository.save(any(Relatorio::class.java))).thenAnswer { it.arguments[0] }

        val result = service.criar(request)

        assertNotNull(result)
        assertEquals("Novo Relatório", result.titulo)
        assertEquals(TipoRelatorio.GERAL, result.tipo)
    }

    @Test
    fun `atualizar deve salvar alteracoes`() {
        val request = RelatorioRequest("Relatório Alterado", TipoRelatorio.POR_FEIRA, "Novo Conteudo")
        `when`(repository.findById(id)).thenReturn(Optional.of(relatorio))
        `when`(repository.save(any(Relatorio::class.java))).thenAnswer { it.arguments[0] }

        val result = service.atualizar(id, request)

        assertEquals("Relatório Alterado", result.titulo)
        assertEquals(TipoRelatorio.POR_FEIRA, result.tipo)
    }

    @Test
    fun `deletar deve chamar repository delete`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(relatorio))

        service.deletar(id)

        verify(repository).delete(relatorio)
    }
}
