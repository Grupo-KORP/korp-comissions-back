package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteDTO.ClientePedidoResponseDTO;
import com.comissions.korp.DTO.DadosEmpresaRequestDTO;
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
import java.util.Map;
import java.util.Optional;
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

    /**
     * Clientes ativos para a tela de pedido. Endereços e contatos vêm em uma consulta cada,
     * para todos os clientes de uma vez (antes eram duas consultas por cliente).
     */
    public List<ClientePedidoResponseDTO> listarTodosPedidoDto() {
        List<Cliente> ativos = clienteRepository.findAll()
                .stream()
                .filter(cliente -> cliente.getAtivo() != null && cliente.getAtivo())
                .toList();
        List<Integer> ids = ativos.stream().map(Cliente::getIdCliente).toList();
        Map<Integer, Endereco> enderecos = enderecoService.buscarEnderecosPorClientes(ids);
        Map<Integer, List<ContatoClienteResponseDTO>> contatos = contatoService.buscarPorClientesToDto(ids);

        return ativos.stream()
                .map(cliente -> convertToPedidoResponseDTO(
                        cliente,
                        enderecos.get(cliente.getIdCliente()),
                        contatos.getOrDefault(cliente.getIdCliente(), List.of())))
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
            // Cliente sem endereço (cadastro antigo): cria um em vez de responder 404
            Optional<Endereco> enderecoAtual = enderecoService.buscarEnderecoPorCliente(id);
            if (enderecoAtual.isPresent()) {
                Endereco enderecoExistente = enderecoAtual.get();
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
     * Atualiza somente nome fantasia e telefone do Cliente (edição pelos detalhes)
     */
    @Transactional
    public ClienteResponseDTO atualizarDados(Integer id, DadosEmpresaRequestDTO requestDTO) {
        Cliente cliente = buscarClientePorId(id);
        DadosEmpresaRequestDTO dados = requestDTO.validarENormalizar();

        // Só confere duplicidade quando o telefone muda, para não bloquear registros antigos com telefone repetido
        String telefoneAtual = cliente.getTelefone() == null ? "" : cliente.getTelefone().replaceAll("\\D", "");
        if (!dados.getTelefone().equals(telefoneAtual) && clienteRepository.existsByTelefone(dados.getTelefone())) {
            throw new UsuarioJaExistente("Já existe um cliente com este telefone: " + dados.getTelefone());
        }

        cliente.setNomeFantasia(dados.getNomeFantasia());
        cliente.setTelefone(dados.getTelefone());
        return convertToResponseDTO(clienteRepository.save(cliente));
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

    private ClientePedidoResponseDTO convertToPedidoResponseDTO(Cliente cliente, Endereco endereco,
                                                                List<ContatoClienteResponseDTO> contatos) {
        ClientePedidoResponseDTO dto = new ClientePedidoResponseDTO();

        // informações cliente
        dto.setId(cliente.getIdCliente());
        dto.setNomeFantasia(cliente.getNomeFantasia());
        dto.setRazaoSocial(cliente.getRazaoSocial());
        dto.setCnpj(cliente.getCnpj());
        dto.setEmail(cliente.getEmail());
        dto.setFone(cliente.getTelefone());
        dto.setInscricaoEstadual(cliente.getInscricaoEstadual());

        // informações endereço (sem endereço cadastrado, os campos ficam vazios em vez de derrubar a lista)
        if (endereco != null) {
            dto.setCidade(endereco.getCidade());
            dto.setUf(endereco.getEstado());
            dto.setCep(endereco.getCep());
            dto.setEndereco(endereco.getLogradouro());
        }

        dto.setContatos(contatos);

        return dto;
    }

    public Page<ListarClientesResponseDTO> listarTodosClientes(String busca, Pageable pageable) {

        String filtro = (busca != null && !busca.isBlank()) ? busca : null;

        Page<Cliente> paginaClientes = clienteRepository.findClientesComFiltro(filtro, pageable);

        // Endereços e contatos da página inteira, uma consulta cada (antes eram duas por cliente)
        List<Integer> ids = paginaClientes.getContent().stream().map(Cliente::getIdCliente).toList();
        Map<Integer, Endereco> enderecos = enderecoService.buscarEnderecosPorClientes(ids);
        Map<Integer, List<ContatoClienteResponseDTO>> contatosPorCliente = contatoService.buscarPorClientesToDto(ids);

        return paginaClientes.map(cliente -> {

            Endereco endereco = enderecos.get(cliente.getIdCliente());

            List<ContatoClienteResponseDTO> contatos = contatosPorCliente.getOrDefault(cliente.getIdCliente(), List.of());

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
