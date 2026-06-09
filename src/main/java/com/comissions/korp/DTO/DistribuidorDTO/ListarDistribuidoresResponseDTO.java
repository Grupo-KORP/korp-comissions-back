package com.comissions.korp.DTO.DistribuidorDTO;


import com.comissions.korp.DTO.ContatoDTO.ContatoDistribuidorResponseDTO;

import java.util.List;

public class ListarDistribuidoresResponseDTO {

    private Integer idDistribuidor;
    private String razaoSocial;
    private String nomeFantasia;
    private String inscricaoEstadual;
    private String email;
    private String telefone;
    private String cnpj;
    private Boolean ativo;
    private String cep;
    private String logradouro;
    private String cidade;
    private String uf;
    private String numero;
    private String complemento;
    private String bairro;
    private List<ContatoDistribuidorResponseDTO> contatos;

    public ListarDistribuidoresResponseDTO() {}

    public ListarDistribuidoresResponseDTO(Integer idDistribuidor, String razaoSocial, String nomeFantasia, String inscricaoEstadual, String email, String telefone, String cnpj, Boolean ativo, String cep, String logradouro, String cidade, String uf, String numero, String complemento, String bairro, List<ContatoDistribuidorResponseDTO> contatos) {
        this.idDistribuidor = idDistribuidor;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.inscricaoEstadual = inscricaoEstadual;
        this.email = email;
        this.telefone = telefone;
        this.cnpj = cnpj;
        this.ativo = ativo;
        this.cep = cep;
        this.logradouro = logradouro;
        this.cidade = cidade;
        this.uf = uf;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.contatos = contatos;
    }

    public String getTelefone() {
        return telefone;
    }

    public List<ContatoDistribuidorResponseDTO> getContatos() {
        return contatos;
    }

    public Integer getIdDistribuidor() {
        return idDistribuidor;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public String getCep() {
        return cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getUf() {
        return uf;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }
}
