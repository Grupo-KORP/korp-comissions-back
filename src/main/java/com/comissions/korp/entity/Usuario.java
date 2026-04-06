package com.comissions.korp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private Integer idUsuario;

    @Column(name = "nome", length = 250, nullable = false)
    private String nome;

    @Column(name = "email", nullable = false, length = 120)
    @Email(message = "Formato do email deve ser válido (ex: nome@dominio.com).") //nome@dominio.com
    private String email;

    @Transient
    private String senha;

    @Column(name = "telefone", length = 20)
    @Length(min = 10, message = "Formato do numero de telefone deve ser válido (ex: xxxxxxxxxxx).")
    private String telefone;

    @Column(name = "percentualComissao", precision = 5, scale = 2)
    private Double percentualComissao;

    @ManyToOne
    @JoinColumn(name = "fkRole", nullable = false)
    private Role role;

    @Transient
    private LocalDateTime dtCriacao;

    public Usuario() {
    }

    public Usuario(Integer idUsuario, String nome, String email, String senha, String telefone, LocalDateTime dtCriacao) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.dtCriacao = dtCriacao;
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

    public LocalDateTime getDtCriacao() {
        return dtCriacao;
    }

    public void setDtCriacao(LocalDateTime dtCriacao) {
        this.dtCriacao = dtCriacao;
    }

    public Double getPercentualComissao() {
        return percentualComissao;
    }

    public void setPercentualComissao(Double percentualComissao) {
        this.percentualComissao = percentualComissao;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
