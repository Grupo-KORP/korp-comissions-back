package com.comissions.korp.entity;

import com.comissions.korp.entity.ENUM.MetodoPagamento;
import jakarta.persistence.*;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagamento")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", nullable = false)
    private MetodoPagamento metodoPagamento;

    @Column(name = "parcelado", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean parcelado;

    @Column(name = "quantidade_parcelas")
    private Integer quantidadeParcelas;

    @OneToOne
    @JoinColumn(name = "fk_pedido", nullable = false, unique = true)
    private Pedido pedido;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
}
