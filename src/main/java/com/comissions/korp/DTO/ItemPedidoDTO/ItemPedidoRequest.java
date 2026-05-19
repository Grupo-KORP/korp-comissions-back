package com.comissions.korp.DTO.ItemPedidoDTO;

import com.comissions.korp.entity.Produto;
import java.math.BigDecimal;

public class ItemPedidoRequest {

    private Integer quantidade;
    private BigDecimal vlrUnitDistr;
    private BigDecimal vlrTotalDistr;
    private BigDecimal vlrUnitCliente;
    private BigDecimal vlrTotalCliente;
    private Integer fkProduto;

    public ItemPedidoRequest() {
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getVlrUnitDistr() {
        return vlrUnitDistr;
    }

    public void setVlrUnitDistr(BigDecimal vlrUnitDistr) {
        this.vlrUnitDistr = vlrUnitDistr;
    }

    public BigDecimal getVlrTotalDistr() {
        return vlrTotalDistr;
    }

    public void setVlrTotalDistr(BigDecimal vlrTotalDistr) {
        this.vlrTotalDistr = vlrTotalDistr;
    }

    public BigDecimal getVlrUnitCliente() {
        return vlrUnitCliente;
    }

    public void setVlrUnitCliente(BigDecimal vlrUnitCliente) {
        this.vlrUnitCliente = vlrUnitCliente;
    }

    public BigDecimal getVlrTotalCliente() {
        return vlrTotalCliente;
    }

    public void setVlrTotalCliente(BigDecimal vlrTotalCliente) {
        this.vlrTotalCliente = vlrTotalCliente;
    }

    public Integer getFkProduto() {
        return fkProduto;
    }

    public void setFkProduto(Integer fkProduto) {
        this.fkProduto = fkProduto;
    }
}
