package br.edu.ufape.agrofeira

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class AgroFeiraApplication

fun main(args: Array<String>) {
    runApplication<AgroFeiraApplication>(*args)
}
