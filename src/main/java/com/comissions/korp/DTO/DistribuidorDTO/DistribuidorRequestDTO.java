package com.comissions.korp.DTO.DistribuidorDTO;

import com.comissions.korp.DTO.ClienteDTO.ClienteRequestDTO;

import java.util.List;

public class DistribuidorRequestDTO {

    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String telefone;
    private String inscricaoEstadual;
    private Boolean ativo;
    private String cep;
    private String bairro;
    private String endereco;
    private String numero;
    private String complemento;
    private String cidade;
    private String uf;
    private List<ContatoItemDistribuidorDTO> contatos;

    public static class ContatoItemDistribuidorDTO {
        private String nome;
        private String email;
        private String telefone;

        public String getNome()     { return nome; }
        public String getEmail()    { return email; }
        public String getTelefone() { return telefone; }

        public void setNome(String nome)         { this.nome = nome; }
        public void setEmail(String email)       { this.email = email; }
        public void setTelefone(String telefone) { this.telefone = telefone; }
    }

    public DistribuidorRequestDTO() {
    }

    public DistribuidorRequestDTO(String razaoSocial, String nomeFantasia, String cnpj, String telefone, String inscricaoEstadual, Boolean ativo, String cep, String bairro, String endereco, String numero, String complemento, String cidade, String uf, List<ContatoItemDistribuidorDTO> contatos) {
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.telefone = telefone;
        this.inscricaoEstadual = inscricaoEstadual;
        this.ativo = ativo;
        this.cep = cep;
        this.bairro = bairro;
        this.endereco = endereco;
        this.numero = numero;
        this.complemento = complemento;
        this.cidade = cidade;
        this.uf = uf;
        this.contatos = contatos;
    }

    public List<ContatoItemDistribuidorDTO> getContatos() {
        return contatos;
    }

    public void setContatos(List<ContatoItemDistribuidorDTO> contatos) {
        this.contatos = contatos;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public void setInscricaoEstadual(String inscricaoEstadual) {
        this.inscricaoEstadual = inscricaoEstadual;
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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
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
}
