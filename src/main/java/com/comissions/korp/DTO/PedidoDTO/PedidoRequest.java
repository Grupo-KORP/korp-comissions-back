package com.comissions.korp.DTO.PedidoDTO;

import com.comissions.korp.DTO.ClienteDTO.ClientePedidoResponseDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorPedidoResponseDTO;
import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoRequest;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

public class PedidoRequest {

    private ClientePedidoResponseDTO cliente;
    private DistribuidorPedidoResponseDTO distribuidor;
    private List<ItemPedidoRequest> itens;

    public PedidoRequest() {
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
