package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteDTO.ClientePedidoResponseDTO;
import com.comissions.korp.DTO.ClienteDTO.ClienteRequestDTO;
import com.comissions.korp.DTO.ClienteDTO.ClienteResponseDTO;
import com.comissions.korp.DTO.ClienteDTO.ListarClientesResponseDTO;
import com.comissions.korp.DTO.ContatoDTO.ContatoClienteResponseDTO;
import com.comissions.korp.DTO.DistribuidorDTO.ListarDistribuidoresResponseDTO;
import com.comissions.korp.config.utils.SecurityUtils;
import com.comissions.korp.entity.*;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.UsuarioJaExistente;
import com.comissions.korp.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final DistribuidorRepository distribuidorRepository;
    private final ContatoService contatoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private EnderecoService enderecoService;
    @Autowired
    private EnderecoRepository enderecoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ContatoRepository contatoRepository;

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

    public List<ClientePedidoResponseDTO> listarTodosPedidoDto() {
        return clienteRepository.findAll()
                .stream()
                .filter(cliente -> cliente.getAtivo() != null && cliente.getAtivo())
                .map(this::convertToPedidoResponseDTO)
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
            Endereco enderecos = enderecoService.buscarEnderecoPorCliente(id);
            if (enderecos != null) {
                Endereco enderecoExistente = enderecos;
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

        // compras realizadas ( pedidos por cliente )
        Integer comprasRealizadas = pedidoRepository.countByCliente(cliente);
        dto.setComprasRealizadas(comprasRealizadas);

        List<ContatoClienteResponseDTO> contatoDtoList = contatoService.buscarPorClienteToDto(cliente);

        dto.setContato(contatoDtoList);

        return dto;
    }

    private ClientePedidoResponseDTO convertToPedidoResponseDTO(Cliente cliente) {
        ClientePedidoResponseDTO dto = new ClientePedidoResponseDTO();

        // informações cliente
        dto.setId(cliente.getIdCliente());
        dto.setNomeFantasia(cliente.getNomeFantasia());
        dto.setRazaoSocial(cliente.getRazaoSocial());
        dto.setCnpj(cliente.getCnpj());
        dto.setEmail(cliente.getEmail());
        dto.setFone(cliente.getTelefone());
        dto.setInscricaoEstadual(cliente.getInscricaoEstadual());

        // informações endereço
        Endereco endereco = enderecoService.buscarEnderecoPorCliente(cliente.getIdCliente());

        dto.setCidade(endereco.getCidade());
        dto.setUf(endereco.getEstado());
        dto.setCep(endereco.getCep());
        dto.setEndereco(endereco.getLogradouro());

        List<ContatoClienteResponseDTO> contatoDtoList = contatoService.buscarPorClienteToDto(cliente);

        dto.setContatos(contatoDtoList);

        return dto;
    }

    public Page<ListarClientesResponseDTO> listarTodosClientes(String busca, Pageable pageable) {

        String filtro = (busca != null && !busca.isBlank()) ? busca : null;

        Page<Cliente> paginaClientes = clienteRepository.findClientesComFiltro(filtro, pageable);

        return paginaClientes.map(cliente -> {

            List<Endereco> enderecoCliente = enderecoRepository.findByCliente_IdCliente(cliente.getIdCliente());

            Endereco endereco = enderecoCliente != null && !enderecoCliente.isEmpty()
                    ? enderecoCliente.getFirst()
                    : null;

            List<ContatoClienteResponseDTO> contatos = contatoService.buscarPorClienteToDto(cliente);

            return new ListarClientesResponseDTO(
                    cliente.getIdCliente(),
                    cliente.getRazaoSocial(),
                    cliente.getNomeFantasia(),
                    cliente.getInscricaoEstadual(),
                    cliente.getEmail(),
                    cliente.getTelefone(),
                    cliente.getCnpj(),
                    cliente.getAtivo(),
                    endereco != null ? endereco.getCep()        : null,
                    endereco != null ? endereco.getLogradouro() : null,
                    endereco != null ? endereco.getCidade()     : null,
                    endereco != null ? endereco.getEstado()     : null,
                    endereco != null ? endereco.getNumero()     : null,
                    endereco != null ? endereco.getComplemento(): null,
                    endereco != null ? endereco.getBairro()     : null,
                    contatos
            );
        });

    }
}
