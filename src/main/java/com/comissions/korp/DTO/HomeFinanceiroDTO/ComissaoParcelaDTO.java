package com.comissions.korp.DTO.HomeFinanceiroDTO;

import com.comissions.korp.entity.ENUM.StatusComissao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ComissaoParcelaDTO(
        Integer idComissao,
        Integer idPedido,
        String codigoPedido,
        String produto,
        String cliente,
        String vendedor,
        BigDecimal valorComissao,
        Integer numeroParcela,
        Integer totalParcelas,
        LocalDate dataVencimento,
        StatusComissao status
) {
}