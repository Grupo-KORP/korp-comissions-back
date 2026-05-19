package com.comissions.korp.DTO.PedidoDTO;

import java.time.LocalDate;

public class PedidoResumidoResponse {

    private Integer idPedido;
    private LocalDate dataPedido;
    private String numeroNotaDistribuidor;
    private java.math.BigDecimal valorTotalDistr;
    private java.math.BigDecimal valorTotalCliente;
    private String statusPedido;

    public PedidoResumidoResponse() {
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

    public java.math.BigDecimal getValorTotalDistr() {
        return valorTotalDistr;
    }

    public void setValorTotalDistr(java.math.BigDecimal valorTotalDistr) {
        this.valorTotalDistr = valorTotalDistr;
    }

    public java.math.BigDecimal getValorTotalCliente() {
        return valorTotalCliente;
    }

    public void setValorTotalCliente(java.math.BigDecimal valorTotalCliente) {
        this.valorTotalCliente = valorTotalCliente;
    }

    public String getStatusPedido() {
        return statusPedido;
    }

    public void setStatusPedido(String statusPedido) {
        this.statusPedido = statusPedido;
    }
}
