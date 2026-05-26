package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteDTO.ClienteRequestDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorRequestDTO;
import com.comissions.korp.DTO.ContatoDTO.ContatoRequestDTO;
import com.comissions.korp.DTO.ContatoDTO.ContatoResponseDTO;
import com.comissions.korp.config.utils.SecurityUtils;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Contato;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.ContatoRepository;
import com.comissions.korp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final UsuarioRepository usuarioRepository;

    @Autowired
    private SecurityUtils securityUtils;

    public ContatoService(ContatoRepository contatoRepository, ClienteService clienteService, DistribuidorService distribuidorService, UsuarioRepository usuarioRepository) {
        this.contatoRepository = contatoRepository;
        this.clienteService = clienteService;
        this.distribuidorService = distribuidorService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ContatoResponseDTO criar(ContatoRequestDTO requestDTO) {
        Contato contato = convertToEntity(requestDTO);
        return convertToResponseDTO(contatoRepository.save(contato));
    }

        @Transactional
        public void criarFromClienteDTO(ClienteRequestDTO requestDTO, Integer id) {
            ContatoRequestDTO contatoDto = new ContatoRequestDTO();
            contatoDto.setNome(requestDTO.getNomeContato());
            contatoDto.setEmail(requestDTO.getEmail());
            contatoDto.setIdCliente(id);
            contatoDto.setAtivo(true);

            Contato contato = convertToEntity(contatoDto);

            contatoRepository.save(contato);
        }

            @Transactional
            public void atualizarFromClienteDTO(ClienteRequestDTO requestDTO, Integer id) {
                Cliente cliente = clienteService.buscarClientePorId(id);
                List<Contato> contatos = contatoRepository.findByCliente(cliente);
                if (contatos != null && !contatos.isEmpty()) {
                    Contato contatoExistente = contatos.get(0);
                    contatoExistente.setNome(requestDTO.getNomeContato());
                    contatoExistente.setEmail(requestDTO.getEmail());
                    contatoRepository.save(contatoExistente);
                } else {
                    criarFromClienteDTO(requestDTO, id);
                }
            }



    public List<ContatoResponseDTO> listarTodos() {
        return contatoRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void criarFromDistribuidorDTO(DistribuidorRequestDTO requestDTO, Integer id) {
        ContatoRequestDTO contatoDto = new ContatoRequestDTO();
        contatoDto.setNome(requestDTO.getContato());
        contatoDto.setEmail(requestDTO.getEmail());
        contatoDto.setIdDistribuidor(id);
        contatoDto.setAtivo(true);

        Contato contato = convertToEntity(contatoDto);

        contatoRepository.save(contato);
    }

    @Transactional
    public void atualizarFromDistribuidorDTO(DistribuidorRequestDTO requestDTO, Integer id) {
        Distribuidor distribuidor = distribuidorService.buscarDistribuidorPorId(id);
        java.util.List<Contato> contatos = contatoRepository.findByDistribuidor(distribuidor);
        if (contatos != null && !contatos.isEmpty()) {
            Contato contatoExistente = contatos.get(0);
            contatoExistente.setNome(requestDTO.getContato());
            contatoExistente.setEmail(requestDTO.getEmail());
            contatoRepository.save(contatoExistente);
        } else {
            criarFromDistribuidorDTO(requestDTO, id);
        }
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
        Contato contato = contatoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontrado("Contato não encontrado com ID: " + id));
        contato.setAtivo(false);
        contatoRepository.save(contato);
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

        contato.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);

        Integer id = securityUtils.getUsuarioIdAutenticado();

        Usuario user = usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontrado("Usuário autenticado não encontrado com ID: " + id));

        contato.setVendedorCadastro(user);

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
