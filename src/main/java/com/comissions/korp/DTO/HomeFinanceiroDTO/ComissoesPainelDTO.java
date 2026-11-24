package com.comissions.korp.DTO.HomeFinanceiroDTO;

import java.util.List;

public record ComissoesPainelDTO(
        long parcelasPagas,
        long parcelasLiberadas,
        long parcelasPendentes,
        int pagina,
        int tamanho,
        long totalPagamentosPeriodo,
        int totalPaginas,
        List<ComissaoParcelaDTO> pagamentosPeriodo,
        List<ComissaoParcelaDTO> proximasLiberacoes
) {
}