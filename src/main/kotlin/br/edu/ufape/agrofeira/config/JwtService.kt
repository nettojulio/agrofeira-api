package br.edu.ufape.agrofeira.config

import br.edu.ufape.agrofeira.domain.entity.Usuario
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.secret:\${JWT_SECRET:ecofeira-secret-key-must-be-at-least-256-bits-long}}")
    private val secret: String,
    @Value("\${jwt.access.expiration:900000}") // Default 15m
    private val accessExpirationMs: Long,
) {
    private val secretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(usuario: Usuario): String {
        val claims = mutableMapOf<String, Any>()
        claims["id"] = usuario.id.toString()
        claims["nome"] = usuario.nome
        usuario.email?.let { claims["email"] = it }
        usuario.telefone?.let { claims["telefone"] = it }
        claims["roles"] = usuario.perfis.map { it.nome }

        return Jwts
            .builder()
            .claims(claims)
            .subject(usuario.email ?: usuario.telefone)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + accessExpirationMs))
            .signWith(secretKey)
            .compact()
    }

    fun generateToken(subject: String): String =
        Jwts
            .builder()
            .subject(subject)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + accessExpirationMs))
            .signWith(secretKey)
            .compact()

    fun extractUsername(token: String): String =
        Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject

    fun isTokenValid(token: String): Boolean =
        try {
            Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
}
