package com.comissions.korp.DTO;

import com.comissions.korp.entity.Role;
import java.math.BigDecimal;

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

    public UsuarioRequestDTO(Integer idUsuario, String nome, String email, String senha, String telefone, Integer role, BigDecimal percentualComissao) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.role = role;
        this.percentualComissao = percentualComissao;
    }

    public BigDecimal getPercentualComissao() {
        return percentualComissao;
    }

    public UsuarioRequestDTO() {}

    public Integer getIdUsuario() {
        return idUsuario;
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
