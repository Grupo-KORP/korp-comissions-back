package com.comissions.korp.DTO.PedidoDTO;

import com.comissions.korp.entity.ENUM.MetodoPagamento;

import java.util.List;

public class PedidoEditRequest {
    private Integer idPedido;
    /** Nova quantidade de cada item; itens que não vierem na lista ficam como estão. */
    private List<ItemQuantidade> itens;
    private String numeroNotaDistribuidor;
    private String observacoes;
    /** Prazo/condição de entrega do pedido; null mantém o valor atual, texto vazio limpa. */
    private String entrega;
    private MetodoPagamento metodoPagamento;
    private List<Integer> datasPagamento;
    private Boolean finalizarPedido;

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public List<ItemQuantidade> getItens() {
        return itens;
    }

    public void setItens(List<ItemQuantidade> itens) {
        this.itens = itens;
    }

    /** Item do pedido e a quantidade que ele passa a ter. */
    public static class ItemQuantidade {
        private Integer idItemPedido;
        private Integer quantidade;

        public ItemQuantidade() {
        }

        public ItemQuantidade(Integer idItemPedido, Integer quantidade) {
            this.idItemPedido = idItemPedido;
            this.quantidade = quantidade;
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
    }

    public String getEntrega() {
        return entrega;
    }

    public void setEntrega(String entrega) {
        this.entrega = entrega;
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
