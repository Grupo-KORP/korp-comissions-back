package com.comissions.korp.DTO.HomeFinanceiroDTO;

import java.math.BigDecimal;

/**
 * Linha de GET /financeiro/home/ranking-vendedores.
 * Instanciado direto pela query JPQL (constructor expression) em FinanceiroRepository.
 */
public record RankingVendedorDTO(
        Integer idVendedor,
        String nome,
        BigDecimal valor,
        Long quantidadeVendas
) {
}
