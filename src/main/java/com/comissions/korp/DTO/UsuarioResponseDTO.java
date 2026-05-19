package com.comissions.korp.DTO;

public class UsuarioResponseDTO {
    private Integer idUsuario;
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private java.math.BigDecimal percentualComissao;
    private Boolean primeiroAcesso;
    private Boolean ativo;
    private String dtCriacao;


    public UsuarioResponseDTO(Integer idUsuario, String nome, String email, String senha, String telefone, java.math.BigDecimal percentualComissao) {
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

    public java.math.BigDecimal getPercentualComissao() {
        return percentualComissao;
    }

    public Boolean getPrimeiroAcesso() {
        return primeiroAcesso;
    }

    public void setPrimeiroAcesso(Boolean primeiroAcesso) {
        this.primeiroAcesso = primeiroAcesso;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getDtCriacao() {
        return dtCriacao;
    }

    public void setDtCriacao(String dtCriacao) {
        this.dtCriacao = dtCriacao;
    }
}
