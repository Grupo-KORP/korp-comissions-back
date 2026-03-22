package com.comissions.korp.service;

import com.comissions.korp.DTO.ContatoResponseDTO;
import com.comissions.korp.DTO.DistribuidorRequestDTO;
import com.comissions.korp.DTO.DistribuidorResponseDTO;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.DistribuidorRespository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DistribuidorService {

    private final DistribuidorRespository distribuidorRespository;

    public DistribuidorService(DistribuidorRespository distribuidorRespository) {
        this.distribuidorRespository = distribuidorRespository;
    }

    public DistribuidorResponseDTO criar(DistribuidorRequestDTO requestDTO) {
        Distribuidor distribuidor = convertToEntity(requestDTO);
        Distribuidor distribuidorSalvo = distribuidorRespository.save(distribuidor);
        return convertToResponseDTO(distribuidorSalvo);
    }

    public List<DistribuidorResponseDTO> listarTodos() {
        return distribuidorRespository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public DistribuidorResponseDTO buscarPorId(Integer id) {
        Distribuidor distribuidor = distribuidorRespository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Distribuidor não encontrado com ID: " + id));
        return convertToResponseDTO(distribuidor);
    }

    public DistribuidorResponseDTO buscarPorCnpj(String cnpj) {
        Distribuidor distribuidor = distribuidorRespository.findByCnpj(cnpj)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Distribuidor não encontrado com CNPJ: " + cnpj));
        return convertToResponseDTO(distribuidor);
    }

    public DistribuidorResponseDTO atualizar(Integer id, DistribuidorRequestDTO requestDTO) {
        Distribuidor distribuidorExistente = distribuidorRespository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Distribuidor não encontrado com ID: " + id));

        // Atualiza os campos usando dados do DTO
        distribuidorExistente.setRazaoSocial(requestDTO.getRazaoSocial());
        distribuidorExistente.setNomeFantasia(requestDTO.getNomeFantasia());
        distribuidorExistente.setCnpj(requestDTO.getCnpj());
        distribuidorExistente.setTelefone(requestDTO.getTelefone());
        distribuidorExistente.setEmail(requestDTO.getEmail());

        Distribuidor distribuidorAtualizado = distribuidorRespository.save(distribuidorExistente);
        return convertToResponseDTO(distribuidorAtualizado);
    }

    public void deletar(Integer id) {
        if (!distribuidorRespository.existsById(id)) {
            throw new RecursoNaoEncontrado("Distribuidor não encontrado com ID: " + id);
        }
        distribuidorRespository.deleteById(id);
    }

    private Distribuidor convertToEntity(DistribuidorRequestDTO dto) {
        Distribuidor distribuidor = new Distribuidor();
        distribuidor.setRazaoSocial(dto.getRazaoSocial());
        distribuidor.setNomeFantasia(dto.getNomeFantasia());
        distribuidor.setCnpj(dto.getCnpj());
        distribuidor.setTelefone(dto.getTelefone());
        distribuidor.setEmail(dto.getEmail());
        return distribuidor;
    }

    private DistribuidorResponseDTO convertToResponseDTO(Distribuidor distribuidor) {
        DistribuidorResponseDTO dto = new DistribuidorResponseDTO();
        dto.setIdDistribuidor(distribuidor.getIdDistribuidor());
        dto.setRazaoSocial(distribuidor.getRazaoSocial());
        dto.setNomeFantasia(distribuidor.getNomeFantasia());
        dto.setCnpj(distribuidor.getCnpj());
        dto.setTelefone(distribuidor.getTelefone());
        dto.setEmail(distribuidor.getEmail());

        // Converte lista de contatos usando Stream
        List<ContatoResponseDTO> contatos = distribuidor.getContatos() != null
                ? distribuidor.getContatos().stream()
                .map(contato -> new ContatoResponseDTO(
                        contato.getIdContato(),
                        contato.getNome(),
                        contato.getEmail(),
                        contato.getTelefone(),
                        contato.getCliente() != null ? contato.getCliente().getIdCliente() : null,
                        contato.getDistribuidor() != null ? contato.getDistribuidor().getIdDistribuidor() : null
                ))
                .collect(Collectors.toList())
                : Collections.emptyList();

        dto.setContatos(contatos);
        return dto;
    }


}
