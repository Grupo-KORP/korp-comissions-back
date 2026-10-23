package com.comissions.korp.DTO.HomeFinanceiroDTO;

import java.math.BigDecimal;

/** Agregado simples (quantidade e soma); instanciado pelas queries em FinanceiroRepository. */
public record TotalQuantidadeDTO(Long quantidade, BigDecimal total) {

    public long quantidadeOuZero() {
        return quantidade == null ? 0L : quantidade;
    }

    public BigDecimal totalOuZero() {
        return total == null ? BigDecimal.ZERO : total;
    }
}
