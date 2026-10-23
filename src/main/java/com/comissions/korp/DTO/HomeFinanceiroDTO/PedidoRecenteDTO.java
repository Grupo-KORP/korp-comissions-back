package com.comissions.korp.DTO.HomeFinanceiroDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Linha de GET /financeiro/home/pedidos. */
public record PedidoRecenteDTO(
        Integer idPedido,
        String codigo,
        LocalDate dataPedido,
        String vendedor,
        String cliente,
        BigDecimal valorFaturado,
        BigDecimal comissao,
        String pagamento,
        String status
) {
}
