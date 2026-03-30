package com.comissions.korp.DTO;

import java.time.LocalDate;

public class PedidoResumidoResponse {

    private Integer idPedido;
    private LocalDate dataPedido;
    private Integer numeroNotaDistribuidor;
    private Double valorTotalRevenda;
    private Double valorTotalFaturamento;
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

    public Integer getNumeroNotaDistribuidor() {
        return numeroNotaDistribuidor;
    }

    public void setNumeroNotaDistribuidor(Integer numeroNotaDistribuidor) {
        this.numeroNotaDistribuidor = numeroNotaDistribuidor;
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
}
