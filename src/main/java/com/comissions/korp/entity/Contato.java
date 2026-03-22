package com.comissions.korp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Contato")
public class Contato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contato")
    private Integer idContato;

    @Column(name = "nome", length = 250, nullable = false)
    private String nome;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "telefone", length = 20)
    private String telefone;

    // Relacionamento N:1 com Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_cliente")
    private Cliente cliente;

    // Relacionamento N:1 com Distribuidor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_distribuidor")
    private Distribuidor distribuidor;

    public Contato() {
    }

    public Contato(Cliente cliente, Distribuidor distribuidor, String email, Integer idContato, String nome, String telefone) {
        this.cliente = cliente;
        this.distribuidor = distribuidor;
        this.email = email;
        this.idContato = idContato;
        this.nome = nome;
        this.telefone = telefone;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Distribuidor getDistribuidor() {
        return distribuidor;
    }

    public void setDistribuidor(Distribuidor distribuidor) {
        this.distribuidor = distribuidor;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getIdContato() {
        return idContato;
    }

    public void setIdContato(Integer idContato) {
        this.idContato = idContato;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
