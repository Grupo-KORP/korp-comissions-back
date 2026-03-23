package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteRequestDTO;
import com.comissions.korp.DTO.ClienteResponseDTO;
import com.comissions.korp.DTO.ContatoResponseDTO;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Cria um novo Cliente
     */
    public ClienteResponseDTO criar(ClienteRequestDTO requestDTO) {
        Cliente cliente = convertToEntity(requestDTO);
        Cliente clienteSalvo = clienteRepository.save(cliente);
        return convertToResponseDTO(clienteSalvo);
    }

    /**
     * Lista todos os Clientes
     */
    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca um Cliente por ID
     */
    public ClienteResponseDTO buscarPorId(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Cliente não encontrado com ID: " + id));
        return convertToResponseDTO(cliente);
    }

    /**
     * Busca um Cliente por CNPJ
     */
    public ClienteResponseDTO buscarPorCnpj(String cnpj) {
        Cliente cliente = clienteRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Cliente não encontrado com CNPJ: " + cnpj));
        return convertToResponseDTO(cliente);
    }

    /**
     * Atualiza um Cliente existente
     */
    public ClienteResponseDTO atualizar(Integer id, ClienteRequestDTO requestDTO) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Cliente não encontrado com ID: " + id));

        // Atualiza os campos usando dados do DTO
        clienteExistente.setRazaoSocial(requestDTO.getRazaoSocial());
        clienteExistente.setNomeFantasia(requestDTO.getNomeFantasia());
        clienteExistente.setCnpj(requestDTO.getCnpj());
        clienteExistente.setTelefone(requestDTO.getTelefone());
        clienteExistente.setEmail(requestDTO.getEmail());

        Cliente clienteAtualizado = clienteRepository.save(clienteExistente);
        return convertToResponseDTO(clienteAtualizado);
    }

    /**
     * Deleta um Cliente por ID
     */
    public void deletar(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new RecursoNaoEncontrado("Cliente não encontrado com ID: " + id);
        }
        clienteRepository.deleteById(id);
    }

    /**
     * Converte ClienteRequestDTO para Entity
     */
    private Cliente convertToEntity(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setRazaoSocial(dto.getRazaoSocial());
        cliente.setNomeFantasia(dto.getNomeFantasia());
        cliente.setCnpj(dto.getCnpj());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEmail(dto.getEmail());
        return cliente;
    }

    /**
     * Converte Entity para ClienteResponseDTO usando Streams
     */
    private ClienteResponseDTO convertToResponseDTO(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setIdCliente(cliente.getIdCliente());
        dto.setRazaoSocial(cliente.getRazaoSocial());
        dto.setNomeFantasia(cliente.getNomeFantasia());
        dto.setCnpj(cliente.getCnpj());
        dto.setTelefone(cliente.getTelefone());
        dto.setEmail(cliente.getEmail());

        return dto;
    }
}
