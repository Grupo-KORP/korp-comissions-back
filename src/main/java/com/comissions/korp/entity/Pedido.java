package com.comissions.korp.entity;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPedido")
    private Integer idPedido;

    @Column(nullable = false)
    private LocalDate dataPedido;

    private Integer numeroNotaDistribuidor;

    @Column(name = "valorTotalRevenda", nullable = false, precision = 10, scale = 2)
    private Double valorTotalRevenda;

    @Column(name = "valorTotalFaturamento", nullable = false, precision = 10, scale = 2)
    private Double valorTotalFaturamento;

    @Column(name = "statusPedido", nullable = false, length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'PENDENTE'")
    private String statusPedido;

    @Column(name = "frete")
    private Boolean frete;

    @Column(length = 150)
    private String transportadora;

    @Column(length = 250)
    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "fkVendedor", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "fkCliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "fkDistribuidor", nullable = false)
    private Distribuidor distribuidor;

    public Pedido() {
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public LocalDate getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDate dataPedido) {
        this.dataPedido = dataPedido;
    }

    public Integer getNumeroNotaDistribuidor() {
        return numeroNotaDistribuidor;
    }

    public void setNumeroNotaDistribuidor(Integer numeroNotaDistribuidor) {
        this.numeroNotaDistribuidor = numeroNotaDistribuidor;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Double getValorTotalRevenda() {
        return valorTotalRevenda;
    }

    public void setValorTotalRevenda(Double valorTotalRevenda) {
        this.valorTotalRevenda = valorTotalRevenda;
    }

    public Double getValorTotalFaturamento() {
        return valorTotalFaturamento;
    }

    public void setValorTotalFaturamento(Double valorTotalFaturamento) {
        this.valorTotalFaturamento = valorTotalFaturamento;
    }

    public String getStatusPedido() {
        return statusPedido;
    }

    public void setStatusPedido(String statusPedido) {
        this.statusPedido = statusPedido;
    }

    public Boolean getFrete() {
        return frete;
    }

    public void setFrete(Boolean frete) {
        this.frete = frete;
    }

    public String getTransportadora() {
        return transportadora;
    }

    public void setTransportadora(String transportadora) {
        this.transportadora = transportadora;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Distribuidor getDistribuidor() {
        return distribuidor;
    }

    public void setDistribuidor(Distribuidor distribuidor) {
        this.distribuidor = distribuidor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
