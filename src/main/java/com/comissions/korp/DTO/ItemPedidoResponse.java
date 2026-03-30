package com.comissions.korp.DTO;

public class ItemPedidoResponse {

    private Integer idItemPedido;
    private Integer quantidade;
    private Double valorUnitario;
    private PedidoResumidoResponse pedido;
    private ProdutoResponse produto;

    public ItemPedidoResponse() {
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

    public Double getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(Double valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public PedidoResumidoResponse getPedido() {
        return pedido;
    }

    public void setPedido(PedidoResumidoResponse pedido) {
        this.pedido = pedido;
    }

    public ProdutoResponse getProduto() {
        return produto;
    }

    public void setProduto(ProdutoResponse produto) {
        this.produto = produto;
    }
}
