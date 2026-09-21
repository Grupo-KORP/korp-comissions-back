package com.comissions.korp.DTO.HomeVendedorDTO;

import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.ParcelaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.VendaResumoDTO;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resposta de GET /vendedor/home.
 * <ul>
 *   <li>resumo: cards e projeção do período (independem da página e do filtro de status)</li>
 *   <li>parcelasLiberadas: lista da coluna direita (independe da página)</li>
 *   <li>vendas: página de vendas já filtrada por status e período</li>
 * </ul>
 */
public record HomeVendedorPainelDTO(
        Integer ano,
        Integer mes,
        String nomeMes,
        ResumoDTO resumo,
        List<ParcelaDTO> parcelasLiberadas,
        Page<VendaResumoDTO> vendas
) {

    public record ResumoDTO(
            Integer totalVendas,
            Integer comissoesLiberadas,
            Integer pagamentosPendentes,
            BigDecimal projecao,
            Integer parcelas,
            String tendencia
    ) {
    }
}