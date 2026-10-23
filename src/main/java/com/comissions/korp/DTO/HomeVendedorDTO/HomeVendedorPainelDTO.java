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
 *   <li>parcelasDoPeriodo: parcelas pendentes, liberadas e pagas da coluna direita (independe da página)</li>
 *   <li>vendas: página de vendas já filtrada por status e período</li>
 * </ul>
 */
public record HomeVendedorPainelDTO(
        Integer ano,
        Integer mes,
        String nomeMes,
        ResumoDTO resumo,
        List<ParcelaDTO> parcelasDoPeriodo,
        Page<VendaResumoDTO> vendas
) {

    /**
     * Tendências comparam o período com o mesmo período do mês anterior
     * ("+25%", "-10%", "0%" ou "Novo" quando o mês anterior não tem base).
     */
    public record ResumoDTO(
            Integer totalVendas,
            Integer comissoesLiberadas,
            Integer pagamentosPendentes,
            BigDecimal projecao,
            Integer parcelasPagas,
            Integer totalParcelas,
            String tendencia,
            String tendenciaVendas,
            String tendenciaLiberadas
    ) {
    }
}