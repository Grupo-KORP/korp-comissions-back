package com.comissions.korp.DTO.ItemPedidoDTO;

import com.comissions.korp.DTO.ProdutoDTO.ProdutoResponse;

public class ItemPedidoResumoResponse {

    private Integer idItemPedido;
    private Integer quantidade;
    private java.math.BigDecimal vlrUnitDistr;
    private java.math.BigDecimal vlrTotalDistr;
    private java.math.BigDecimal vlrUnitCliente;
    private java.math.BigDecimal vlrTotalCliente;
    private ProdutoResponse produto;

    public ItemPedidoResumoResponse() {
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

    public java.math.BigDecimal getVlrUnitDistr() {
        return vlrUnitDistr;
    }

    public void setVlrUnitDistr(java.math.BigDecimal vlrUnitDistr) {
        this.vlrUnitDistr = vlrUnitDistr;
    }

    public java.math.BigDecimal getVlrTotalDistr() {
        return vlrTotalDistr;
    }

    public void setVlrTotalDistr(java.math.BigDecimal vlrTotalDistr) {
        this.vlrTotalDistr = vlrTotalDistr;
    }

    public java.math.BigDecimal getVlrUnitCliente() {
        return vlrUnitCliente;
    }

    public void setVlrUnitCliente(java.math.BigDecimal vlrUnitCliente) {
        this.vlrUnitCliente = vlrUnitCliente;
    }

    public java.math.BigDecimal getVlrTotalCliente() {
        return vlrTotalCliente;
    }

    public void setVlrTotalCliente(java.math.BigDecimal vlrTotalCliente) {
        this.vlrTotalCliente = vlrTotalCliente;
    }

    public ProdutoResponse getProduto() {
        return produto;
    }

    public void setProduto(ProdutoResponse produto) {
        this.produto = produto;
    }
}
