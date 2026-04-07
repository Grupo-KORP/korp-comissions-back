package com.comissions.korp.DTO.PedidoDTO;

import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoRequest;

import java.time.LocalDate;
import java.util.List;

public class PedidoRequest {

    private LocalDate dataPedido;
    private Integer numeroNotaDistribuidor;
    private Double valorTotalRevenda;
    private Double valorTotalFaturamento;
    private String statusPedido;
    private Boolean frete;
    private String transportadora;
    private String observacoes;
    private Integer fkVendedor;
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

    public Integer getFkVendedor() {
        return fkVendedor;
    }

    public void setFkVendedor(Integer fkVendedor) {
        this.fkVendedor = fkVendedor;
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

    public Integer getNumeroNotaDistribuidor() {
        return numeroNotaDistribuidor;
    }

    public void setNumeroNotaDistribuidor(Integer numeroNotaDistribuidor) {
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

    public Double getValorTotalFaturamento() {
        return valorTotalFaturamento;
    }

    public void setValorTotalFaturamento(Double valorTotalFaturamento) {
        this.valorTotalFaturamento = valorTotalFaturamento;
    }

    public Double getValorTotalRevenda() {
        return valorTotalRevenda;
    }

    public void setValorTotalRevenda(Double valorTotalRevenda) {
        this.valorTotalRevenda = valorTotalRevenda;
    }
}
