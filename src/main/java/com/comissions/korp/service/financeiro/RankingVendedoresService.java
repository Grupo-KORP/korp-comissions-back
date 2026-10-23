package com.comissions.korp.service.financeiro;

import com.comissions.korp.DTO.HomeFinanceiroDTO.RankingVendedorDTO;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ENUM.StatusParcela;
import com.comissions.korp.repository.FinanceiroRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Ranking de comissão por vendedor no período. */
@Service
public class RankingVendedoresService {

    private static final int LIMITE_PADRAO = 5;
    private static final int LIMITE_MAXIMO = 50;
    private static final List<StatusComissao> COMISSOES_VALIDAS =
            List.of(StatusComissao.PENDENTE, StatusComissao.LIBERADA, StatusComissao.PAGA);

    private final FinanceiroRepository financeiroRepository;

    public RankingVendedoresService(FinanceiroRepository financeiroRepository) {
        this.financeiroRepository = financeiroRepository;
    }

    @Transactional(readOnly = true)
    public List<RankingVendedorDTO> buscarRanking(PeriodoFinanceiro periodo, Integer limite) {
        int tamanho = limite == null ? LIMITE_PADRAO : Math.min(Math.max(limite, 1), LIMITE_MAXIMO);

        return financeiroRepository.rankearVendedoresPorComissao(
                COMISSOES_VALIDAS,
                periodo.inicio(),
                periodo.fim(),
                StatusParcela.CANCELADO,
                PageRequest.of(0, tamanho)
        );
    }
}
