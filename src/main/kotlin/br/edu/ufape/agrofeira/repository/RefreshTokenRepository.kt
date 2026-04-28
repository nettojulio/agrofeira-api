package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.RefreshToken
import br.edu.ufape.agrofeira.domain.entity.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByToken(token: String): Optional<RefreshToken>

    @Modifying
    @Transactional
    fun deleteByUsuario(usuario: Usuario)

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.expiracao < :now OR r.revogado = true")
    fun deleteAllExpiredOrRevokedSince(now: Instant): Int
}
