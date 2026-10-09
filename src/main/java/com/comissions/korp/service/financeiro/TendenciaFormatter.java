package com.comissions.korp.service.financeiro;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Formata a variação de um indicador em relação ao mês anterior. */
final class TendenciaFormatter {

    private TendenciaFormatter() {
    }

    /** Variação percentual com sinal, ex.: "+13,5%". */
    static String percentual(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.signum() == 0) {
            return atual.signum() > 0 ? "+100%" : "0%";
        }

        BigDecimal variacao = atual.subtract(anterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(anterior, 1, RoundingMode.HALF_UP);

        String texto = variacao.stripTrailingZeros().toPlainString().replace('.', ',');
        return variacao.signum() > 0 ? "+" + texto + "%" : texto + "%";
    }

    /** Diferença absoluta com sinal, ex.: "+8". */
    static String diferenca(long atual, long anterior) {
        long delta = atual - anterior;
        return delta > 0 ? "+" + delta : String.valueOf(delta);
    }
}
