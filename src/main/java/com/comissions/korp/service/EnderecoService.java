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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        endereco.setBairro(requestDTO.getBairro());
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

    /** Primeiro endereço de cada cliente, numa consulta só. Cliente sem endereço não aparece no mapa. */
    public Map<Integer, Endereco> buscarEnderecosPorClientes(Collection<Integer> idsClientes) {
        if (idsClientes.isEmpty()) {
            return Map.of();
        }
        Map<Integer, Endereco> porCliente = new HashMap<>();
        enderecoRepository.buscarDosClientes(idsClientes)
                .forEach(endereco -> porCliente.putIfAbsent(endereco.getCliente().getIdCliente(), endereco));
        return porCliente;
    }

    /** Primeiro endereço de cada distribuidor, numa consulta só. Distribuidor sem endereço não aparece no mapa. */
    public Map<Integer, Endereco> buscarEnderecosPorDistribuidores(Collection<Integer> idsDistribuidores) {
        if (idsDistribuidores.isEmpty()) {
            return Map.of();
        }
        Map<Integer, Endereco> porDistribuidor = new HashMap<>();
        enderecoRepository.buscarDosDistribuidores(idsDistribuidores)
                .forEach(endereco -> porDistribuidor.putIfAbsent(endereco.getDistribuidor().getIdDistribuidor(), endereco));
        return porDistribuidor;
    }

    /** Endereço do cliente, se houver. Cadastros antigos podem não ter; quem chama decide o que fazer. */
    public Optional<Endereco> buscarEnderecoPorCliente(Integer idCliente) {
        return enderecoRepository.findFirstByCliente_IdCliente(idCliente);
    }
}
