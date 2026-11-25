package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteDTO.ClienteRequestDTO;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Endereco;
import com.comissions.korp.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private EnderecoService enderecoService;

    @Test
    void convertToEntityFromClienteRequest_deveManterOBairroInformado() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(10);
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));

        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setEndereco("Rua A");
        request.setNumero("123");
        request.setBairro("Centro");
        request.setCidade("Sao Paulo");
        request.setUf("SP");
        request.setCep("01000000");

        Endereco endereco = enderecoService.convertToEntityFromClienteRequest(request, 10);

        assertThat(endereco.getBairro()).isEqualTo("Centro");
        assertThat(endereco.getCliente()).isSameAs(cliente);
    }
}
