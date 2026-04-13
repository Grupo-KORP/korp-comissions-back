package com.comissions.korp.DTO;

public class UsuarioResponseDTO {
    private Integer idUsuario;
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private Double percentualComissao;


    public UsuarioResponseDTO(Integer idUsuario, String nome, String email, String senha, String telefone, Double percentualComissao) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.percentualComissao = percentualComissao;
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

    public Double getPercentualComissao() {
        return percentualComissao;
    }
}
