package com.comissions.korp.DTO.PedidoDTO;

import com.comissions.korp.entity.ENUM.MetodoPagamento;

import java.util.List;

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
    private List<Integer> datasPagamento;
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

    /** Prazo, em dias após a finalização do pedido, de cada parcela. */
    public List<Integer> getDatasPagamento() {
        return datasPagamento;
    }

    public void setDatasPagamento(List<Integer> datasPagamento) {
        this.datasPagamento = datasPagamento;
    }

    public Boolean getFinalizarPedido() {
        return finalizarPedido;
    }

    public void setFinalizarPedido(Boolean finalizarPedido) {
        this.finalizarPedido = finalizarPedido;
    }
}
