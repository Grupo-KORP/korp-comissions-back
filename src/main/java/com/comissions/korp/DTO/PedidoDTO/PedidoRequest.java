package com.comissions.korp.DTO.PedidoDTO;

import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoRequest;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

public class PedidoRequest {

    private LocalDate dataPedido;
    private String numeroNotaDistribuidor;
    private BigDecimal valorTotalDistr;
    private BigDecimal valorTotalCliente;
    private String statusPedido;
    private Boolean frete;
    private String transportadora;
    private String observacoes;
    private Integer fkCliente;
    private Integer fkDistribuidor;
    private List<ItemPedidoRequest> itens;


    public PedidoRequest() {
    }

    public LocalDate getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDate dataPedido) {
        this.dataPedido = dataPedido;
    }

    public Integer getFkCliente() {
        return fkCliente;
    }

    public void setFkCliente(Integer fkCliente) {
        this.fkCliente = fkCliente;
    }

    public Integer getFkDistribuidor() {
        return fkDistribuidor;
    }

    public void setFkDistribuidor(Integer fkDistribuidor) {
        this.fkDistribuidor = fkDistribuidor;
    }

    public Boolean getFrete() {
        return frete;
    }

    public void setFrete(Boolean frete) {
        this.frete = frete;
    }

    public List<ItemPedidoRequest> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoRequest> itens) {
        this.itens = itens;
    }

    public String getNumeroNotaDistribuidor() {
        return numeroNotaDistribuidor;
    }

    public void setNumeroNotaDistribuidor(String numeroNotaDistribuidor) {
        this.numeroNotaDistribuidor = numeroNotaDistribuidor;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getStatusPedido() {
        return statusPedido;
    }

    public void setStatusPedido(String statusPedido) {
        this.statusPedido = statusPedido;
    }

    public String getTransportadora() {
        return transportadora;
    }

    public void setTransportadora(String transportadora) {
        this.transportadora = transportadora;
    }

    public BigDecimal getValorTotalFaturamento() {
        return valorTotalCliente;
    }

    public void setValorTotalFaturamento(BigDecimal valorTotalFaturamento) {
        this.valorTotalCliente = valorTotalFaturamento;
    }

    public BigDecimal getValorTotalRevenda() {
        return valorTotalDistr;
    }

    public void setValorTotalRevenda(BigDecimal valorTotalRevenda) {
        this.valorTotalDistr = valorTotalRevenda;
    }

    public BigDecimal getValorTotalCliente() {
        return valorTotalCliente;
    }

    public void setValorTotalCliente(BigDecimal valorTotalCliente) {
        this.valorTotalCliente = valorTotalCliente;
    }

    public BigDecimal getValorTotalDistr() {
        return valorTotalDistr;
    }

    public void setValorTotalDistr(BigDecimal valorTotalDistr) {
        this.valorTotalDistr = valorTotalDistr;
    }
}
