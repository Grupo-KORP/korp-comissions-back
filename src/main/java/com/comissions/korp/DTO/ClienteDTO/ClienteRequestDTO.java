package com.comissions.korp.DTO.ClienteDTO;

import com.comissions.korp.DTO.ContatoDTO.ContatoRequestDTO;
import java.util.List;

public class ClienteRequestDTO {
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String telefone;
    private String inscricaoEstadual;
    private Boolean ativo;
    private Integer fkVendedorCadastro;
    private String cep;
    private String bairro;
    private String endereco;
    private String numero;
    private String complemento;
    private String cidade;
    private String uf;
    private List<ContatoItemClienteDTO> contatos; // nova lista

    public static class ContatoItemClienteDTO {
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

    public ClienteRequestDTO() {}

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

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
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

    public Integer getFkVendedorCadastro() {
        return fkVendedorCadastro;
    }

    public void setFkVendedorCadastro(Integer fkVendedorCadastro) {
        this.fkVendedorCadastro = fkVendedorCadastro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
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

    public List<ContatoItemClienteDTO> getContatos() {
        return contatos;
    }

    public void setContatos(List<ContatoItemClienteDTO> contatos) {
        this.contatos = contatos;
    }
}
