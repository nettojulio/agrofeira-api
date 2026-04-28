package br.edu.ufape.agrofeira.config

import br.edu.ufape.agrofeira.repository.PasswordResetTokenRepository
import br.edu.ufape.agrofeira.repository.RefreshTokenRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class TokenCleanupSchedulerTest {
    @Mock
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Mock
    private lateinit var passwordResetTokenRepository: PasswordResetTokenRepository

    @InjectMocks
    private lateinit var scheduler: TokenCleanupScheduler

    /**
     * Helper para usar matchers do Mockito com tipos não-nulos do Kotlin.
     */
    private fun <T> anyKotlin(): T {
        any<T>()
        @Suppress("UNCHECKED_CAST")
        return Instant.now() as T
    }

    @Test
    fun `cleanUpTokens deve chamar os repositorios para deletar tokens obsoletos`() {
        `when`(refreshTokenRepository.deleteAllExpiredOrRevokedSince(anyKotlin())).thenReturn(5)
        `when`(passwordResetTokenRepository.deleteAllExpiredOrUsedSince(anyKotlin())).thenReturn(3)

        scheduler.cleanUpTokens()

        verify(refreshTokenRepository).deleteAllExpiredOrRevokedSince(anyKotlin())
        verify(passwordResetTokenRepository).deleteAllExpiredOrUsedSince(anyKotlin())
    }
}
