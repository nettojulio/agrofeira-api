package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.PasswordResetToken
import br.edu.ufape.agrofeira.domain.entity.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, UUID> {
    fun findByToken(token: String): Optional<PasswordResetToken>

    @Modifying
    @Transactional
    fun deleteByUsuario(usuario: Usuario)

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiracao < :now OR p.usado = true")
    fun deleteAllExpiredOrUsedSince(now: Instant): Int
}
