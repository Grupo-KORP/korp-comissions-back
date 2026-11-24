package com.comissions.korp.DTO.PedidoDTO;

import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoResumoResponse;
import com.comissions.korp.entity.ENUM.MetodoPagamento;

import java.time.LocalDate;
import java.util.List;

public class PedidoResponse {

    private Integer idPedido;
    private LocalDate dataPedido;
    private String numeroNotaDistribuidor;
    private java.math.BigDecimal valorTotalDistr;
    private java.math.BigDecimal valorTotalCliente;
    private String statusPedido;
    private Boolean frete;
    private String transportadora;
    private String observacoes;
    private MetodoPagamento metodoPagamento;
    private Integer fkCliente;
    private Integer fkDistribuidor;
    private Integer fkVendedor;
    private String cliente;
    private java.math.BigDecimal valorFaturado;
    private java.math.BigDecimal comissao;
    private String pagamento;
    private String status;
    private List<ItemPedidoResumoResponse> itens;

    public PedidoResponse() {
    }

    public Integer getFkVendedor() {
        return fkVendedor;
    }

    public void setFkVendedor(Integer fkVendedor) {
        this.fkVendedor = fkVendedor;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public java.math.BigDecimal getValorFaturado() {
        return valorFaturado;
    }

    public void setValorFaturado(java.math.BigDecimal valorFaturado) {
        this.valorFaturado = valorFaturado;
    }

    public java.math.BigDecimal getComissao() {
        return comissao;
    }

    public void setComissao(java.math.BigDecimal comissao) {
        this.comissao = comissao;
    }

    public String getPagamento() {
        return pagamento;
    }

    public void setPagamento(String pagamento) {
        this.pagamento = pagamento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
