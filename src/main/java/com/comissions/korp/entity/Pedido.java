package com.comissions.korp.entity;


import com.comissions.korp.entity.ENUM.MetodoPagamento;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "pedido",
        uniqueConstraints = @UniqueConstraint(name = "uc_pedido_data_numero", columnNames = {"data_pedido", "numero_dia"}))
public class Pedido {

    /** Parte de data do código do pedido: dia, mês e ano com dois dígitos (ex.: 251126). */
    private static final DateTimeFormatter FORMATO_DATA_CODIGO = DateTimeFormatter.ofPattern("ddMMyy");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    /** Sequência do pedido no dia da criação, no sistema todo (1º do dia = 1). Nunca muda. */
    @Column(name = "numero_dia", nullable = false)
    private Integer numeroDia;

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

    /** Prazo/condição de entrega do pedido inteiro (ex.: "Imediata", "15 dias"). */
    @Column(name = "entrega", length = 100)
    private String entrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", length = 20)
    private MetodoPagamento metodoPagamento;

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

    public String getEntrega() {
        return entrega;
    }

    public void setEntrega(String entrega) {
        this.entrega = entrega;
    }

    public Integer getNumeroDia() {
        return numeroDia;
    }

    public void setNumeroDia(Integer numeroDia) {
        this.numeroDia = numeroDia;
    }

    /**
     * Código exibido em todo o sistema: data de criação + "-P" + sequência do dia com pelo menos
     * dois dígitos. Ex.: 3º pedido de 25/11/2026 = "251126-P03"; o 100º do dia = "251126-P100".
     */
    public String getCodigo() {
        if (dataPedido == null || numeroDia == null) {
            return null;
        }
        return dataPedido.format(FORMATO_DATA_CODIGO) + "-P" + String.format("%02d", numeroDia);
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

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
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
