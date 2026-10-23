package com.comissions.korp.DTO.HomeFinanceiroDTO;

import java.math.BigDecimal;

/** Ponto do gráfico de GET /financeiro/home/evolucao-vendas ("mes" já vem no formato "Out/25"). */
public record EvolucaoVendasMesDTO(
        String mes,
        Integer ano,
        Integer numeroMes,
        BigDecimal valor,
        Long quantidadeVendas
) {
}
