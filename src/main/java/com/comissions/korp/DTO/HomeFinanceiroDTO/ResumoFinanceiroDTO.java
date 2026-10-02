package com.comissions.korp.DTO.HomeFinanceiroDTO;

import java.math.BigDecimal;

/** Resposta de GET /financeiro/home/resumo (cards do painel financeiro). */
public record ResumoFinanceiroDTO(
        BigDecimal faturamentoTotalEstimado,
        String tendenciaFaturamento,
        Long totalVendas,
        String tendenciaVendas,
        BigDecimal pagamentosPendentes,
        Long qtdPagamentosPendentes,
        BigDecimal comissoesPagas,
        Long qtdVendasComissoesPagas,
        BigDecimal comissaoAPagar,
        Long qtdVendasComissaoAPagar
) {
}
