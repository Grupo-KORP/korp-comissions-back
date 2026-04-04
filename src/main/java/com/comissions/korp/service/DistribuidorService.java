package com.comissions.korp.service;

import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorRequestDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorResponseDTO;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.DistribuidorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DistribuidorService {

    private final DistribuidorRepository distribuidorRepository;

    public DistribuidorService(DistribuidorRepository distribuidorRepository) {
        this.distribuidorRepository = distribuidorRepository;
    }

    public DistribuidorResponseDTO criar(DistribuidorRequestDTO requestDTO) {
        Distribuidor distribuidor = convertToEntity(requestDTO);
        Distribuidor distribuidorSalvo = distribuidorRepository.save(distribuidor);
        return convertToResponseDTO(distribuidorSalvo);
    }

    public List<DistribuidorResponseDTO> listarTodos() {
        return distribuidorRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public DistribuidorResponseDTO buscarPorId(Integer id) {
        Distribuidor distribuidor = distribuidorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Distribuidor não encontrado com ID: " + id));
        return convertToResponseDTO(distribuidor);
    }

    public DistribuidorResponseDTO buscarPorCnpj(String cnpj) {
        Distribuidor distribuidor = distribuidorRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Distribuidor não encontrado com CNPJ: " + cnpj));
        return convertToResponseDTO(distribuidor);
    }

    public DistribuidorResponseDTO atualizar(Integer id, DistribuidorRequestDTO requestDTO) {
        Distribuidor distribuidorExistente = distribuidorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Distribuidor não encontrado com ID: " + id));

        // Atualiza os campos usando dados do DTO
        distribuidorExistente.setRazaoSocial(requestDTO.getRazaoSocial());
        distribuidorExistente.setNomeFantasia(requestDTO.getNomeFantasia());
        distribuidorExistente.setCnpj(requestDTO.getCnpj());
        distribuidorExistente.setTelefone(requestDTO.getTelefone());
        distribuidorExistente.setEmail(requestDTO.getEmail());

        Distribuidor distribuidorAtualizado = distribuidorRepository.save(distribuidorExistente);
        return convertToResponseDTO(distribuidorAtualizado);
    }

    public void deletar(Integer id) {
        if (!distribuidorRepository.existsById(id)) {
            throw new RecursoNaoEncontrado("Distribuidor não encontrado com ID: " + id);
        }
        distribuidorRepository.deleteById(id);
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
        return dto;
    }
}
