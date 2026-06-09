package com.comissions.korp.DTO.ContatoDTO;

public class ContatoDistribuidorResponseDTO {

    private Integer idContato;
    private String nome;
    private String email;
    private String telefone;
    private Integer idDistribuidor;

    public ContatoDistribuidorResponseDTO() {
    }

    public ContatoDistribuidorResponseDTO(Integer idContato, String nome, String email, String telefone, Integer idDistribuidor) {
        this.idContato = idContato;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.idDistribuidor = idDistribuidor;
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

    public Integer getIdDistribuidor() {
        return idDistribuidor;
    }

    public void setIdDistribuidor(Integer idDistribuidor) {
        this.idDistribuidor = idDistribuidor;
    }
}
