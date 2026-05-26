package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteDTO.ClienteRequestDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorRequestDTO;
import com.comissions.korp.config.utils.SecurityUtils;
import com.comissions.korp.entity.Endereco;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.ClienteRepository;
import com.comissions.korp.repository.DistribuidorRepository;
import com.comissions.korp.repository.EnderecoRepository;
import com.comissions.korp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnderecoService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private DistribuidorRepository distribuidorRepository;

    public Endereco convertToEntityFromClienteRequest(ClienteRequestDTO requestDTO, Integer idCliente){
        Endereco endereco = new Endereco();
        endereco.setLogradouro(requestDTO.getEndereco());
        endereco.setNumero(requestDTO.getNumero());
        endereco.setComplemento(requestDTO.getComplemento());
        endereco.setBairro(null);
        endereco.setEstado(requestDTO.getUf());
        endereco.setCidade(requestDTO.getCidade());
        endereco.setCep(requestDTO.getCep());
        endereco.setCliente(clienteRepository.findById(idCliente).orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com id: " + idCliente)));

        return endereco;
    }

    public Endereco convertToEntityFromDistribuidorRequest(DistribuidorRequestDTO requestDTO, Integer idDistribuidor){
        Endereco endereco = new Endereco();
        endereco.setLogradouro(requestDTO.getEndereco());
        endereco.setNumero(requestDTO.getNumero());
        endereco.setComplemento(requestDTO.getComplemento());
        endereco.setBairro(requestDTO.getBairro());
        endereco.setEstado(requestDTO.getUf());
        endereco.setCidade(requestDTO.getCidade());
        endereco.setCep(requestDTO.getCep());
        endereco.setDistribuidor(distribuidorRepository.findById(idDistribuidor).orElseThrow(() -> new RecursoNaoEncontrado("Distribuidor não encontrado com id: " + idDistribuidor)));

        return endereco;
    }

    public Endereco criarEndereco(Endereco endereco){
        Endereco novoEndereco = enderecoRepository.save(endereco);

        return novoEndereco;
    }
}
