package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.RateioResultado
import br.edu.ufape.agrofeira.domain.entity.Repasse
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.domain.enums.StatusPagamento
import br.edu.ufape.agrofeira.dto.request.RepasseRequest
import br.edu.ufape.agrofeira.exception.BusinessRuleException
import br.edu.ufape.agrofeira.repository.RateioResultadoRepository
import br.edu.ufape.agrofeira.repository.RepasseRepository
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
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class RepasseServiceTest {
    @Mock
    lateinit var repository: RepasseRepository

    @Mock
    lateinit var rateioResultadoRepository: RateioResultadoRepository

    @Mock
    lateinit var comercianteService: ComercianteService

    @Mock
    lateinit var feiraService: FeiraService

    @InjectMocks
    lateinit var service: RepasseService

    @Test
    fun `listarPorComerciante deve retornar lista do comerciante`() {
        val comercianteId = UUID.randomUUID()
        val repasse = Repasse(id = UUID.randomUUID(), comerciante = Usuario(id = comercianteId))

        `when`(repository.findByComercianteId(comercianteId)).thenReturn(listOf(repasse))

        val result = service.listarPorComerciante(comercianteId)

        assertEquals(1, result.size)
        verify(comercianteService).buscarPorId(comercianteId)
    }

    @Test
    fun `relatorioMensal deve agrupar por mes`() {
        val ano = 2024
        val repasse =
            Repasse(
                id = UUID.randomUUID(),
                valorBruto = BigDecimal("100.00"),
                valorLiquido = BigDecimal("100.00"),
                repassadoEm = LocalDateTime.of(2024, 1, 15, 10, 0),
            )

        `when`(repository.findAll()).thenReturn(listOf(repasse))

        val result = service.relatorioMensal(ano)

        assertEquals(1, result.size)
        assertEquals("Jan", result[0].mesLabel)
        assertEquals(BigDecimal("100.00"), result[0].totalBruto)
    }

    @Test
    fun `relatorioGeralPorComerciante deve agrupar e ordenar por valor`() {
        val c1 = Usuario(id = UUID.randomUUID(), nome = "C1")
        val c2 = Usuario(id = UUID.randomUUID(), nome = "C2")
        val r1 = Repasse(id = UUID.randomUUID(), comerciante = c1, valorBruto = BigDecimal("100.00"))
        val r2 = Repasse(id = UUID.randomUUID(), comerciante = c2, valorBruto = BigDecimal("200.00"))

        `when`(repository.findAll()).thenReturn(listOf(r1, r2))

        val result = service.relatorioGeralPorComerciante()

        assertEquals(2, result.size)
        assertEquals("C2", result[0].comerciante.nome) // C2 primeiro (200 > 100)
    }

    @Test
    fun `registrar deve salvar novo repasse quando nao duplicado`() {
        val rateioResultadoId = UUID.randomUUID()
        val request = RepasseRequest(rateioResultadoId)
        val comerciante = Usuario(id = UUID.randomUUID(), nome = "Vendedor")
        val rateioResultado =
            RateioResultado(
                id = rateioResultadoId,
                comerciante = comerciante,
                valorBrutoVenda = BigDecimal("100.00"),
            )

        `when`(rateioResultadoRepository.findById(rateioResultadoId)).thenReturn(Optional.of(rateioResultado))
        `when`(repository.findByComercianteId(comerciante.id)).thenReturn(emptyList())
        `when`(repository.save(any(Repasse::class.java))).thenAnswer { it.arguments[0] }

        val result = service.registrar(request)

        assertNotNull(result)
        assertEquals(BigDecimal("100.00"), result.valorBruto)
        assertEquals(StatusPagamento.PAGO, result.status)
        verify(repository).save(any(Repasse::class.java))
    }

    @Test
    fun `registrar deve lancar excecao quando repasse ja existe para o rateio`() {
        val rateioResultadoId = UUID.randomUUID()
        val request = RepasseRequest(rateioResultadoId)
        val comerciante = Usuario(id = UUID.randomUUID(), nome = "Vendedor")
        val rateioResultado =
            RateioResultado(
                id = rateioResultadoId,
                comerciante = comerciante,
                valorBrutoVenda = BigDecimal("100.00"),
            )
        val repasseExistente =
            Repasse(
                id = UUID.randomUUID(),
                rateioResultado = rateioResultado,
                comerciante = comerciante,
                valorBruto = BigDecimal("100.00"),
                valorLiquido = BigDecimal("100.00"),
            )

        `when`(rateioResultadoRepository.findById(rateioResultadoId)).thenReturn(Optional.of(rateioResultado))
        `when`(repository.findByComercianteId(comerciante.id)).thenReturn(listOf(repasseExistente))

        assertThrows(BusinessRuleException::class.java) {
            service.registrar(request)
        }
    }

    @Test
    fun `listarTotaisPorFeira deve retornar lista agrupada`() {
        val feiraId = UUID.randomUUID()
        val comerciante = Usuario(id = UUID.randomUUID(), nome = "Vendedor")
        val repasse =
            Repasse(
                id = UUID.randomUUID(),
                comerciante = comerciante,
                valorBruto = BigDecimal("100.00"),
                valorLiquido = BigDecimal("90.00"),
            )

        `when`(repository.findByRateioResultadoFeiraId(feiraId)).thenReturn(listOf(repasse))

        val result = service.listarTotaisPorFeira(feiraId)

        assertEquals(1, result.size)
        assertEquals("Vendedor", result[0].comerciante.nome)
        assertEquals(BigDecimal("100.00"), result[0].totalBruto)
    }
}
