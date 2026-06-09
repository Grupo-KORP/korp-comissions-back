package com.comissions.korp.DTO.ClienteDTO;

import com.comissions.korp.DTO.ContatoDTO.ContatoClienteResponseDTO;

import java.util.List;

public class ListarClientesResponseDTO {

    private Integer idCliente;
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
    private List<ContatoClienteResponseDTO> contatos;

    public ListarClientesResponseDTO(Integer idCliente, String razaoSocial, String nomeFantasia, String inscricaoEstadual, String email, String telefone, String cnpj, Boolean ativo, String cep, String logradouro, String cidade, String uf, String numero, String complemento, String bairro, List<ContatoClienteResponseDTO> contatos) {
        this.idCliente = idCliente;
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

    public ListarClientesResponseDTO() {}


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public void setInscricaoEstadual(String inscricaoEstadual) {
        this.inscricaoEstadual = inscricaoEstadual;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public List<ContatoClienteResponseDTO> getContatos() {
        return contatos;
    }

    public void setContatos(List<ContatoClienteResponseDTO> contatos) {
        this.contatos = contatos;
    }
}
