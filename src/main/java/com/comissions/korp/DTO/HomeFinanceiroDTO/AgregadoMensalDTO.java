package com.comissions.korp.DTO.HomeFinanceiroDTO;

import java.math.BigDecimal;

/** Vendas (quantidade e faturamento) agrupadas por mês; instanciado pela query em FinanceiroRepository. */
public record AgregadoMensalDTO(Integer ano, Integer mes, Long quantidade, BigDecimal total) {
}
