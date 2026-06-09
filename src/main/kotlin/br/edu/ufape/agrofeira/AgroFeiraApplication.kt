package br.edu.ufape.agrofeira

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AgroFeiraApplication

fun main(args: Array<String>) {
    runApplication<AgroFeiraApplication>(*args)
}
