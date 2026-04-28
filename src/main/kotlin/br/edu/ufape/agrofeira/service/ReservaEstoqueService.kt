package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.repository.OfertaEstoqueRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
class ReservaEstoqueService(
    private val ofertaEstoqueRepository: OfertaEstoqueRepository,
) {
    @Transactional
    fun reservar(
        ofertaId: UUID,
        quantidade: BigDecimal,
    ): Boolean {
        val linhasAfetadas = ofertaEstoqueRepository.reservarEstoqueAtomicamente(ofertaId, quantidade)
        return linhasAfetadas > 0
    }

    @Transactional
    fun liberar(
        ofertaId: UUID,
        quantidade: BigDecimal,
    ): Boolean {
        val linhasAfetadas = ofertaEstoqueRepository.liberarReservaAtomicamente(ofertaId, quantidade)
        return linhasAfetadas > 0
    }
}
