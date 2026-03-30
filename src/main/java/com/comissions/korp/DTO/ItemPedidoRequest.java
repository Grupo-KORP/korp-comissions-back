package com.comissions.korp.DTO;

import com.comissions.korp.entity.Produto;

public class ItemPedidoRequest {

    private Integer quantidade;
    private Double valorUnitario;
    private Produto fkProduto;

    public ItemPedidoRequest() {
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

    public Produto getFkProduto() {
        return fkProduto;
    }

    public void setFkProduto(Produto fkProduto) {
        this.fkProduto = fkProduto;
    }
}
