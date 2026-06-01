package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteDTO.ClientePedidoResponseDTO;
import com.comissions.korp.DTO.ContatoDTO.ContatoClienteResponseDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorPedidoResponseDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorRequestDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorResponseDTO;
import com.comissions.korp.DTO.DistribuidorDTO.ListarDistribuidoresResponseDTO;
import com.comissions.korp.DTO.ListarVendedoresResponseDTO;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.Endereco;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.UsuarioJaExistente;
import com.comissions.korp.repository.ClienteRepository;
import com.comissions.korp.repository.DistribuidorRepository;
import com.comissions.korp.repository.EnderecoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DistribuidorService {

    private final DistribuidorRepository distribuidorRepository;
    private final ClienteRepository clienteRepository;
    private final ContatoService contatoService;

    @Autowired
    private EnderecoService enderecoService;
    @Autowired
    private EnderecoRepository enderecoRepository;

    public DistribuidorService(DistribuidorRepository distribuidorRepository, ClienteRepository clienteRepository, @Lazy ContatoService contatoService) {
        this.distribuidorRepository = distribuidorRepository;
        this.clienteRepository = clienteRepository;
        this.contatoService = contatoService;
    }

    @Transactional(rollbackFor = Exception.class)
    public DistribuidorResponseDTO criar(DistribuidorRequestDTO requestDTO) {
        if (distribuidorRepository.existsByEmail((requestDTO.getEmail()))) {
            throw new UsuarioJaExistente("Já existe um distribuidor com este email: " + requestDTO.getEmail());
        }

        if (distribuidorRepository.existsByCnpj((requestDTO.getCnpj()))) {
            throw new UsuarioJaExistente("Já existe um distribuidor com este CNPJ: " + requestDTO.getCnpj());
        }

        if(clienteRepository.existsByCnpj(requestDTO.getCnpj())) {
            throw new UsuarioJaExistente("Já existe um cliente com este CNPJ: " + requestDTO.getCnpj());
        }

        if (distribuidorRepository.existsByTelefone(((requestDTO.getTelefone())))) {
            throw new UsuarioJaExistente("Já existe um distribuidor com este telefone: " + requestDTO.getTelefone());
        }

        Distribuidor distribuidor = convertToEntity(requestDTO);

        Distribuidor distribuidorSalvo = distribuidorRepository.save(distribuidor);

        Endereco endereco = enderecoService.convertToEntityFromDistribuidorRequest(requestDTO, distribuidorSalvo.getIdDistribuidor());

        enderecoService.criarEndereco(endereco);

        contatoService.criarFromDistribuidorDTO(requestDTO, distribuidorSalvo.getIdDistribuidor());

        return convertToResponseDTO(distribuidorSalvo);
    }

    public List<DistribuidorResponseDTO> listarTodos() {
        return distribuidorRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public DistribuidorResponseDTO buscarDtoPorId(Integer id) {
        Distribuidor distribuidor = buscarDistribuidorPorId(id);
        return convertToResponseDTO(distribuidor);
    }

    public Distribuidor buscarDistribuidorPorId(Integer id) {
        return distribuidorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Distribuidor não encontrado com ID: " + id));
    }

    public DistribuidorResponseDTO buscarPorCnpj(String cnpj) {
        Distribuidor distribuidor = distribuidorRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new RecursoNaoEncontrado(
                        "Distribuidor não encontrado com CNPJ: " + cnpj));
        return convertToResponseDTO(distribuidor);
    }

    @Transactional
    public DistribuidorResponseDTO atualizar(Integer id, DistribuidorRequestDTO requestDTO) {
        Distribuidor distribuidorExistente = buscarDistribuidorPorId(id);

        // Atualiza os campos usando dados do DTO
        distribuidorExistente.setRazaoSocial(requestDTO.getRazaoSocial());
        distribuidorExistente.setNomeFantasia(requestDTO.getNomeFantasia());
        distribuidorExistente.setCnpj(requestDTO.getCnpj());
        distribuidorExistente.setInscricaoEstadual(requestDTO.getInscricaoEstadual());
        distribuidorExistente.setTelefone(requestDTO.getTelefone());
        distribuidorExistente.setEmail(requestDTO.getEmail());
        if (requestDTO.getAtivo() != null) {
            distribuidorExistente.setAtivo(requestDTO.getAtivo());
        }

        Distribuidor distribuidorAtualizado = distribuidorRepository.save(distribuidorExistente);

        if (requestDTO.getEndereco() != null || requestDTO.getCep() != null || requestDTO.getCidade() != null || requestDTO.getUf() != null
                || requestDTO.getNumero() != null || requestDTO.getComplemento() != null || requestDTO.getBairro() != null) {
            List<Endereco> enderecos = enderecoRepository.findByDistribuidor_IdDistribuidor(id);
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
                Endereco endereco = enderecoService.convertToEntityFromDistribuidorRequest(requestDTO, distribuidorAtualizado.getIdDistribuidor());
                enderecoService.criarEndereco(endereco);
            }
        }

        return convertToResponseDTO(distribuidorAtualizado);
    }

    @Transactional
    public void deletar(Integer id) {

        Distribuidor distribuidor = distribuidorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado("Distribuidor não encontrado com ID: " + id));
        distribuidor.setAtivo(false);

        distribuidorRepository.save(distribuidor);
    }


    /**
     * Lista todos os Distribuidores com paginação
     */
    public Page<ListarDistribuidoresResponseDTO> listarTodosDistribuidores(String busca, Pageable pageable) {

        String filtro = (busca != null && !busca.isBlank()) ? busca : null;

        Page<Distribuidor> paginaDistribuidores = distribuidorRepository.findDistribuidoresComFiltro(filtro, pageable);

        return paginaDistribuidores.map(distribuidor -> {
            List<Endereco> enderecoDistribuidor = enderecoRepository.findByDistribuidor_IdDistribuidor(distribuidor.getIdDistribuidor());

            Endereco endereco = enderecoDistribuidor != null && !enderecoDistribuidor.isEmpty()
                    ? enderecoDistribuidor.getFirst()
                    : null;

            return new ListarDistribuidoresResponseDTO(
                    distribuidor.getIdDistribuidor(),
                    distribuidor.getRazaoSocial(),
                    distribuidor.getNomeFantasia(),
                    distribuidor.getInscricaoEstadual(),
                    distribuidor.getCnpj(),
                    distribuidor.getTelefone(),
                    distribuidor.getEmail(),
                    distribuidor.getAtivo(),
                    endereco != null ? endereco.getCep() : null,
                    endereco != null ? endereco.getLogradouro() : null,
                    endereco != null ? endereco.getCidade() : null,
                    endereco != null ? endereco.getEstado() : null,
                    endereco != null ? endereco.getNumero() : null,
                    endereco != null ? endereco.getComplemento() : null,
                    endereco != null ? endereco.getBairro() : null

            );
        });
    }


    public List<DistribuidorPedidoResponseDTO> listarTodosPedidoDto() {
        return distribuidorRepository.findAll()
                .stream()
                .map(this::convertToPedidoResponseDTO)
                .collect(Collectors.toList());
    }

    private Distribuidor convertToEntity(DistribuidorRequestDTO dto) {
        Distribuidor distribuidor = new Distribuidor();
        distribuidor.setRazaoSocial(dto.getRazaoSocial());
        distribuidor.setNomeFantasia(dto.getNomeFantasia());
        distribuidor.setCnpj(dto.getCnpj());
        distribuidor.setInscricaoEstadual(dto.getInscricaoEstadual());
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
        dto.setInscricaoEstadual(distribuidor.getInscricaoEstadual());
        dto.setTelefone(distribuidor.getTelefone());
        dto.setEmail(distribuidor.getEmail());
        return dto;
    }

    private DistribuidorPedidoResponseDTO convertToPedidoResponseDTO(Distribuidor distribuidor) {
        DistribuidorPedidoResponseDTO dto = new DistribuidorPedidoResponseDTO();

        // informações distribuidor
        dto.setId(distribuidor.getIdDistribuidor());
        dto.setNomeFantasia(distribuidor.getNomeFantasia());
        dto.setRazaoSocial(distribuidor.getRazaoSocial());
        dto.setCnpj(distribuidor.getCnpj());
        dto.setInscEst(distribuidor.getInscricaoEstadual());
        dto.setEmail(distribuidor.getEmail());
        dto.setFone(distribuidor.getTelefone());

        // informações endereço
        Endereco endereco = enderecoService.buscarEnderecoPorDistribuidor(distribuidor.getIdDistribuidor());

        dto.setCidade(endereco.getCidade());
        dto.setUf(endereco.getEstado());
        dto.setCep(endereco.getCep());
        dto.setEndereco(endereco.getLogradouro());

        // informações contato
        List<ContatoClienteResponseDTO> contatoDtoList = contatoService.buscarPorDistribuidorToDto(distribuidor);

        dto.setContatos(contatoDtoList);

        return dto;
    }
}
