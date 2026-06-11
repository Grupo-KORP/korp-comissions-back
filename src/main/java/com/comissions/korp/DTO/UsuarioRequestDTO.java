package com.comissions.korp.DTO;

import com.comissions.korp.entity.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UsuarioRequestDTO{
    private Integer idUsuario;
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private Integer role;
    private BigDecimal percentualComissao;
    private Boolean primeiroAcesso;
    private Boolean ativo;
    private String dtCriacao;
    private String token;
    private LocalDateTime expiracaoToken;

    public UsuarioRequestDTO(Integer idUsuario, String nome, String email, String senha, String telefone, Integer role, BigDecimal percentualComissao, Boolean primeiroAcesso, Boolean ativo, String dtCriacao, String token, LocalDateTime expiracaoToken) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.role = role;
        this.percentualComissao = percentualComissao;
        this.primeiroAcesso = primeiroAcesso;
        this.ativo = ativo;
        this.dtCriacao = dtCriacao;
        this.token = token;
        this.expiracaoToken = expiracaoToken;
    }

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

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
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

    public String getDtCriacao() {
        return dtCriacao;
    }

    public void setDtCriacao(String dtCriacao) {
        this.dtCriacao = dtCriacao;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiracaoToken() {
        return expiracaoToken;
    }

    public void setExpiracaoToken(LocalDateTime expiracaoToken) {
        this.expiracaoToken = expiracaoToken;
    }
}
