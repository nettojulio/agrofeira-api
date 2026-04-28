package br.edu.ufape.agrofeira.exception

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer
import org.springframework.boot.diagnostics.FailureAnalysis
import java.sql.SQLException

class DatabaseConnectionFailureAnalyzer : AbstractFailureAnalyzer<SQLException>() {
    override fun analyze(
        rootFailure: Throwable,
        cause: SQLException,
    ): FailureAnalysis? {
        val msg = cause.message ?: ""
        if (msg.contains("Connection", ignoreCase = true) ||
            msg.contains("Conexão", ignoreCase = true) ||
            msg.contains("refused", ignoreCase = true) ||
            msg.contains("fatal", ignoreCase = true)
        ) {
            return FailureAnalysis(
                "A aplicação falhou ao iniciar pois não conseguiu estabelecer conexão com o Banco de Dados.\n\nDetalhes Técnicos: $msg",
                "Para resolver este problema, siga os passos abaixo:\n" +
                    "1. Certifique-se de que o banco de dados (ex: PostgreSQL) está em execução ('docker-compose up -d').\n" +
                    "2. Verifique se as credenciais (usuário e senha) no 'application.properties' estão corretas.\n" +
                    "3. Verifique se a URL de conexão (host e porta) aponta para um banco acessível na sua rede.",
                cause,
            )
        }
        return null
    }
}
