package br.edu.ufape.agrofeira.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.client.RestTemplate

@ExtendWith(MockitoExtension::class)
class ViaCepServiceTest {
    @Mock
    lateinit var restTemplate: RestTemplate

    @InjectMocks
    lateinit var service: ViaCepService

    @Test
    fun `consultarCep deve retornar dados quando CEP for valido`() {
        val cep = "55290000"
        val url = "https://viacep.com.br/ws/$cep/json"
        val mockResponse =
            ViaCepResponse(cep = "55290-000", logradouro = "Rua Teste", localidade = "Garanhuns", uf = "PE")

        `when`(restTemplate.getForObject(url, ViaCepResponse::class.java)).thenReturn(mockResponse)

        val result = service.consultarCep(cep)

        assertNotNull(result)
        assertEquals("55290-000", result?.cep)
        assertEquals("Garanhuns", result?.localidade)
    }

    @Test
    fun `consultarCep deve retornar null quando API retornar erro`() {
        val cep = "00000000"
        val url = "https://viacep.com.br/ws/$cep/json"
        val mockResponse = ViaCepResponse(erro = true)

        `when`(restTemplate.getForObject(url, ViaCepResponse::class.java)).thenReturn(mockResponse)

        val result = service.consultarCep(cep)

        assertNull(result)
    }

    @Test
    fun `consultarCep deve retornar null quando ocorrer excecao`() {
        val cep = "123"
        val url = "https://viacep.com.br/ws/$cep/json"

        `when`(restTemplate.getForObject(url, ViaCepResponse::class.java)).thenThrow(RuntimeException("API Offline"))

        val result = service.consultarCep(cep)

        assertNull(result)
    }
}
