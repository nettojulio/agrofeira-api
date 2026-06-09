package br.edu.ufape.agrofeira.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.core.MethodParameter
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mail.MailSendException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleMailSendException deve retornar INTERNAL_SERVER_ERROR`() {
        val ex = MailSendException("Erro ao enviar e-mail")
        val response = handler.handleMailSendException(ex)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertFalse(response.body?.success ?: true)
        assertEquals("Erro ao enviar e-mail", response.body?.message)
        assertEquals(
            "Entrar em contato com o administrador (lmts@ufape.edu.br)",
            response.body
                ?.errors
                ?.get(0)
                ?.detail,
        )
    }

    @Test
    fun `handleHttpMessageNotReadableException deve retornar BAD_REQUEST para MissingKotlinParameterException`() {
        val ex = mock(HttpMessageNotReadableException::class.java)
        `when`(ex.message).thenReturn("MissingKotlinParameterException: some field")

        val response = handler.handleHttpMessageNotReadableException(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(
            "Campos obrigatórios estão ausentes no corpo da requisição.",
            response.body
                ?.errors
                ?.get(0)
                ?.detail,
        )
    }

    @Test
    fun `handleHttpMessageNotReadableException deve retornar BAD_REQUEST para outros erros`() {
        val ex = mock(HttpMessageNotReadableException::class.java)
        `when`(ex.message).thenReturn("Invalid JSON")

        val response = handler.handleHttpMessageNotReadableException(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(
            "O corpo da requisição é inválido ou malformado.",
            response.body
                ?.errors
                ?.get(0)
                ?.detail,
        )
    }

    @Test
    fun `handleAuthenticationException deve retornar UNAUTHORIZED`() {
        val ex = object : AuthenticationException("Falha na autenticação") {}
        val response = handler.handleAuthenticationException(ex)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("Falha na autenticação. Verifique suas credenciais.", response.body?.message)
    }

    @Test
    fun `handleAccessDeniedException deve retornar FORBIDDEN`() {
        val ex = AccessDeniedException("Acesso negado")
        val response = handler.handleAccessDeniedException(ex)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals("Acesso negado. Você não tem permissão para realizar esta ação.", response.body?.message)
    }

    @Test
    fun `handleAgroFeiraException deve retornar status da excecao`() {
        val ex = ResourceNotFoundException("Usuario", "123")
        val response = handler.handleAgroFeiraException(ex)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Usuario com ID 123 não encontrado", response.body?.message)
        assertEquals(
            "RESOURCE_NOT_FOUND",
            response.body
                ?.errors
                ?.get(0)
                ?.rule,
        )
    }

    @Test
    fun `handleRuntimeException deve retornar NOT_FOUND quando mensagem contem nao encontrado`() {
        val ex = RuntimeException("Objeto não encontrado")
        val response = handler.handleRuntimeException(ex)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `handleRuntimeException deve retornar INTERNAL_SERVER_ERROR por padrao`() {
        val ex = RuntimeException("Erro genérico")
        val response = handler.handleRuntimeException(ex)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }

    @Test
    fun `handleIllegalArgumentException deve retornar BAD_REQUEST`() {
        val ex = IllegalArgumentException("Argumento inválido")
        val response = handler.handleIllegalArgumentException(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(
            "Argumento inválido",
            response.body
                ?.errors
                ?.get(0)
                ?.detail,
        )
    }

    @Test
    fun `handleValidationException deve retornar BAD_REQUEST com detalhes dos erros`() {
        val bindingResult = mock(BindingResult::class.java)
        val fieldError = FieldError("object", "field", "must not be null")
        `when`(bindingResult.fieldErrors).thenReturn(listOf(fieldError))

        val ex = MethodArgumentNotValidException(mock(MethodParameter::class.java), bindingResult)
        val response = handler.handleValidationException(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Erro de validação nos campos", response.body?.message)
        assertEquals(
            "field",
            response.body
                ?.errors
                ?.get(0)
                ?.field,
        )
        assertEquals(
            "must not be null",
            response.body
                ?.errors
                ?.get(0)
                ?.detail,
        )
    }

    @Test
    fun `handleDataIntegrityException deve retornar CONFLICT para email duplicado`() {
        val rootCause = RuntimeException("detail: usuarios_email_key violation")
        val ex = DataIntegrityViolationException("Integrity error", rootCause)

        val response = handler.handleDataIntegrityException(ex)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals(
            "E-mail já está em uso por outro usuário.",
            response.body
                ?.errors
                ?.get(0)
                ?.detail,
        )
    }

    @Test
    fun `handleDataIntegrityException deve retornar CONFLICT para telefone duplicado`() {
        val rootCause = RuntimeException("detail: usuarios_telefone_key violation")
        val ex = DataIntegrityViolationException("Integrity error", rootCause)

        val response = handler.handleDataIntegrityException(ex)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals(
            "Telefone já está em uso por outro usuário.",
            response.body
                ?.errors
                ?.get(0)
                ?.detail,
        )
    }

    @Test
    fun `handleException deve retornar INTERNAL_SERVER_ERROR`() {
        val ex = Exception("Erro inesperado")
        val response = handler.handleException(ex)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Erro interno do servidor", response.body?.message)
    }
}
