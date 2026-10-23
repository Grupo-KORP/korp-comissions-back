package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteDTO.ClienteContatosRequestDTO;
import com.comissions.korp.DTO.ClienteDTO.ClienteRequestDTO;
import com.comissions.korp.DTO.ContatoDTO.*;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorContatosRequestDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorRequestDTO;
import com.comissions.korp.config.utils.SecurityUtils;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Contato;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.OperacaoNaoPermitida;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.RequisicaoInvalida;
import com.comissions.korp.repository.ContatoRepository;
import com.comissions.korp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public void criarFromClienteDTO(ClienteRequestDTO requestDTO, Integer idCliente) {
        if (requestDTO.getContatos() == null || requestDTO.getContatos().isEmpty()) return;

        for (ClienteRequestDTO.ContatoItemClienteDTO item : requestDTO.getContatos()) {
            salvarContatoCliente(item.getNome(), item.getEmail(), item.getTelefone(), idCliente);
        }
    }

    @Transactional
    public void atualizarContatosCliente(
            ClienteContatosRequestDTO requestDTO,
            Integer idCliente) {

        clienteService.buscarClientePorId(idCliente);

        for (ClienteContatosRequestDTO.ContatoItemDTO contatoDTO : requestDTO.getContatos()) {

            validarDadosContato(contatoDTO.getNome(), contatoDTO.getEmail(), contatoDTO.getTelefone());

            // Caso o contato venha como nulo é porque está sendo criado no front
            if (contatoDTO.getIdContato() == null) {
                salvarContatoCliente(contatoDTO.getNome(), contatoDTO.getEmail(), contatoDTO.getTelefone(), idCliente);
            } else {

                Contato contato = contatoRepository.findById(contatoDTO.getIdContato())
                        .orElseThrow(() -> new RecursoNaoEncontrado(
                                "Contato não encontrado com ID: " + contatoDTO.getIdContato()));

                if (contato.getCliente() == null || !idCliente.equals(contato.getCliente().getIdCliente())) {
                    throw new RequisicaoInvalida("O contato " + contatoDTO.getIdContato() + " não pertence a este cliente.");
                }

                contato.setNome(contatoDTO.getNome());
                contato.setEmail(contatoDTO.getEmail());
                contato.setTelefone(contatoDTO.getTelefone());

                contatoRepository.save(contato);
            }
        }
    }


    public List<ContatoResponseDTO> listarTodos() {
        return contatoRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void criarFromDistribuidorDTO(DistribuidorRequestDTO requestDTO, Integer idDistribuidor) {

        if (requestDTO.getContatos() == null || requestDTO.getContatos().isEmpty()) return;

        for (DistribuidorRequestDTO.ContatoItemDistribuidorDTO item : requestDTO.getContatos()) {
            salvarContatoDistribuidor(item.getNome(), item.getEmail(), item.getTelefone(), idDistribuidor);
        }
    }

    @Transactional
    public void atualizarContatosDistribuidor(
            DistribuidorContatosRequestDTO requestDTO,
            Integer idDistribuidor) {

        distribuidorService.buscarDistribuidorPorId(idDistribuidor);

        for (DistribuidorContatosRequestDTO.ContatoItemDTO contatoDTO : requestDTO.getContatos()) {

            validarDadosContato(contatoDTO.getNome(), contatoDTO.getEmail(), contatoDTO.getTelefone());

            // Caso o contato venha como nulo é porque está sendo criado no front
            if (contatoDTO.getIdContato() == null) {
                salvarContatoDistribuidor(contatoDTO.getNome(), contatoDTO.getEmail(), contatoDTO.getTelefone(), idDistribuidor);
            } else {

                Contato contato = contatoRepository.findById(contatoDTO.getIdContato())
                        .orElseThrow(() -> new RecursoNaoEncontrado(
                                "Contato não encontrado com ID: " + contatoDTO.getIdContato()));

                if (contato.getDistribuidor() == null || !idDistribuidor.equals(contato.getDistribuidor().getIdDistribuidor())) {
                    throw new RequisicaoInvalida("O contato " + contatoDTO.getIdContato() + " não pertence a este distribuidor.");
                }

                contato.setNome(contatoDTO.getNome());
                contato.setEmail(contatoDTO.getEmail());
                contato.setTelefone(contatoDTO.getTelefone());

                contatoRepository.save(contato);
            }
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

        validarDadosContato(requestDTO.getNome(), requestDTO.getEmail(), requestDTO.getTelefone());

        // O contato não pode mudar de dono
        if (requestDTO.getIdCliente() != null
                && (contatoExistente.getCliente() == null
                || !requestDTO.getIdCliente().equals(contatoExistente.getCliente().getIdCliente()))) {
            throw new RequisicaoInvalida("Não é permitido mover o contato para outro cliente.");
        }

        if (requestDTO.getIdDistribuidor() != null
                && (contatoExistente.getDistribuidor() == null
                || !requestDTO.getIdDistribuidor().equals(contatoExistente.getDistribuidor().getIdDistribuidor()))) {
            throw new RequisicaoInvalida("Não é permitido mover o contato para outro distribuidor.");
        }

        // Atualiza os campos usando dados do DTO
        contatoExistente.setNome(requestDTO.getNome());
        contatoExistente.setEmail(requestDTO.getEmail());
        contatoExistente.setTelefone(requestDTO.getTelefone());

        Contato contatoAtualizado = contatoRepository.save(contatoExistente);
        return convertToResponseDTO(contatoAtualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        Contato contato = contatoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontrado("Contato não encontrado com ID: " + id));

        if (Boolean.TRUE.equals(contato.getAtivo()) && eUltimoContatoAtivo(contato)) {
            throw new OperacaoNaoPermitida("A empresa precisa ter pelo menos um contato. Edite este contato ou cadastre um novo antes de excluí-lo.");
        }

        contato.setAtivo(false);
        contatoRepository.save(contato);
    }

    private boolean eUltimoContatoAtivo(Contato contato) {
        if (contato.getCliente() != null) {
            return contatoRepository.countByClienteAndAtivoTrue(contato.getCliente()) <= 1;
        }
        if (contato.getDistribuidor() != null) {
            return contatoRepository.countByDistribuidorAndAtivoTrue(contato.getDistribuidor()) <= 1;
        }
        return false;
    }

    private void validarDadosContato(String nome, String email, String telefone) {
        if (nome == null || nome.isBlank()) {
            throw new RequisicaoInvalida("Nome do contato obrigatório.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new RequisicaoInvalida("E-mail do contato inválido.");
        }
        String digitos = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (digitos.length() < 10 || digitos.length() > 11) {
            throw new RequisicaoInvalida("Telefone do contato inválido: informe DDD e 10 ou 11 dígitos.");
        }
    }

    private Contato convertToEntity(ContatoRequestDTO dto) {
        validarDadosContato(dto.getNome(), dto.getEmail(), dto.getTelefone());

        if ((dto.getIdCliente() == null) == (dto.getIdDistribuidor() == null)) {
            throw new RequisicaoInvalida("Informe o cliente ou o distribuidor do contato (apenas um).");
        }

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

    public List<ContatoDistribuidorResponseDTO> buscarPorDistribuidorToDto(Distribuidor distribuidor) {
        List<Contato> contatos = contatoRepository.findByDistribuidorAndAtivoTrue(distribuidor);

        List<ContatoDistribuidorResponseDTO> contatoDtoList = new ArrayList<>();

        contatos.forEach(contato -> {
            ContatoDistribuidorResponseDTO contatoDTO = new ContatoDistribuidorResponseDTO();
            contatoDTO.setIdContato(contato.getIdContato());
            contatoDTO.setNome(contato.getNome());
            contatoDTO.setEmail(contato.getEmail());
            contatoDTO.setTelefone(contato.getTelefone());
            contatoDtoList.add(contatoDTO);
        });

        return contatoDtoList;
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

    public List<ContatoClienteResponseDTO> buscarPorClienteToDto(Cliente cliente) {
        List<Contato> contatos = contatoRepository.findByClienteAndAtivoTrue(cliente);

        List<ContatoClienteResponseDTO> contatoDtoList = new ArrayList<>();

        contatos.forEach(contato -> {;
            ContatoClienteResponseDTO contatoDTO = new ContatoClienteResponseDTO();
            contatoDTO.setIdContato(contato.getIdContato());
            contatoDTO.setNome(contato.getNome());
            contatoDTO.setEmail(contato.getEmail());
            contatoDTO.setTelefone(contato.getTelefone());
            contatoDtoList.add(contatoDTO);
        });

        return contatoDtoList;
    }

    private void salvarContatoCliente(String nome, String email, String telefone, Integer idCliente) {
        ContatoRequestDTO dto = new ContatoRequestDTO();
        dto.setNome(nome);
        dto.setEmail(email);
        dto.setTelefone(telefone);
        dto.setIdCliente(idCliente);
        dto.setAtivo(true);
        contatoRepository.save(convertToEntity(dto));
    }

    private void salvarContatoDistribuidor(String nome, String email, String telefone, Integer idDistribuidor) {
        ContatoRequestDTO dto = new ContatoRequestDTO();
        dto.setNome(nome);
        dto.setEmail(email);
        dto.setTelefone(telefone);
        dto.setIdDistribuidor(idDistribuidor);
        dto.setAtivo(true);
        contatoRepository.save(convertToEntity(dto));
    }
}
