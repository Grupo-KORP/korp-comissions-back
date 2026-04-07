package com.comissions.korp.DTO;

import com.comissions.korp.entity.Role;

public class UsuarioRequestDTO{
    private Integer idUsuario;
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private Integer role;

    public UsuarioRequestDTO(Integer idUsuario, String nome, String email, String senha, String telefone, Integer role) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.role = role;
    }

    public UsuarioRequestDTO() {}

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

    public Integer getRole() {
        return role;
    }
}
