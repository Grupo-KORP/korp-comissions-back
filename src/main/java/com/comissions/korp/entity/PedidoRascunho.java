package com.comissions.korp.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pedido_rascunho")
public class PedidoRascunho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rascunho")
    private Integer idRascunho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_vendedor", nullable = false)
    private Usuario usuario;

    @Column(name = "dados", nullable = false, columnDefinition = "LONGTEXT")
    private String dados;

    @Column(name = "versao_schema", nullable = false)
    private Integer versaoSchema;

    @Column(name = "nome_cliente", length = 150)
    private String nomeCliente;

    @Column(name = "nome_distribuidor", length = 150)
    private String nomeDistribuidor;

    @Column(name = "qtd_produtos", nullable = false)
    private Integer qtdProdutos;

    @Column(name = "metodo_pagamento", length = 20)
    private String metodoPagamento;

    @Version
    @Column(name = "versao", nullable = false)
    private Integer versao;

    @Column(name = "dt_criacao", nullable = false)
    private LocalDateTime dtCriacao;

    @Column(name = "dt_atualizacao", nullable = false)
    private LocalDateTime dtAtualizacao;

    public PedidoRascunho() {
    }

    public Integer getIdRascunho() {
        return idRascunho;
    }

    public void setIdRascunho(Integer idRascunho) {
        this.idRascunho = idRascunho;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getDados() {
        return dados;
    }

    public void setDados(String dados) {
        this.dados = dados;
    }

    public Integer getVersaoSchema() {
        return versaoSchema;
    }

    public void setVersaoSchema(Integer versaoSchema) {
        this.versaoSchema = versaoSchema;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getNomeDistribuidor() {
        return nomeDistribuidor;
    }

    public void setNomeDistribuidor(String nomeDistribuidor) {
        this.nomeDistribuidor = nomeDistribuidor;
    }

    public Integer getQtdProdutos() {
        return qtdProdutos;
    }

    public void setQtdProdutos(Integer qtdProdutos) {
        this.qtdProdutos = qtdProdutos;
    }

    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public Integer getVersao() {
        return versao;
    }

    public void setVersao(Integer versao) {
        this.versao = versao;
    }

    public LocalDateTime getDtCriacao() {
        return dtCriacao;
    }

    public void setDtCriacao(LocalDateTime dtCriacao) {
        this.dtCriacao = dtCriacao;
    }

    public LocalDateTime getDtAtualizacao() {
        return dtAtualizacao;
    }

    public void setDtAtualizacao(LocalDateTime dtAtualizacao) {
        this.dtAtualizacao = dtAtualizacao;
    }
}
