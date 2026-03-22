package com.comissions.korp.service;

import com.comissions.korp.DTO.ContatoRequestDTO;
import com.comissions.korp.DTO.ContatoResponseDTO;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Contato;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.ClienteRepository;
import com.comissions.korp.repository.ContatoRepository;
import com.comissions.korp.repository.DistribuidorRespository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContatoService {

    private final ContatoRepository contatoRepository;
    private final ClienteRepository clienteRepository;
    private final DistribuidorRespository distribuidorRespository;

    public ContatoService(ContatoRepository contatoRepository, ClienteRepository clienteRepository, DistribuidorRespository distribuidorRespository) {
        this.contatoRepository = contatoRepository;
        this.clienteRepository = clienteRepository;
        this.distribuidorRespository = distribuidorRespository;
    }

    public ContatoResponseDTO criar(ContatoRequestDTO requestDTO) {
        Contato contato = convertToEntity(requestDTO);
        Contato contatoSalvo = contatoRepository.save(contato);
        return convertToResponseDTO(contatoSalvo);
    }

    public List<ContatoResponseDTO> listarTodos() {
        return contatoRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public ContatoResponseDTO buscarPorId(Integer id) {
        Contato contato = contatoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Contato não encontrado com ID: " + id));
        return convertToResponseDTO(contato);
    }

    public ContatoResponseDTO atualizar(Integer id, ContatoRequestDTO requestDTO) {
        Contato contatoExistente = contatoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Contato não encontrado com ID: " + id));

        // Atualiza os campos usando dados do DTO
        contatoExistente.setNome(requestDTO.getNome());
        contatoExistente.setEmail(requestDTO.getEmail());
        contatoExistente.setTelefone(requestDTO.getTelefone());

        // Atualiza relacionamentos se fornecidos
        if (requestDTO.getFkCliente() != null) {
            Cliente cliente = clienteRepository.findById(requestDTO.getFkCliente())
                    .orElseThrow(() -> new RecursoNaoEncontrado(
                            "Cliente não encontrado com ID: " + requestDTO.getFkCliente()));
            contatoExistente.setCliente(cliente);
        } else {
            contatoExistente.setCliente(null);
        }

        if (requestDTO.getFkDistribuidor() != null) {
            Distribuidor distribuidor = distribuidorRespository.findById(requestDTO.getFkDistribuidor())
                    .orElseThrow(() -> new RecursoNaoEncontrado(
                            "Distribuidor não encontrado com ID: " + requestDTO.getFkDistribuidor()));
            contatoExistente.setDistribuidor(distribuidor);
        } else {
            contatoExistente.setDistribuidor(null);
        }

        Contato contatoAtualizado = contatoRepository.save(contatoExistente);
        return convertToResponseDTO(contatoAtualizado);
    }

    public void deletar(Integer id) {
        if (!contatoRepository.existsById(id)) {
            throw new RecursoNaoEncontrado("Contato não encontrado com ID: " + id);
        }
        contatoRepository.deleteById(id);
    }

    private Contato convertToEntity(ContatoRequestDTO dto) {
        Contato contato = new Contato();
        contato.setNome(dto.getNome());
        contato.setEmail(dto.getEmail());
        contato.setTelefone(dto.getTelefone());

        // Define relacionamentos se fornecidos
        if (dto.getFkCliente() != null) {
            Cliente cliente = clienteRepository.findById(dto.getFkCliente())
                    .orElseThrow(() -> new RecursoNaoEncontrado(
                            "Cliente não encontrado com ID: " + dto.getFkCliente()));
            contato.setCliente(cliente);
        }

        if (dto.getFkDistribuidor() != null) {
            Distribuidor distribuidor = distribuidorRespository.findById(dto.getFkDistribuidor())
                    .orElseThrow(() -> new RecursoNaoEncontrado(
                            "Distribuidor não encontrado com ID: " + dto.getFkDistribuidor()));
            contato.setDistribuidor(distribuidor);
        }

        return contato;
    }

    private ContatoResponseDTO convertToResponseDTO(Contato contato) {
        return new ContatoResponseDTO(
                contato.getIdContato(),
                contato.getNome(),
                contato.getEmail(),
                contato.getTelefone(),
                contato.getCliente() != null ? contato.getCliente().getIdCliente() : null,
                contato.getDistribuidor() != null ? contato.getDistribuidor().getIdDistribuidor() : null
        );
    }
}
