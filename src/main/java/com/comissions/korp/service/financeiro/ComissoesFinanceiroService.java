package com.comissions.korp.service.financeiro;

import com.comissions.korp.DTO.HomeFinanceiroDTO.ComissaoParcelaDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.ComissoesPainelDTO;
import com.comissions.korp.entity.Comissao;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ENUM.StatusParcela;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.FinanceiroRepository;
import com.comissions.korp.repository.ItemPedidoRepository;
import com.comissions.korp.service.ComissaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ComissoesFinanceiroService {

    private static final int LIMITE_PROXIMAS_LIBERACOES = 5;
        private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final FinanceiroRepository financeiroRepository;
    private final ItemPedidoRepository itemPedidoRepository;
        private final ComissaoRepository comissaoRepository;

    public ComissoesFinanceiroService(
            FinanceiroRepository financeiroRepository,
                        ItemPedidoRepository itemPedidoRepository,
                        ComissaoRepository comissaoRepository
    ) {
        this.financeiroRepository = financeiroRepository;
        this.itemPedidoRepository = itemPedidoRepository;
                this.comissaoRepository = comissaoRepository;
    }

        @Transactional
        public StatusComissao atualizarStatus(Integer idComissao, StatusComissao novoStatus) {
                Comissao comissao = comissaoRepository.findById(idComissao)
                                .orElseThrow(() -> new RecursoNaoEncontrado("Comissão não encontrada com ID: " + idComissao));
                comissao.setStatusComissao(novoStatus);
                return comissaoRepository.save(comissao).getStatusComissao();
        }

    @Transactional(readOnly = true)
    public ComissoesPainelDTO buscarPainel(
            PeriodoFinanceiro periodo,
            String busca,
            StatusComissao status,
            int pagina,
            int tamanho
    ) {
        List<Comissao> comissoes = financeiroRepository.buscarComissoesDoPeriodo(
                periodo.inicio(), periodo.fim(), StatusComissao.CANCELADA, StatusParcela.CANCELADO);
        List<Integer> idsPedido = comissoes.stream()
                .map(comissao -> comissao.getPedido().getIdPedido())
                .distinct()
                .toList();
        Map<Integer, String> produtosPorPedido = idsPedido.isEmpty()
                ? Map.of()
                : itemPedidoRepository.buscarItensDosPedidos(idsPedido).stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getPedido().getIdPedido(),
                                Collectors.mapping(item -> item.getProduto().getNome(), Collectors.joining(", "))
                        ));
        List<ComissaoParcelaDTO> todas = comissoes.stream()
                .map(comissao -> paraDTO(comissao, produtosPorPedido))
                .toList();

        List<ComissaoParcelaDTO> filtradas = todas.stream()
                .filter(linha -> status == null || linha.status() == status)
                .filter(linha -> correspondeBusca(linha, busca))
                .toList();
        int paginaResolvida = Math.max(pagina, 0);
        int tamanhoResolvido = Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO_PAGINA);
        long inicio = (long) paginaResolvida * tamanhoResolvido;
        List<ComissaoParcelaDTO> pagamentosPagina = inicio >= filtradas.size()
                ? List.of()
                : filtradas.subList((int) inicio, (int) Math.min(inicio + tamanhoResolvido, filtradas.size()));
        int totalPaginas = (int) Math.ceil((double) filtradas.size() / tamanhoResolvido);

        return new ComissoesPainelDTO(
                contar(todas, StatusComissao.PAGA),
                contar(todas, StatusComissao.LIBERADA),
                contar(todas, StatusComissao.PENDENTE),
                paginaResolvida,
                tamanhoResolvido,
                filtradas.size(),
                totalPaginas,
                pagamentosPagina,
                todas.stream().limit(LIMITE_PROXIMAS_LIBERACOES).toList()
        );
    }

    private long contar(List<ComissaoParcelaDTO> linhas, StatusComissao status) {
        return linhas.stream().filter(linha -> linha.status() == status).count();
    }

    private boolean correspondeBusca(ComissaoParcelaDTO linha, String busca) {
        if (busca == null || busca.isBlank()) {
            return true;
        }

        String termo = busca.trim().toLowerCase(Locale.ROOT);
        String numeroPedido = termo.replaceFirst("^vd?-?", "");
        if (numeroPedido.matches("\\d+") && linha.idPedido().toString().equals(numeroPedido)) {
            return true;
        }

        return contem(linha.vendedor(), termo)
                || contem(linha.cliente(), termo)
                || contem(linha.codigoPedido(), termo)
                || contem(linha.produto(), termo);
    }

    private boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termo);
    }

    private ComissaoParcelaDTO paraDTO(Comissao comissao, Map<Integer, String> produtosPorPedido) {
        var pedido = comissao.getPedido();
        var parcela = comissao.getParcela();
        var cliente = pedido.getCliente();
        String nomeCliente = cliente.getNomeFantasia() == null || cliente.getNomeFantasia().isBlank()
                ? cliente.getRazaoSocial()
                : cliente.getNomeFantasia();

        return new ComissaoParcelaDTO(
                comissao.getId(),
                pedido.getIdPedido(),
                "VD-" + pedido.getIdPedido(),
                produtosPorPedido.get(pedido.getIdPedido()),
                nomeCliente,
                comissao.getUsuario().getNome(),
                comissao.getValorComissao(),
                parcela.getNumeroParcela(),
                parcela.getPagamento().getQuantidadeParcelas(),
                parcela.getDataVencimento(),
                comissao.getStatusComissao()
        );
    }
}