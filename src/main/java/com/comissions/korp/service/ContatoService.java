package com.comissions.korp.service;

import com.comissions.korp.DTO.ContatoRequestDTO;
import com.comissions.korp.DTO.ContatoResponseDTO;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Contato;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.ClienteRepository;
import com.comissions.korp.repository.ContatoRepository;
import com.comissions.korp.repository.DistribuidorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContatoService {

    private final ContatoRepository contatoRepository;
    private final ClienteRepository clienteRepository;
    private final DistribuidorRepository distribuidorRepository;

    public ContatoService(ContatoRepository contatoRepository, ClienteRepository clienteRepository, DistribuidorRepository distribuidorRepository) {
        this.contatoRepository = contatoRepository;
        this.clienteRepository = clienteRepository;
        this.distribuidorRepository = distribuidorRepository;
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
        if (requestDTO.getIdCliente() != null) {
            Cliente cliente = clienteRepository.findById(requestDTO.getIdCliente())
                    .orElseThrow(() -> new RecursoNaoEncontrado(
                            "Cliente não encontrado com ID: " + requestDTO.getIdCliente()));
            contatoExistente.setCliente(cliente);
        }

        if (requestDTO.getIdDistribuidor() != null) {
            Distribuidor distribuidor = distribuidorRepository.findById(requestDTO.getIdDistribuidor())
                    .orElseThrow(() -> new RecursoNaoEncontrado(
                            "Distribuidor não encontrado com ID: " + requestDTO.getIdDistribuidor()));
            contatoExistente.setDistribuidor(distribuidor);
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
        if (dto.getIdCliente() != null) {
            Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                    .orElseThrow(() -> new RecursoNaoEncontrado(
                            "Cliente não encontrado com ID: " + dto.getIdCliente()));
            contato.setCliente(cliente);
        }

        if (dto.getIdDistribuidor() != null) {
            Distribuidor distribuidor = distribuidorRepository.findById(dto.getIdDistribuidor())
                    .orElseThrow(() -> new RecursoNaoEncontrado(
                            "Distribuidor não encontrado com ID: " + dto.getIdDistribuidor()));
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
