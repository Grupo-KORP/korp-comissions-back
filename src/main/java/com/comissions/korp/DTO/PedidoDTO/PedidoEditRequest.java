package com.comissions.korp.DTO.PedidoDTO;

import com.comissions.korp.entity.ENUM.MetodoPagamento;

public class PedidoEditRequest {
    private Integer idPedido;
    private Integer idCliente;
    private String nomeFantasiaCliente;
    private Integer idDistribuidor;
    private String nomeFantasiaDistribuidor;
    private Integer idItemPedido;
    private Integer quantidade;
    private String numeroNotaDistribuidor;
    private String observacoes;
    private MetodoPagamento metodoPagamento;
    private Boolean parcelado;
    private Integer quantidadeParcelas;
    private Boolean finalizarPedido;

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNomeFantasiaCliente() {
        return nomeFantasiaCliente;
    }

    public void setNomeFantasiaCliente(String nomeFantasiaCliente) {
        this.nomeFantasiaCliente = nomeFantasiaCliente;
    }

    public Integer getIdDistribuidor() {
        return idDistribuidor;
    }

    public void setIdDistribuidor(Integer idDistribuidor) {
        this.idDistribuidor = idDistribuidor;
    }

    public String getNomeFantasiaDistribuidor() {
        return nomeFantasiaDistribuidor;
    }

    public void setNomeFantasiaDistribuidor(String nomeFantasiaDistribuidor) {
        this.nomeFantasiaDistribuidor = nomeFantasiaDistribuidor;
    }

    public Integer getIdItemPedido() {
        return idItemPedido;
    }

    public void setIdItemPedido(Integer idItemPedido) {
        this.idItemPedido = idItemPedido;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
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

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public Boolean getParcelado() {
        return parcelado;
    }

    public void setParcelado(Boolean parcelado) {
        this.parcelado = parcelado;
    }

    public Integer getQuantidadeParcelas() {
        return quantidadeParcelas;
    }

    public void setQuantidadeParcelas(Integer quantidadeParcelas) {
        this.quantidadeParcelas = quantidadeParcelas;
    }

    public Boolean getFinalizarPedido() {
        return finalizarPedido;
    }

    public void setFinalizarPedido(Boolean finalizarPedido) {
        this.finalizarPedido = finalizarPedido;
    }
}
