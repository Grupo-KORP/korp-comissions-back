package com.comissions.korp.entity;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    @Column(name = "numero_nota_distribuidor", length = 20)
    private String numeroNotaDistribuidor;

    @Column(name = "valor_total_distr", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotalDistr;

    @Column(name = "valor_total_cliente", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotalCliente;

    @Column(name = "status_pedido", nullable = false, length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'PENDENTE'")
    private String statusPedido;

    @Column(name = "frete")
    private Boolean frete;

    @Column(length = 150)
    private String transportadora;

    @Column(length = 250)
    private String observacoes;

    @Column(name = "ativo", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "fk_vendedor", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "fk_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "fk_distribuidor", nullable = false)
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

    public String getNumeroNotaDistribuidor() {
        return numeroNotaDistribuidor;
    }

    public void setNumeroNotaDistribuidor(String numeroNotaDistribuidor) {
        this.numeroNotaDistribuidor = numeroNotaDistribuidor;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public BigDecimal getValorTotalRevenda() {
        return valorTotalDistr;
    }

    public void setValorTotalRevenda(BigDecimal valorTotalRevenda) {
        this.valorTotalDistr = valorTotalRevenda;
    }

    public BigDecimal getValorTotalFaturamento() {
        return valorTotalCliente;
    }

    public void setValorTotalFaturamento(BigDecimal valorTotalFaturamento) {
        this.valorTotalCliente = valorTotalFaturamento;
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

    public BigDecimal getValorTotalDistr() {
        return valorTotalDistr;
    }

    public void setValorTotalDistr(BigDecimal valorTotalDistr) {
        this.valorTotalDistr = valorTotalDistr;
    }

    public BigDecimal getValorTotalCliente() {
        return valorTotalCliente;
    }

    public void setValorTotalCliente(BigDecimal valorTotalCliente) {
        this.valorTotalCliente = valorTotalCliente;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
