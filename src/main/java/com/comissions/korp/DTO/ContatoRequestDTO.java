package com.comissions.korp.DTO;

public class ContatoRequestDTO {
    private String nome;
    private String email;
    private String telefone;
    private Integer fkCliente;
    private Integer fkDistribuidor;

    public ContatoRequestDTO() {
    }

    public ContatoRequestDTO(String email, String nome, String telefone) {
        this.email = email;
        this.nome = nome;
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Integer getFkCliente() {
        return fkCliente;
    }

    public void setFkCliente(Integer fkCliente) {
        this.fkCliente = fkCliente;
    }

    public Integer getFkDistribuidor() {
        return fkDistribuidor;
    }

    public void setFkDistribuidor(Integer fkDistribuidor) {
        this.fkDistribuidor = fkDistribuidor;
    }
}
