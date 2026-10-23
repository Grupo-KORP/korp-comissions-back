package com.comissions.korp.DTO.HomeFinanceiroDTO;

import java.math.BigDecimal;

/** Total de comissão de um pedido; instanciado pela query em FinanceiroRepository. */
public record ComissaoPedidoDTO(Integer idPedido, BigDecimal total) {
}
