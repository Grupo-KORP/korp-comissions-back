package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteDTO.ClienteRequestDTO;
import com.comissions.korp.DTO.ClienteDTO.ClienteResponseDTO;
import com.comissions.korp.config.utils.SecurityUtils;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Endereco;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.UsuarioJaExistente;
import com.comissions.korp.repository.ClienteRepository;
import com.comissions.korp.repository.ContatoRepository;
import com.comissions.korp.repository.DistribuidorRepository;
import com.comissions.korp.repository.EnderecoRepository;
import com.comissions.korp.repository.PedidoRepository;
import com.comissions.korp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private DistribuidorRepository distribuidorRepository;
    @Mock
    private ContatoService contatoService;
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private EnderecoService enderecoService;
    @Mock
    private EnderecoRepository enderecoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ContatoRepository contatoRepository;

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(clienteRepository, distribuidorRepository, contatoService);
        ReflectionTestUtils.setField(clienteService, "pedidoRepository", pedidoRepository);
        ReflectionTestUtils.setField(clienteService, "securityUtils", securityUtils);
        ReflectionTestUtils.setField(clienteService, "enderecoService", enderecoService);
        ReflectionTestUtils.setField(clienteService, "enderecoRepository", enderecoRepository);
        ReflectionTestUtils.setField(clienteService, "usuarioRepository", usuarioRepository);
        ReflectionTestUtils.setField(clienteService, "contatoRepository", contatoRepository);
    }

    @Test
    void criar_deveCadastrarClienteEnderecoEContato_quandoDadosSaoValidos() {
        // Arrange
        ClienteRequestDTO request = clienteRequest();
        Usuario vendedor = usuario(99);
        Endereco endereco = endereco();

        when(clienteRepository.existsByCnpj(request.getCnpj())).thenReturn(false);
        when(distribuidorRepository.existsByCnpj(request.getCnpj())).thenReturn(false);
        when(clienteRepository.existsByTelefone(request.getTelefone())).thenReturn(false);
        when(securityUtils.getUsuarioIdAutenticado()).thenReturn(99);
        when(usuarioRepository.findById(99)).thenReturn(Optional.of(vendedor));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setIdCliente(10);
            return cliente;
        });
        when(enderecoService.convertToEntityFromClienteRequest(request, 10)).thenReturn(endereco);
        when(pedidoRepository.countByCliente(any(Cliente.class))).thenReturn(0);
        when(contatoService.buscarPorClienteToDto(any(Cliente.class))).thenReturn(List.of());

        // Act
        ClienteResponseDTO response = clienteService.criar(request);

        // Assert
        assertThat(response.getIdCliente()).isEqualTo(10);
        assertThat(response.getCnpj()).isEqualTo(request.getCnpj());
        assertThat(response.getNomeFantasia()).isEqualTo(request.getNomeFantasia());

        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(clienteCaptor.capture());
        assertThat(clienteCaptor.getValue().getVendedorCadastro()).isSameAs(vendedor);
        verify(enderecoService).criarEndereco(endereco);
        verify(contatoService).criarFromClienteDTO(request, 10);
    }

    @Test
    void criar_deveFalhar_quandoJaExisteClienteComMesmoCnpj() {
        // Arrange
        ClienteRequestDTO request = clienteRequest();
        when(clienteRepository.existsByCnpj(request.getCnpj())).thenReturn(true);

        // Act / Assert
        assertThatThrownBy(() -> clienteService.criar(request))
                .isInstanceOf(UsuarioJaExistente.class)
                .hasMessageContaining(request.getCnpj());

        verify(clienteRepository, never()).save(any());
        verify(enderecoService, never()).criarEndereco(any());
        verify(contatoService, never()).criarFromClienteDTO(any(), any());
    }

    @Test
    void criar_deveFalhar_quandoCnpjJaPertenceADistribuidor() {
        // Arrange
        ClienteRequestDTO request = clienteRequest();
        when(clienteRepository.existsByCnpj(request.getCnpj())).thenReturn(false);
        when(distribuidorRepository.existsByCnpj(request.getCnpj())).thenReturn(true);

        // Act / Assert
        assertThatThrownBy(() -> clienteService.criar(request))
                .isInstanceOf(UsuarioJaExistente.class)
                .hasMessageContaining("distribuidor");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void atualizar_deveAlterarDadosClienteVendedorEEnderecoExistente() {
        // Arrange
        Cliente cliente = cliente(10);
        ClienteRequestDTO request = clienteRequest();
        request.setNomeFantasia("Cliente Atualizado");
        request.setAtivo(false);
        request.setFkVendedorCadastro(77);

        Usuario novoVendedor = usuario(77);
        Endereco enderecoExistente = endereco();

        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(77)).thenReturn(Optional.of(novoVendedor));
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(enderecoService.buscarEnderecoPorCliente(10)).thenReturn(enderecoExistente);
        when(pedidoRepository.countByCliente(cliente)).thenReturn(2);
        when(contatoService.buscarPorClienteToDto(cliente)).thenReturn(List.of());

        // Act
        ClienteResponseDTO response = clienteService.atualizar(10, request);

        // Assert
        assertThat(response.getNomeFantasia()).isEqualTo("Cliente Atualizado");
        assertThat(cliente.getAtivo()).isFalse();
        assertThat(cliente.getVendedorCadastro()).isSameAs(novoVendedor);
        assertThat(enderecoExistente.getLogradouro()).isEqualTo(request.getEndereco());
        assertThat(enderecoExistente.getCidade()).isEqualTo(request.getCidade());
        verify(enderecoRepository).save(enderecoExistente);
    }

    @Test
    void deletar_deveInativarCliente_quandoClienteExiste() {
        // Arrange
        Cliente cliente = cliente(10);
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));

        // Act
        clienteService.deletar(10);

        // Assert
        assertThat(cliente.getAtivo()).isFalse();
        verify(clienteRepository).save(cliente);
    }

    @Test
    void buscarClientePorId_deveFalhar_quandoClienteNaoExiste() {
        // Arrange
        when(clienteRepository.findById(404)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> clienteService.buscarClientePorId(404))
                .isInstanceOf(RecursoNaoEncontrado.class)
                .hasMessageContaining("404");
    }

    private ClienteRequestDTO clienteRequest() {
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setRazaoSocial("Cliente Razao");
        request.setNomeFantasia("Cliente Fantasia");
        request.setCnpj("11222333000144");
        request.setTelefone("11999998888");
        request.setInscricaoEstadual("938123");
        request.setEndereco("Rua A");
        request.setNumero("123");
        request.setBairro("Centro");
        request.setCidade("Sao Paulo");
        request.setUf("SP");
        request.setCep("01000-000");
        return request;
    }

    private Cliente cliente(Integer id) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(id);
        cliente.setRazaoSocial("Cliente Razao");
        cliente.setNomeFantasia("Cliente Fantasia");
        cliente.setCnpj("11222333000144");
        cliente.setTelefone("11999998888");
        cliente.setInscricaoEstadual("IE-1");
        cliente.setAtivo(true);
        return cliente;
    }

    private Usuario usuario(Integer id) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNome("Vendedor");
        return usuario;
    }

    private Endereco endereco() {
        Endereco endereco = new Endereco();
        endereco.setLogradouro("Rua das Paixões");
        endereco.setNumero("823");
        endereco.setBairro("Bairro");
        endereco.setCidade("Cidade");
        endereco.setEstado("SP");
        endereco.setCep("01000-000");
        return endereco;
    }
}
