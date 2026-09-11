package com.comissions.korp.service.Email.dto;

public record EnviarPedidoDistribuidorRequest(
        String emailDistribuidor,
        String nomeDistribuidor,
        String codigoPedido,
        String nomeCliente,
        String pdfBase64
) {
}