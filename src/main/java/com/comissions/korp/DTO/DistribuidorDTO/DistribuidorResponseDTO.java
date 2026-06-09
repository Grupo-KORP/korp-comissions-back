package com.comissions.korp.DTO.DistribuidorDTO;

import com.comissions.korp.DTO.ContatoDTO.ContatoClienteResponseDTO;
import com.comissions.korp.DTO.ContatoDTO.ContatoDistribuidorResponseDTO;

import java.util.List;

public class DistribuidorResponseDTO {
    private Integer idDistribuidor;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String telefone;
    private String email;
    private String inscricaoEstadual;
    private Boolean ativo;
    private List<ContatoDistribuidorResponseDTO> contato;
    private Integer fkVendedorCadastro;
    private String cep;
    private String bairro;
    private String endereco;
    private String numero;
    private String complemento;
    private String cidade;
    private String uf;
    private String nomeContato;

    public DistribuidorResponseDTO() {
    }

    public DistribuidorResponseDTO(Integer idDistribuidor, String razaoSocial, String nomeFantasia, String cnpj, String telefone, String email, String inscricaoEstadual, Boolean ativo, List<ContatoDistribuidorResponseDTO> contato, Integer fkVendedorCadastro, String cep, String bairro, String endereco, String numero, String complemento, String cidade, String uf, String nomeContato) {
        this.idDistribuidor = idDistribuidor;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.telefone = telefone;
        this.email = email;
        this.inscricaoEstadual = inscricaoEstadual;
        this.ativo = ativo;
        this.contato = contato;
        this.fkVendedorCadastro = fkVendedorCadastro;
        this.cep = cep;
        this.bairro = bairro;
        this.endereco = endereco;
        this.numero = numero;
        this.complemento = complemento;
        this.cidade = cidade;
        this.uf = uf;
        this.nomeContato = nomeContato;
    }

    public List<ContatoDistribuidorResponseDTO> getContato() {
        return contato;
    }

    public void setContato(List<ContatoDistribuidorResponseDTO> contato) {
        this.contato = contato;
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

    public String getNomeContato() {
        return nomeContato;
    }

    public void setNomeContato(String nomeContato) {
        this.nomeContato = nomeContato;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getIdDistribuidor() {
        return idDistribuidor;
    }

    public void setIdDistribuidor(Integer idDistribuidor) {
        this.idDistribuidor = idDistribuidor;
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
}
