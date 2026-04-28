package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Endereco
import br.edu.ufape.agrofeira.dto.request.EnderecoRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.EnderecoRepository
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.repository.ZonaEntregaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class EnderecoService(
    private val repository: EnderecoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val zonaEntregaRepository: ZonaEntregaRepository,
) {
    @Transactional(readOnly = true)
    fun buscarPorUsuarioId(usuarioId: UUID): Endereco =
        repository
            .findById(usuarioId)
            .orElseThrow { ResourceNotFoundException("Endereço", usuarioId.toString()) }

    @Transactional
    fun salvarEndereco(
        usuarioId: UUID,
        request: EnderecoRequest,
    ): Endereco {
        val usuario =
            usuarioRepository
                .findById(usuarioId)
                .orElseThrow { ResourceNotFoundException("Usuário", usuarioId.toString()) }

        val zonaEntrega =
            zonaEntregaRepository
                .findById(request.zonaEntregaId)
                .orElseThrow { ResourceNotFoundException("Zona de Entrega", request.zonaEntregaId.toString()) }

        if (!zonaEntrega.ativo) {
            throw IllegalArgumentException("A Zona de Entrega selecionada está inativa")
        }

        val endereco =
            repository.findById(usuarioId).orElseGet {
                Endereco(usuario = usuario)
            }

        endereco.rua = request.rua
        endereco.numero = request.numero
        endereco.complemento = request.complemento
        endereco.cidade = request.cidade
        endereco.estado = request.estado
        endereco.cep = request.cep
        endereco.zonaEntrega = zonaEntrega
        endereco.atualizadoEm = LocalDateTime.now()

        return repository.save(endereco)
    }

    @Transactional
    fun deletarEndereco(usuarioId: UUID) {
        val endereco = buscarPorUsuarioId(usuarioId)
        repository.delete(endereco)
    }
}
