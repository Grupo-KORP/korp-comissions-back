package com.comissions.korp.DTO;

import com.comissions.korp.entity.ENUM.MetodoPagamento;

import java.util.List;

public class PagamentoDTO {
    private MetodoPagamento metodoPagamento;
    private List<Integer> datasPagamento;

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
}
