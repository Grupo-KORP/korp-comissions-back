package com.comissions.korp.DTO;

public class UsuarioResponseDTO {
    private Integer idUsuario;
    private String nome;
    private String email;
    private String senha;
    private String telefone;


    public UsuarioResponseDTO(Integer idUsuario, String nome, String email, String senha, String telefone) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
    }

    public UsuarioResponseDTO() {}

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getTelefone() {
        return telefone;
    }
}
