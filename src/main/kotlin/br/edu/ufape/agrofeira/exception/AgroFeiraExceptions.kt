package br.edu.ufape.agrofeira.exception

import org.springframework.http.HttpStatus

abstract class AgroFeiraException(
    val status: HttpStatus,
    override val message: String,
    val rule: String? = null,
) : RuntimeException(message)

class ResourceNotFoundException(
    resource: String,
    id: String,
) : AgroFeiraException(HttpStatus.NOT_FOUND, "$resource com ID $id não encontrado", "RESOURCE_NOT_FOUND")

class BusinessRuleException(
    message: String,
    rule: String? = null,
) : AgroFeiraException(HttpStatus.UNPROCESSABLE_ENTITY, message, rule)

class UnauthorizedActionException(
    message: String,
) : AgroFeiraException(HttpStatus.FORBIDDEN, message, "UNAUTHORIZED_ACTION")

class InvalidTokenException(
    message: String,
) : AgroFeiraException(HttpStatus.UNAUTHORIZED, message, "INVALID_TOKEN")
