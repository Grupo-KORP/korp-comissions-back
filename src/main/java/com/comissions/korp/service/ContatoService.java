package com.comissions.korp.service;

import com.comissions.korp.DTO.ContatoDTO.ContatoRequestDTO;
import com.comissions.korp.DTO.ContatoDTO.ContatoResponseDTO;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Contato;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.ContatoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ContatoService {

    private final ContatoRepository contatoRepository;
    private final ClienteService clienteService;
    private final DistribuidorService distribuidorService;

    public ContatoService(ContatoRepository contatoRepository, ClienteService clienteService, DistribuidorService distribuidorService) {
        this.contatoRepository = contatoRepository;
        this.clienteService = clienteService;
        this.distribuidorService = distribuidorService;
    }

    @Transactional
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

    public ContatoResponseDTO buscarDtoPorId(Integer id) {
        Contato contato = contatoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Contato não encontrado com ID: " + id));
        return convertToResponseDTO(contato);
    }

    @Transactional
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
            Cliente cliente = clienteService.buscarClientePorId(requestDTO.getIdCliente());
            contatoExistente.setCliente(cliente);
        }

        if (requestDTO.getIdDistribuidor() != null) {
            Distribuidor distribuidor = distribuidorService.buscarDistribuidorPorId(requestDTO.getIdDistribuidor());
            contatoExistente.setDistribuidor(distribuidor);
        }

        Contato contatoAtualizado = contatoRepository.save(contatoExistente);
        return convertToResponseDTO(contatoAtualizado);
    }

    @Transactional
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
            Cliente cliente = clienteService.buscarClientePorId(dto.getIdCliente());
            contato.setCliente(cliente);
        }

        if (dto.getIdDistribuidor() != null) {
            Distribuidor distribuidor = distribuidorService.buscarDistribuidorPorId(dto.getIdDistribuidor());
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
