package br.edu.ufape.agrofeira.config

import br.edu.ufape.agrofeira.repository.PasswordResetTokenRepository
import br.edu.ufape.agrofeira.repository.RefreshTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
@ConditionalOnProperty(value = ["app.scheduling.enabled"], havingValue = "true", matchIfMissing = true)
class TokenCleanupScheduler(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
) {
    private val logger = LoggerFactory.getLogger(TokenCleanupScheduler::class.java)

    /**
     * Limpa tokens de refresh e de recuperação de senha expirados ou revogados/usados do banco de dados.
     * Executa todos os dias às 03:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    fun cleanUpTokens() {
        logger.debug("Iniciando limpeza de tokens obsoletos...")

        val deletedRefreshCount = refreshTokenRepository.deleteAllExpiredOrRevokedSince(Instant.now())
        if (deletedRefreshCount > 0) {
            logger.info("Limpeza de refresh tokens concluída: $deletedRefreshCount tokens removidos.")
        }

        val deletedResetCount = passwordResetTokenRepository.deleteAllExpiredOrUsedSince(Instant.now())
        if (deletedResetCount > 0) {
            logger.info("Limpeza de tokens de recuperação concluída: $deletedResetCount tokens removidos.")
        }
    }
}
