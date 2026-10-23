package com.comissions.korp.service.financeiro;

import com.comissions.korp.DTO.HomeFinanceiroDTO.AgregadoMensalDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.EvolucaoVendasMesDTO;
import com.comissions.korp.repository.FinanceiroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Gráfico de evolução de vendas: os últimos meses terminando no mês selecionado. */
@Service
public class EvolucaoVendasService {

    private static final int QUANTIDADE_MESES = 6;

    private final FinanceiroRepository financeiroRepository;

    public EvolucaoVendasService(FinanceiroRepository financeiroRepository) {
        this.financeiroRepository = financeiroRepository;
    }

    @Transactional(readOnly = true)
    public List<EvolucaoVendasMesDTO> buscarEvolucao(PeriodoFinanceiro periodo) {
        YearMonth primeiroMes = periodo.mesesAtras(QUANTIDADE_MESES - 1).referencia();

        Map<YearMonth, AgregadoMensalDTO> vendasPorMes = financeiroRepository
                .totalizarVendasPorMes(primeiroMes.atDay(1), periodo.fim())
                .stream()
                .collect(Collectors.toMap(v -> YearMonth.of(v.ano(), v.mes()), Function.identity()));

        // Meses sem vendas entram zerados para o gráfico não pular datas
        return IntStream.range(0, QUANTIDADE_MESES)
                .mapToObj(primeiroMes::plusMonths)
                .map(mes -> criarPonto(mes, vendasPorMes.get(mes)))
                .toList();
    }

    private EvolucaoVendasMesDTO criarPonto(YearMonth mes, AgregadoMensalDTO vendas) {
        return new EvolucaoVendasMesDTO(
                PeriodoFinanceiro.rotuloAbreviado(mes),
                mes.getYear(),
                mes.getMonthValue(),
                vendas == null || vendas.total() == null ? BigDecimal.ZERO : vendas.total(),
                vendas == null || vendas.quantidade() == null ? 0L : vendas.quantidade()
        );
    }
}
