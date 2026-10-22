package com.comissions.korp.DTO.PedidoDTO;

import com.comissions.korp.DTO.ClienteDTO.ClientePedidoResponseDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorPedidoResponseDTO;
import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoRequest;
import com.comissions.korp.entity.ENUM.MetodoPagamento;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

public class PedidoRequest {

    private ClientePedidoResponseDTO cliente;
    private DistribuidorPedidoResponseDTO distribuidor;
    private List<ItemPedidoRequest> itens;
    private String pdfBase64;
    private MetodoPagamento metodoPagamento;
    private Integer idRascunho;

    public PedidoRequest() {
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    /** Rascunho que originou o pedido; é apagado junto com a criação. */
    public Integer getIdRascunho() {
        return idRascunho;
    }

    public void setIdRascunho(Integer idRascunho) {
        this.idRascunho = idRascunho;
    }

    public String getPdfBase64() {
        return pdfBase64;
    }

    public void setPdfBase64(String pdfBase64) {
        this.pdfBase64 = pdfBase64;
    }

    public ClientePedidoResponseDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClientePedidoResponseDTO cliente) {
        this.cliente = cliente;
    }

    public DistribuidorPedidoResponseDTO getDistribuidor() {
        return distribuidor;
    }

    public void setDistribuidor(DistribuidorPedidoResponseDTO distribuidor) {
        this.distribuidor = distribuidor;
    }

    public List<ItemPedidoRequest> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoRequest> itens) {
        this.itens = itens;
    }
}
