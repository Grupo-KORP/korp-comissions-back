package com.comissions.korp.DTO.PedidoDTO;

import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoResumoResponse;

import java.time.LocalDate;
import java.util.List;

public class PedidoResponse {

    private Integer idPedido;
    private LocalDate dataPedido;
    private Integer numeroNotaDistribuidor;
    private Double valorTotalRevenda;
    private Double valorTotalFaturamento;
    private String statusPedido;
    private Boolean frete;
    private String transportadora;
    private String observacoes;
    private Integer fkCliente;
    private Integer fkDistribuidor;
    private List<ItemPedidoResumoResponse> itens;

    public PedidoResponse() {
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

    public List<ItemPedidoResumoResponse> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoResumoResponse> itens) {
        this.itens = itens;
    }
}
