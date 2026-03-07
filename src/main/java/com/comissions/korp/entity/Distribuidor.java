package com.comissions.korp.entity;

public class Distribuidor {


    private String cnpj;
    private String nomeEmpresa;
    private String nomeFantasia;
    private String logradouro;
    private Integer numero;
    private String complemento;
    private Integer cep;
    private String distrito;
    private String municipio;
    private String uf;
    private String email;
    private String telComercial;
    private String telRepresentante;

    public Distribuidor(Integer cep, String cnpj, String complemento, String distrito, String email, String logradouro, String municipio, String nomeEmpresa, String nomeFantasia, Integer numero, String telComercial, String telRepresentante, String uf) {
        this.cep = cep;
        this.cnpj = cnpj;
        this.complemento = complemento;
        this.distrito = distrito;
        this.email = email;
        this.logradouro = logradouro;
        this.municipio = municipio;
        this.nomeEmpresa = nomeEmpresa;
        this.nomeFantasia = nomeFantasia;
        this.numero = numero;
        this.telComercial = telComercial;
        this.telRepresentante = telRepresentante;
        this.uf = uf;
    }

    public Integer getCep() {
        return cep;
    }

    public void setCep(Integer cep) {
        this.cep = cep;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getTelComercial() {
        return telComercial;
    }

    public void setTelComercial(String telComercial) {
        this.telComercial = telComercial;
    }

    public String getTelRepresentante() {
        return telRepresentante;
    }

    public void setTelRepresentante(String telRepresentante) {
        this.telRepresentante = telRepresentante;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }
}
