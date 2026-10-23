package com.comissions.korp.service.financeiro;

import com.comissions.korp.DTO.HomeFinanceiroDTO.AgregadoMensalDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.ResumoFinanceiroDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.TotalQuantidadeDTO;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ENUM.StatusParcela;
import com.comissions.korp.repository.FinanceiroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/** Cards do painel financeiro. */
@Service
public class ResumoFinanceiroService {

    private static final List<StatusParcela> PARCELAS_PENDENTES = List.of(StatusParcela.PENDENTE, StatusParcela.ATRASADO);
    private static final List<StatusComissao> COMISSOES_PAGAS = List.of(StatusComissao.PAGA);
    private static final List<StatusComissao> COMISSOES_A_PAGAR = List.of(StatusComissao.PENDENTE, StatusComissao.LIBERADA);

    private final FinanceiroRepository financeiroRepository;

    public ResumoFinanceiroService(FinanceiroRepository financeiroRepository) {
        this.financeiroRepository = financeiroRepository;
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroDTO buscarResumo(PeriodoFinanceiro periodo) {
        PeriodoFinanceiro anterior = periodo.mesesAtras(1);

        // Uma query traz o mês selecionado e o anterior (base da tendência)
        List<AgregadoMensalDTO> vendas = financeiroRepository.totalizarVendasPorMes(anterior.inicio(), periodo.fim());
        AgregadoMensalDTO vendasAtual = vendasDoMes(vendas, periodo.referencia());
        AgregadoMensalDTO vendasAnterior = vendasDoMes(vendas, anterior.referencia());

        TotalQuantidadeDTO pendentes = financeiroRepository.totalizarParcelas(
                PARCELAS_PENDENTES, periodo.inicio(), periodo.fim());
        TotalQuantidadeDTO pagas = financeiroRepository.totalizarComissoes(
                COMISSOES_PAGAS, periodo.inicio(), periodo.fim(), StatusParcela.CANCELADO);
        TotalQuantidadeDTO aPagar = financeiroRepository.totalizarComissoes(
                COMISSOES_A_PAGAR, periodo.inicio(), periodo.fim(), StatusParcela.CANCELADO);

        return new ResumoFinanceiroDTO(
                vendasAtual.total(),
                TendenciaFormatter.percentual(vendasAtual.total(), vendasAnterior.total()),
                vendasAtual.quantidade(),
                TendenciaFormatter.diferenca(vendasAtual.quantidade(), vendasAnterior.quantidade()),
                pendentes.totalOuZero(),
                pendentes.quantidadeOuZero(),
                pagas.totalOuZero(),
                pagas.quantidadeOuZero(),
                aPagar.totalOuZero(),
                aPagar.quantidadeOuZero()
        );
    }

    private AgregadoMensalDTO vendasDoMes(List<AgregadoMensalDTO> vendas, YearMonth mes) {
        return vendas.stream()
                .filter(v -> v.ano() == mes.getYear() && v.mes() == mes.getMonthValue())
                .findFirst()
                .map(v -> new AgregadoMensalDTO(
                        v.ano(),
                        v.mes(),
                        v.quantidade() == null ? 0L : v.quantidade(),
                        v.total() == null ? BigDecimal.ZERO : v.total()))
                .orElseGet(() -> new AgregadoMensalDTO(mes.getYear(), mes.getMonthValue(), 0L, BigDecimal.ZERO));
    }
}
