package com.comissions.korp.DTO.HomeVendedorDTO;

import com.comissions.korp.entity.ENUM.StatusComissao;

import java.util.List;

/**
 * Filtro de status do painel do vendedor (query param "status").
 * <p>
 * Cada valor define quais status de comissão fazem uma venda entrar na listagem,
 * se pedidos EM_ANDAMENTO (ainda sem comissão gerada) também devem aparecer e
 * quais linhas a tabela mostra para cada venda (ver {@link LinhasTabela}).
 * Comissões CANCELADA nunca entram; parcelas CANCELADO também são excluídas nas queries.
 */
public enum FiltroStatusVenda {

    /** Sem card selecionado: vendas do período e as parcelas de comissão do período. */
    TODAS(
            List.of(StatusComissao.PENDENTE, StatusComissao.LIBERADA, StatusComissao.PAGA),
            true,
            true,
            LinhasTabela.VENDAS_E_PARCELAS
    ),

    /** Card "Total de Vendas": as mesmas vendas contadas no card, sem as parcelas. */
    VENDAS(
            List.of(StatusComissao.PENDENTE, StatusComissao.LIBERADA, StatusComissao.PAGA),
            true,
            true,
            LinhasTabela.SOMENTE_VENDAS
    ),

    /** Card "Comissões Liberadas": só as parcelas liberadas e pagas. */
    LIBERADAS(
            List.of(StatusComissao.LIBERADA, StatusComissao.PAGA),
            false,
            false,
            LinhasTabela.SOMENTE_PARCELAS
    ),

    /** Card "Pagamentos Pendentes": só as parcelas pendentes. */
    PENDENTES(
            List.of(StatusComissao.PENDENTE),
            false,
            false,
            LinhasTabela.SOMENTE_PARCELAS
    );

    public enum LinhasTabela {
        /** Linha da venda (só quando feita no período) seguida das parcelas dela. */
        VENDAS_E_PARCELAS,
        /** Uma linha por venda listada, sem parcelas. */
        SOMENTE_VENDAS,
        /** Só as parcelas de comissão, sem a linha da venda. */
        SOMENTE_PARCELAS
    }

    private final List<StatusComissao> statusComissoes;
    private final boolean incluiEmAndamento;
    private final boolean incluiVendaDoPeriodo;
    private final LinhasTabela linhasTabela;

    FiltroStatusVenda(
            List<StatusComissao> statusComissoes,
            boolean incluiEmAndamento,
            boolean incluiVendaDoPeriodo,
            LinhasTabela linhasTabela
    ) {
        this.statusComissoes = statusComissoes;
        this.incluiEmAndamento = incluiEmAndamento;
        this.incluiVendaDoPeriodo = incluiVendaDoPeriodo;
        this.linhasTabela = linhasTabela;
    }

    public LinhasTabela getLinhasTabela() {
        return linhasTabela;
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