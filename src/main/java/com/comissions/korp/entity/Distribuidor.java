package com.comissions.korp.entity;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="distribuidor")
public class Distribuidor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_distribuidor")
    private Integer idDistribuidor;

    @Column(name = "razao_social", length = 150, nullable = false)
    private String razaoSocial;

    @Column(name = "nome_fantasia", length = 150)
    private String nomeFantasia;

    @Column(name = "cnpj", length = 20, nullable = false, unique = true)
    private String cnpj;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "email", length = 120)
    private String email;

    public Distribuidor() {
    }

    public Distribuidor(String cnpj, String email, String nomeFantasia, String razaoSocial, String telefone) {
        this.cnpj = cnpj;
        this.email = email;
        this.nomeFantasia = nomeFantasia;
        this.razaoSocial = razaoSocial;
        this.telefone = telefone;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getIdDistribuidor() {
        return idDistribuidor;
    }

    public void setIdDistribuidor(Integer idDistribuidor) {
        this.idDistribuidor = idDistribuidor;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
