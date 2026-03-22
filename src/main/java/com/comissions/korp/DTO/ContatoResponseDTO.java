package com.comissions.korp.DTO;

public class ContatoResponseDTO {
    private Integer idContato;
    private String nome;
    private String email;
    private String telefone;
    private Integer fkCliente;
    private Integer fkDistribuidor;

    public ContatoResponseDTO() {
    }

    public ContatoResponseDTO(Integer idContato, String nome, String email, String telefone, Integer fkCliente, Integer fkDistribuidor){
        this.idContato = idContato;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.fkCliente = fkCliente;
        this.fkDistribuidor = fkDistribuidor;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getIdContato() {
        return idContato;
    }

    public void setIdContato(Integer idContato) {
        this.idContato = idContato;
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
}
