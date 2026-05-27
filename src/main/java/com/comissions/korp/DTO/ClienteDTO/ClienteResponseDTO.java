package com.comissions.korp.DTO.ClienteDTO;

import com.comissions.korp.DTO.ContatoDTO.ContatoClienteResponseDTO;

import java.util.List;

public class ClienteResponseDTO {
    private Integer idCliente;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String telefone;
    private String email;
    private String inscricaoEstadual;
    private Boolean ativo;
    private Integer comprasRealizadas;
    private List<ContatoClienteResponseDTO> contato;
    private Integer fkVendedorCadastro;

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

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
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

    public Integer getFkVendedorCadastro() {
        return fkVendedorCadastro;
    }

    public void setFkVendedorCadastro(Integer fkVendedorCadastro) {
        this.fkVendedorCadastro = fkVendedorCadastro;
    }

    public Integer getComprasRealizadas() {
        return comprasRealizadas;
    }

    public void setComprasRealizadas(Integer comprasRealizadas) {
        this.comprasRealizadas = comprasRealizadas;
    }

    public List<ContatoClienteResponseDTO> getContato() {
        return contato;
    }

    public void setContato(List<ContatoClienteResponseDTO> contato) {
        this.contato = contato;
    }
}
