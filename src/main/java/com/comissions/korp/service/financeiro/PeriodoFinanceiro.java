package com.comissions.korp.service.financeiro;

import java.time.LocalDate;
import java.time.YearMonth;

/** Mês de referência dos filtros do painel financeiro (ano/mes; sem valores, usa o mês atual). */
public record PeriodoFinanceiro(YearMonth referencia) {

    private static final String[] MESES_ABREVIADOS = {
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    };

    public static PeriodoFinanceiro de(Integer ano, Integer mes) {
        LocalDate hoje = LocalDate.now();
        int anoResolvido = ano == null ? hoje.getYear() : ano;
        int mesResolvido = mes == null ? hoje.getMonthValue() : mes;

        if (mesResolvido < 1 || mesResolvido > 12) {
            throw new IllegalArgumentException("Mês inválido: informe um valor entre 1 e 12.");
        }
        return new PeriodoFinanceiro(YearMonth.of(anoResolvido, mesResolvido));
    }

    public LocalDate inicio() {
        return referencia.atDay(1);
    }

    public LocalDate fim() {
        return referencia.atEndOfMonth();
    }

    public PeriodoFinanceiro mesesAtras(int quantidade) {
        return new PeriodoFinanceiro(referencia.minusMonths(quantidade));
    }

    /** Formato usado no eixo do gráfico, ex.: "Out/25". */
    public static String rotuloAbreviado(YearMonth mes) {
        return MESES_ABREVIADOS[mes.getMonthValue() - 1] + "/" + String.format("%02d", mes.getYear() % 100);
    }
}
