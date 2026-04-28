package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Perfil
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.dto.request.ClienteRequest
import br.edu.ufape.agrofeira.dto.request.ClienteUpdateRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

@ExtendWith(MockitoExtension::class)
class ClienteServiceTest {
    @Mock
    private lateinit var usuarioRepository: UsuarioRepository

    @Mock
    private lateinit var usuarioService: UsuarioService

    @InjectMocks
    private lateinit var clienteService: ClienteService

    private lateinit var cliente: Usuario
    private lateinit var perfilConsumidor: Perfil
    private val id = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        perfilConsumidor = Perfil(nome = "CONSUMIDOR")
        cliente =
            Usuario(
                id = id,
                nome = "Maria",
                email = "maria@email.com",
                senhaHash = "123456",
                perfis = mutableSetOf(perfilConsumidor),
            )
    }

    @Test
    fun `listar deve retornar uma pagina de clientes`() {
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(cliente))

        `when`(usuarioRepository.findByPerfilNome("CONSUMIDOR", pageable)).thenReturn(page)

        val result = clienteService.listar(pageable)

        assertEquals(1, result.totalElements)
        assertEquals("Maria", result.content[0].nome)
        verify(usuarioRepository).findByPerfilNome("CONSUMIDOR", pageable)
    }

    @Test
    fun `buscarPorId deve retornar o cliente se for consumidor`() {
        `when`(usuarioRepository.findById(id)).thenReturn(Optional.of(cliente))

        val result = clienteService.buscarPorId(id)

        assertNotNull(result)
        assertEquals(cliente.id, result.id)
    }

    @Test
    fun `buscarPorId deve lancar excecao se nao for consumidor`() {
        val adminPerfil = Perfil(nome = "ADMINISTRADOR")
        val admin = cliente.copy(perfis = mutableSetOf(adminPerfil))

        `when`(usuarioRepository.findById(id)).thenReturn(Optional.of(admin))

        assertThrows(ResourceNotFoundException::class.java) {
            clienteService.buscarPorId(id)
        }
    }

    private fun anyUsuario(): Usuario {
        any(Usuario::class.java)
        return Usuario(nome = "", senhaHash = "")
    }

    private fun eqSet(value: Set<String>): Set<String> {
        eq(value)
        return value
    }

    @Test
    fun `criar deve chamar o usuarioService cadastrar repassando CONSUMIDOR`() {
        val request = ClienteRequest("João", "joao@email.com", "8799999999", "senha123", "desc")
        `when`(usuarioService.cadastrar(anyUsuario(), eqSet(setOf("CONSUMIDOR")))).thenReturn(cliente)

        val result = clienteService.criar(request)

        assertNotNull(result)
        verify(usuarioService).cadastrar(anyUsuario(), eqSet(setOf("CONSUMIDOR")))
    }

    @Test
    fun `atualizar deve salvar usuario com os novos dados`() {
        val request = ClienteUpdateRequest("Maria Atualizada", "maria.nova@email.com", "8799999888", "desc")

        `when`(usuarioRepository.findById(id)).thenReturn(Optional.of(cliente))
        `when`(usuarioRepository.save(anyUsuario())).thenAnswer { it.arguments[0] }

        val result = clienteService.atualizar(id, request)

        assertEquals("Maria Atualizada", result.nome)
        assertEquals("maria.nova@email.com", result.email)
        assertEquals("8799999888", result.telefone)
        assertEquals("desc", result.descricao)
        verify(usuarioRepository).save(anyUsuario())
    }

    @Test
    fun `deletar deve chamar deletarLogicamente do repository`() {
        `when`(usuarioRepository.findById(id)).thenReturn(Optional.of(cliente))

        clienteService.deletar(id)

        verify(usuarioRepository).deletarLogicamente(id)
    }
}
