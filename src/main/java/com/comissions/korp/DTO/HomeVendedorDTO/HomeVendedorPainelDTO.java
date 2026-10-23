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
 *   <li>linhas: página da tabela já filtrada por status e período. Sem card selecionado, cada
 *   venda vira uma linha VENDA (só quando foi realizada no período) seguida de uma linha
 *   PAGAMENTO por parcela de comissão; os cards mostram só vendas (VENDAS) ou só parcelas
 *   (LIBERADAS/PENDENTES). A paginação conta linhas, não vendas</li>
 * </ul>
 */
public record HomeVendedorPainelDTO(
        Integer ano,
        Integer mes,
        String nomeMes,
        ResumoDTO resumo,
        List<ParcelaDTO> parcelasDoPeriodo,
        Page<LinhaPainelDTO> linhas
) {

    public enum TipoLinha {
        VENDA,
        PAGAMENTO
    }

    /** Linha da tabela: a venda vai em todas as linhas (abre o modal); parcela só nas linhas PAGAMENTO. */
    public record LinhaPainelDTO(
            TipoLinha tipo,
            VendaResumoDTO venda,
            ParcelaDTO parcela
    ) {
        public static LinhaPainelDTO venda(VendaResumoDTO venda) {
            return new LinhaPainelDTO(TipoLinha.VENDA, venda, null);
        }

        public static LinhaPainelDTO pagamento(VendaResumoDTO venda, ParcelaDTO parcela) {
            return new LinhaPainelDTO(TipoLinha.PAGAMENTO, venda, parcela);
        }
    }

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