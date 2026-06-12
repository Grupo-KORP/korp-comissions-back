package com.comissions.korp.service;

import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorRequestDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorResponseDTO;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.Endereco;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.UsuarioJaExistente;
import com.comissions.korp.repository.ClienteRepository;
import com.comissions.korp.repository.DistribuidorRepository;
import com.comissions.korp.repository.EnderecoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class DistribuidorServiceTest {

    @Mock
    private DistribuidorRepository distribuidorRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ContatoService contatoService;
    @Mock
    private EnderecoService enderecoService;
    @Mock
    private EnderecoRepository enderecoRepository;

    private DistribuidorService distribuidorService;

    @BeforeEach
    void setUp() {
        distribuidorService = new DistribuidorService(distribuidorRepository, clienteRepository, contatoService);
        ReflectionTestUtils.setField(distribuidorService, "enderecoService", enderecoService);
        ReflectionTestUtils.setField(distribuidorService, "enderecoRepository", enderecoRepository);
    }

    @Test
    void criar_deveCadastrarDistribuidorEnderecoEContato_quandoDadosSaoValidos() {
        // Arrange
        DistribuidorRequestDTO request = distribuidorRequest();
        Endereco endereco = endereco();

        when(distribuidorRepository.existsByCnpj(request.getCnpj())).thenReturn(false);
        when(clienteRepository.existsByCnpj(request.getCnpj())).thenReturn(false);
        when(distribuidorRepository.existsByTelefone(request.getTelefone())).thenReturn(false);
        when(distribuidorRepository.save(any(Distribuidor.class))).thenAnswer(invocation -> {
            Distribuidor distribuidor = invocation.getArgument(0);
            distribuidor.setIdDistribuidor(20);
            return distribuidor;
        });
        when(enderecoService.convertToEntityFromDistribuidorRequest(request, 20)).thenReturn(endereco);
        when(contatoService.buscarPorDistribuidorToDto(any(Distribuidor.class))).thenReturn(List.of());

        // Act
        DistribuidorResponseDTO response = distribuidorService.criar(request);

        // Assert
        assertThat(response.getIdDistribuidor()).isEqualTo(20);
        assertThat(response.getCnpj()).isEqualTo(request.getCnpj());
        assertThat(response.getNomeFantasia()).isEqualTo(request.getNomeFantasia());
        verify(enderecoService).criarEndereco(endereco);
        verify(contatoService).criarFromDistribuidorDTO(request, 20);
    }

    @Test
    void criar_deveFalhar_quandoJaExisteDistribuidorComMesmoCnpj() {
        // Arrange
        DistribuidorRequestDTO request = distribuidorRequest();
        when(distribuidorRepository.existsByCnpj(request.getCnpj())).thenReturn(true);

        // Act / Assert
        assertThatThrownBy(() -> distribuidorService.criar(request))
                .isInstanceOf(UsuarioJaExistente.class)
                .hasMessageContaining(request.getCnpj());

        verify(distribuidorRepository, never()).save(any());
        verify(enderecoService, never()).criarEndereco(any());
    }

    @Test
    void criar_deveFalhar_quandoCnpjJaPertenceACliente() {
        // Arrange
        DistribuidorRequestDTO request = distribuidorRequest();
        when(distribuidorRepository.existsByCnpj(request.getCnpj())).thenReturn(false);
        when(clienteRepository.existsByCnpj(request.getCnpj())).thenReturn(true);

        // Act / Assert
        assertThatThrownBy(() -> distribuidorService.criar(request))
                .isInstanceOf(UsuarioJaExistente.class)
                .hasMessageContaining("cliente");

        verify(distribuidorRepository, never()).save(any());
    }

    @Test
    void atualizar_deveAlterarDadosEAtualizarEnderecoExistente() {
        // Arrange
        Distribuidor distribuidor = distribuidor(20);
        DistribuidorRequestDTO request = distribuidorRequest();
        request.setNomeFantasia("Distribuidor Atualizado");
        request.setAtivo(false);
        Endereco enderecoExistente = endereco();

        when(distribuidorRepository.findById(20)).thenReturn(Optional.of(distribuidor));
        when(distribuidorRepository.save(distribuidor)).thenReturn(distribuidor);
        when(enderecoRepository.findByDistribuidor_IdDistribuidor(20)).thenReturn(List.of(enderecoExistente));
        when(contatoService.buscarPorDistribuidorToDto(distribuidor)).thenReturn(List.of());

        // Act
        DistribuidorResponseDTO response = distribuidorService.atualizar(20, request);

        // Assert
        assertThat(response.getNomeFantasia()).isEqualTo("Distribuidor Atualizado");
        assertThat(distribuidor.getAtivo()).isFalse();
        assertThat(enderecoExistente.getLogradouro()).isEqualTo(request.getEndereco());
        assertThat(enderecoExistente.getCidade()).isEqualTo(request.getCidade());
        verify(enderecoRepository).save(enderecoExistente);
    }

    @Test
    void deletar_deveInativarDistribuidor_quandoDistribuidorExiste() {
        // Arrange
        Distribuidor distribuidor = distribuidor(20);
        when(distribuidorRepository.findById(20)).thenReturn(Optional.of(distribuidor));

        // Act
        distribuidorService.deletar(20);

        // Assert
        assertThat(distribuidor.getAtivo()).isFalse();
        verify(distribuidorRepository).save(distribuidor);
    }

    @Test
    void buscarDistribuidorPorId_deveFalhar_quandoDistribuidorNaoExiste() {
        // Arrange
        when(distribuidorRepository.findById(404)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> distribuidorService.buscarDistribuidorPorId(404))
                .isInstanceOf(RecursoNaoEncontrado.class)
                .hasMessageContaining("404");
    }

    private DistribuidorRequestDTO distribuidorRequest() {
        DistribuidorRequestDTO request = new DistribuidorRequestDTO();
        request.setRazaoSocial("Distribuidor Razao");
        request.setNomeFantasia("Distribuidor Fantasia");
        request.setCnpj("55666777000188");
        request.setTelefone("11888887777");
        request.setInscricaoEstadual("IE-2");
        request.setEndereco("Avenida B");
        request.setNumero("456");
        request.setBairro("Industrial");
        request.setCidade("Campinas");
        request.setUf("SP");
        request.setCep("13000-000");
        return request;
    }

    private Distribuidor distribuidor(Integer id) {
        Distribuidor distribuidor = new Distribuidor();
        distribuidor.setIdDistribuidor(id);
        distribuidor.setRazaoSocial("Distribuidor Razao");
        distribuidor.setNomeFantasia("Distribuidor Fantasia");
        distribuidor.setCnpj("55666777000188");
        distribuidor.setTelefone("11888887777");
        distribuidor.setInscricaoEstadual("IE-2");
        distribuidor.setAtivo(true);
        return distribuidor;
    }

    private Endereco endereco() {
        Endereco endereco = new Endereco();
        endereco.setLogradouro("Endereco Antigo");
        endereco.setNumero("1");
        endereco.setBairro("Bairro");
        endereco.setCidade("Cidade");
        endereco.setEstado("SP");
        endereco.setCep("13000-000");
        return endereco;
    }
}
