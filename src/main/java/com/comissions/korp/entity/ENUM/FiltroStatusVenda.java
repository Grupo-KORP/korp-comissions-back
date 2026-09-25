package com.comissions.korp.DTO.HomeVendedorDTO;

import com.comissions.korp.entity.ENUM.StatusComissao;

import java.util.List;

/**
 * Filtro de status do painel do vendedor (query param "status").
 * <p>
 * Cada valor define quais status de comissão fazem uma venda entrar na listagem
 * e se pedidos EM_ANDAMENTO (ainda sem comissão gerada) também devem aparecer.
 * Comissões CANCELADA nunca entram; parcelas CANCELADO também são excluídas nas queries.
 */
public enum FiltroStatusVenda {

    TODAS(
            List.of(StatusComissao.PENDENTE, StatusComissao.LIBERADA, StatusComissao.PAGA),
            true,
            true
    ),

    LIBERADAS(
            List.of(StatusComissao.LIBERADA, StatusComissao.PAGA),
            false,
            false
    ),

    PENDENTES(
            List.of(StatusComissao.PENDENTE),
            true,
            false
    );

    private final List<StatusComissao> statusComissoes;
    private final boolean incluiEmAndamento;
    private final boolean incluiVendaDoPeriodo;

    FiltroStatusVenda(
            List<StatusComissao> statusComissoes,
            boolean incluiEmAndamento,
            boolean incluiVendaDoPeriodo
    ) {
        this.statusComissoes = statusComissoes;
        this.incluiEmAndamento = incluiEmAndamento;
        this.incluiVendaDoPeriodo = incluiVendaDoPeriodo;
    }

    public List<StatusComissao> getStatusComissoes() {
        return statusComissoes;
    }

    public boolean isIncluiEmAndamento() {
        return incluiEmAndamento;
    }

    public boolean isIncluiVendaDoPeriodo() {
        return incluiVendaDoPeriodo;
    }
}