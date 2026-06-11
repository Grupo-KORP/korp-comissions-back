package com.comissions.korp.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UsuarioResponseDTO {
    private Integer idUsuario;
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private java.math.BigDecimal percentualComissao;
    private Boolean primeiroAcesso;
    private Boolean ativo;
    private LocalDateTime dtCriacao;
    private LocalDateTime expiracaoToken;
    private String token;


    public UsuarioResponseDTO(Integer idUsuario, String nome, String email, String senha, String telefone, BigDecimal percentualComissao, Boolean primeiroAcesso, Boolean ativo, LocalDateTime dtCriacao, LocalDateTime expiracaoToken, String token) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.percentualComissao = percentualComissao;
        this.primeiroAcesso = primeiroAcesso;
        this.ativo = ativo;
        this.dtCriacao = dtCriacao;
        this.expiracaoToken = expiracaoToken;
        this.token = token;
    }

    public UsuarioResponseDTO() {}

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public BigDecimal getPercentualComissao() {
        return percentualComissao;
    }

    public void setPercentualComissao(BigDecimal percentualComissao) {
        this.percentualComissao = percentualComissao;
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

    public LocalDateTime getDtCriacao() {
        return dtCriacao;
    }

    public void setDtCriacao(LocalDateTime dtCriacao) {
        this.dtCriacao = dtCriacao;
    }

    public LocalDateTime getExpiracaoToken() {
        return expiracaoToken;
    }

    public void setExpiracaoToken(LocalDateTime expiracaoToken) {
        this.expiracaoToken = expiracaoToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
