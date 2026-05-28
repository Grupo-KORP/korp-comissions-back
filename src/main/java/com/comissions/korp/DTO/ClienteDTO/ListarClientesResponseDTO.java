package com.comissions.korp.DTO.ClienteDTO;

public class ListarClientesResponseDTO {

    private Integer idCliente;
    private String razaoSocial;
    private String nomeFantasia;
    private String inscricaoEstadual;
    private String cnpj;
    private String contato;
    private String email;
    private Boolean ativo;
    private String cep;
    private String logradouro;
    private String cidade;
    private String uf;
    private String numero;
    private String complemento;
    private String bairro;

    public ListarClientesResponseDTO(Integer idCliente, String razaoSocial, String inscricaoEstadual, String nomeFantasia, String cnpj, String contato, String email, Boolean ativo, String cep, String logradouro, String cidade, String uf, String numero, String complemento, String bairro) {
        this.idCliente = idCliente;
        this.razaoSocial = razaoSocial;
        this.inscricaoEstadual = inscricaoEstadual;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.contato = contato;
        this.email = email;
        this.ativo = ativo;
        this.cep = cep;
        this.logradouro = logradouro;
        this.cidade = cidade;
        this.uf = uf;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
    }

    public ListarClientesResponseDTO() {}

    public Integer getIdCliente() {
        return idCliente;
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

    public String getContato() {
        return contato;
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
