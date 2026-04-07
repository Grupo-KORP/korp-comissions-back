package com.comissions.korp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.hibernate.annotations.CreationTimestamp;
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

    @Column(name = "email", unique = true, nullable = false, length = 120)
    @Email(message = "Formato do email deve ser válido (ex: nome@dominio.com).") //nome@dominio.com
    private String email;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "telefone", nullable = false)
    @Length(min = 10, message = "Formato do numero de telefone deve ser válido (ex: xxxxxxxxxxx).")
    private String telefone;

    @CreationTimestamp
    @Column(name = "dtCriacao")
    private LocalDateTime dtCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fkRole")
    private Role roles;

    public Usuario(Integer idUsuario, String nome, String email, String senha, String telefone, LocalDateTime dtCriacao, Role roles) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.dtCriacao = dtCriacao;
        this.roles = roles;
    }

    public Usuario() {
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

    public Role getRoles() {
        return roles;
    }

    public void setRoles(Role roles) {
        this.roles = roles;
    }
}
