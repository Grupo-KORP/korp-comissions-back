package com.comissions.korp.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item_pedido")
    private Integer idItemPedido;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "vlr_unit_distr", nullable = false, precision = 10, scale = 2)
    private BigDecimal vlrUnitDistr;

    @Column(name = "vlr_total_distr", nullable = false, precision = 10, scale = 2)
    private BigDecimal vlrTotalDistr;

    @Column(name = "vlr_unit_cliente", nullable = false, precision = 10, scale = 2)
    private BigDecimal vlrUnitCliente;

    @Column(name = "vlr_total_cliente", nullable = false, precision = 10, scale = 2)
    private BigDecimal vlrTotalCliente;

    @ManyToOne
    @JoinColumn(name = "fk_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "fk_produto", nullable = false)
    private Produto produto;

    public ItemPedido() {
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

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }
}
