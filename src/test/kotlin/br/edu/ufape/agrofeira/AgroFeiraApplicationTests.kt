package br.edu.ufape.agrofeira

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AgroFeiraApplicationTests {
    companion object {
        @Container
        @ServiceConnection
        val postgres =
            PostgreSQLContainer("postgres:18-alpine").apply {
                withDatabaseName("testdb")
                withUsername("test")
                withPassword("test")
            }
    }

    @Test
    fun contextLoads() {
    }
}
