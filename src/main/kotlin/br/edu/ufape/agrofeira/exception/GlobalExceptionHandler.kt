package br.edu.ufape.agrofeira.exception

import br.edu.ufape.agrofeira.dto.response.ApiErrorDetail
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mail.MailSendException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MailSendException::class)
    fun handleMailSendException(ex: MailSendException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse(
                success = false,
                message = ex.message ?: "Falha no envio de e-mail",
                errors = listOf(ApiErrorDetail(detail = "Entrar em contato com o administrador (lmts@ufape.edu.br)")),
            ),
        )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> {
        val detail =
            if (ex.message?.contains("MissingKotlinParameterException", ignoreCase = true) == true) {
                "Campos obrigatórios estão ausentes no corpo da requisição."
            } else {
                "O corpo da requisição é inválido ou malformado."
            }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse(
                success = false,
                message = "Requisição inválida",
                errors = listOf(ApiErrorDetail(detail = detail)),
            ),
        )
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ApiResponse(
                success = false,
                message = "Falha na autenticação. Verifique suas credenciais.",
                errors = listOf(ApiErrorDetail(detail = ex.message ?: "Credenciais inválidas")),
            ),
        )

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiResponse(
                success = false,
                message = "Acesso negado. Você não tem permissão para realizar esta ação.",
                errors = listOf(ApiErrorDetail(detail = ex.message ?: "Permissão insuficiente")),
            ),
        )

    @ExceptionHandler(AgroFeiraException::class)
    fun handleAgroFeiraException(ex: AgroFeiraException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(ex.status).body(
            ApiResponse(
                success = false,
                message = ex.message,
                errors = listOf(ApiErrorDetail(rule = ex.rule, detail = ex.message)),
            ),
        )

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ApiResponse<Nothing>> {
        val status =
            when {
                ex.message?.contains("não encontrado", ignoreCase = true) == true -> HttpStatus.NOT_FOUND
                ex.message?.contains("não encontrada", ignoreCase = true) == true -> HttpStatus.NOT_FOUND
                else -> HttpStatus.INTERNAL_SERVER_ERROR
            }
        return ResponseEntity.status(status).body(
            ApiResponse(
                success = false,
                message = "Ocorreu um erro ao processar a requisição",
                errors = listOf(ApiErrorDetail(detail = ex.message ?: "Erro interno")),
            ),
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse(
                success = false,
                message = ex.message ?: "Requisição inválida",
                errors = listOf(ApiErrorDetail(detail = ex.message ?: "Argumento inválido")),
            ),
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errors =
            ex.bindingResult.fieldErrors.map {
                ApiErrorDetail(
                    field = it.field,
                    rule = it.code,
                    detail = it.defaultMessage ?: "Valor inválido",
                )
            }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse(
                success = false,
                message = "Erro de validação nos campos",
                errors = errors,
            ),
        )
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException::class)
    fun handleDataIntegrityException(ex: org.springframework.dao.DataIntegrityViolationException): ResponseEntity<ApiResponse<Nothing>> {
        val rootCause = ex.mostSpecificCause.message ?: ""
        val detail =
            when {
                rootCause.contains(
                    "usuarios_email_key",
                    ignoreCase = true,
                ) -> "E-mail já está em uso por outro usuário."

                rootCause.contains(
                    "usuarios_telefone_key",
                    ignoreCase = true,
                ) -> "Telefone já está em uso por outro usuário."

                else -> "Não foi possível concluir a operação devido a um conflito de dados."
            }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiResponse(
                success = false,
                message = "Erro de integridade de dados",
                errors = listOf(ApiErrorDetail(detail = detail)),
            ),
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse(
                success = false,
                message = "Erro interno do servidor",
                errors = listOf(ApiErrorDetail(detail = "Um erro inesperado ocorreu. Por favor, tente novamente mais tarde.")),
            ),
        )
}
