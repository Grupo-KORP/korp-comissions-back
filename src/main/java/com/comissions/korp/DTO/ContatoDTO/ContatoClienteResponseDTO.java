package com.comissions.korp.DTO.ContatoDTO;

public class ContatoClienteResponseDTO {
    private Integer idContato;
    private String nome;
    private String email;
    private Integer idCliente;

    public ContatoClienteResponseDTO() {
    }

    public ContatoClienteResponseDTO(Integer idContato, String nome, String email, String telefone, Integer idCliente, Integer idDistribuidor){
        this.idContato = idContato;
        this.nome = nome;
        this.email = email;
        this.idCliente = idCliente;
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

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }
}
