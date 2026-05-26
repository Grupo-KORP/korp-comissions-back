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
import com.comissions.korp.repository.DistribuidorRepository;
import com.comissions.korp.repository.EnderecoRepository;
import com.comissions.korp.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final DistribuidorRepository distribuidorRepository;
    private final ContatoService contatoService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private EnderecoService enderecoService;
    @Autowired
    private EnderecoRepository enderecoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public ClienteService(ClienteRepository clienteRepository, DistribuidorRepository distribuidorRepository, @Lazy ContatoService contatoService) {
        this.clienteRepository = clienteRepository;
        this.distribuidorRepository = distribuidorRepository;
        this.contatoService = contatoService;
    }

    /**
     * Cria um novo Cliente
     */

    @Transactional(rollbackOn = Exception.class)
    public ClienteResponseDTO criar(ClienteRequestDTO requestDTO) {
        // Verifica se já existe cliente com mesmo email
        if (clienteRepository.existsByEmail((requestDTO.getEmail()))) {
            throw new UsuarioJaExistente("Já existe um cliente com este email: " + requestDTO.getEmail());
        }

        // Verifica se já existe cliente com mesmo cnpj
        if (clienteRepository.existsByCnpj((requestDTO.getCnpj()))) {
            throw new UsuarioJaExistente("Já existe um cliente com este CNPJ: " + requestDTO.getCnpj());
        }

        if(distribuidorRepository.existsByCnpj(requestDTO.getCnpj())) {
            throw new UsuarioJaExistente("Já existe um distribuidor com este CNPJ: " + requestDTO.getCnpj());
        }

        if (clienteRepository.existsByTelefone(((requestDTO.getTelefone())))) {
            throw new UsuarioJaExistente("Já existe um cliente com este telefone: " + requestDTO.getTelefone());
        }

        Cliente cliente = convertToEntity(requestDTO);

        Cliente clienteSalvo = clienteRepository.save(cliente);

        Endereco endereco = enderecoService.convertToEntityFromClienteRequest(requestDTO, clienteSalvo.getIdCliente());

        enderecoService.criarEndereco(endereco);

        contatoService.criarFromClienteDTO(requestDTO, clienteSalvo.getIdCliente());

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
    public ClienteResponseDTO buscarDtoPorId(Integer id) {
        Cliente cliente = buscarClientePorId(id);
        return convertToResponseDTO(cliente);
    }

    public Cliente buscarClientePorId(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Cliente não encontrado com ID: " + id));
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

    @Transactional
    public ClienteResponseDTO atualizar(Integer id, ClienteRequestDTO requestDTO) {
        Cliente clienteExistente = buscarClientePorId(id);

        // Atualiza os campos usando dados do DTO
        clienteExistente.setRazaoSocial(requestDTO.getRazaoSocial());
        clienteExistente.setNomeFantasia(requestDTO.getNomeFantasia());
        clienteExistente.setCnpj(requestDTO.getCnpj());
        clienteExistente.setInscricaoEstadual(requestDTO.getInscricaoEstadual());
        clienteExistente.setTelefone(requestDTO.getTelefone());
        clienteExistente.setEmail(requestDTO.getEmail());
        if (requestDTO.getAtivo() != null) {
            clienteExistente.setAtivo(requestDTO.getAtivo());
        }

        if (requestDTO.getFkVendedorCadastro() != null) {
            Usuario usuario = usuarioRepository.findById(requestDTO.getFkVendedorCadastro())
                    .orElseThrow(() -> new RecursoNaoEncontrado("Usuário não encontrado com ID: " + requestDTO.getFkVendedorCadastro()));
            clienteExistente.setVendedorCadastro(usuario);
        }

        Cliente clienteAtualizado = clienteRepository.save(clienteExistente);

        if (requestDTO.getEndereco() != null || requestDTO.getCep() != null || requestDTO.getCidade() != null || requestDTO.getUf() != null
                || requestDTO.getNumero() != null || requestDTO.getComplemento() != null || requestDTO.getBairro() != null) {
            List<Endereco> enderecos = enderecoRepository.findByCliente_IdCliente(id);
            if (enderecos != null && !enderecos.isEmpty()) {
                Endereco enderecoExistente = enderecos.getFirst();
                enderecoExistente.setLogradouro(requestDTO.getEndereco());
                enderecoExistente.setNumero(requestDTO.getNumero());
                enderecoExistente.setComplemento(requestDTO.getComplemento());
                enderecoExistente.setBairro(requestDTO.getBairro());
                enderecoExistente.setEstado(requestDTO.getUf());
                enderecoExistente.setCidade(requestDTO.getCidade());
                enderecoExistente.setCep(requestDTO.getCep());
                enderecoRepository.save(enderecoExistente);
            } else {
                Endereco endereco = enderecoService.convertToEntityFromClienteRequest(requestDTO, clienteAtualizado.getIdCliente());
                enderecoService.criarEndereco(endereco);
            }
        }
        return convertToResponseDTO(clienteAtualizado);
    }

    /**
     * Deleta um Cliente por ID
     */

    @Transactional
    public void deletar(Integer id) {

        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com ID: " + id));

        cliente.setAtivo(false);

        clienteRepository.save(cliente);
    }

    /**
     * Converte ClienteRequestDTO para Entity
     */
    private Cliente convertToEntity(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setRazaoSocial(dto.getRazaoSocial());
        cliente.setNomeFantasia(dto.getNomeFantasia());
        cliente.setCnpj(dto.getCnpj());
        cliente.setInscricaoEstadual(dto.getInscricaoEstadual());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEmail(dto.getEmail());

        Integer id = securityUtils.getUsuarioIdAutenticado();

        Usuario user = usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontrado("Usuário autenticado não encontrado com ID: " + id));

        cliente.setVendedorCadastro(user);

        return cliente;
    }

    /**
     * Converte Entity para ClienteResponseDTO usando Streams
     */
    private ClienteResponseDTO convertToResponseDTO(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setIdCliente(cliente.getIdCliente());
        dto.setRazaoSocial(cliente.getRazaoSocial());
        dto.setInscricaoEstadual(cliente.getInscricaoEstadual());
        dto.setNomeFantasia(cliente.getNomeFantasia());
        dto.setCnpj(cliente.getCnpj());
        dto.setTelefone(cliente.getTelefone());
        dto.setEmail(cliente.getEmail());

        return dto;
    }
}
