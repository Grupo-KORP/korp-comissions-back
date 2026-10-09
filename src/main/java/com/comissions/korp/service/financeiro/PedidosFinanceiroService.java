package com.comissions.korp.service.financeiro;

import com.comissions.korp.DTO.HomeFinanceiroDTO.ComissaoPedidoDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.PedidoRecenteDTO;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ENUM.StatusParcela;
import com.comissions.korp.entity.Pagamento;
import com.comissions.korp.entity.Parcela;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.repository.FinanceiroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Tabela paginada de pedidos do período. */
@Service
public class PedidosFinanceiroService {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;
    /** Ordenação fixa: o cliente escolhe só a página e o tamanho. */
    private static final Sort ORDENACAO = Sort.by(Sort.Direction.DESC, "dataPedido", "idPedido");

    private static final String STATUS_EM_ANALISE = "Em Análise";
    private static final String STATUS_PAGO = "Pago";
    private static final String STATUS_PENDENTE = "Pendente";

    private final FinanceiroRepository financeiroRepository;

    public PedidosFinanceiroService(FinanceiroRepository financeiroRepository) {
        this.financeiroRepository = financeiroRepository;
    }

    @Transactional(readOnly = true)
    public Page<PedidoRecenteDTO> buscarPedidos(PeriodoFinanceiro periodo, Pageable pageable) {
        Pageable paginacao = PageRequest.of(
                Math.max(pageable.getPageNumber(), 0),
                Math.min(Math.max(pageable.getPageSize(), 1), TAMANHO_MAXIMO_PAGINA),
                ORDENACAO
        );

        Page<Pedido> pagina = financeiroRepository.buscarPedidosDoPeriodo(periodo.inicio(), periodo.fim(), paginacao);
        List<Integer> ids = pagina.getContent().stream().map(Pedido::getIdPedido).toList();

        Map<Integer, List<Parcela>> parcelasPorPedido = ids.isEmpty()
                ? Map.of()
                : financeiroRepository.buscarParcelasDosPedidos(ids).stream()
                .collect(Collectors.groupingBy(parcela -> parcela.getPagamento().getPedido().getIdPedido()));

        Map<Integer, BigDecimal> comissaoPorPedido = ids.isEmpty()
                ? Map.of()
                : financeiroRepository.somarComissoesPorPedido(ids, StatusComissao.CANCELADA).stream()
                .collect(Collectors.toMap(ComissaoPedidoDTO::idPedido, ComissaoPedidoDTO::total));

        return pagina.map(pedido -> criarPedido(
                pedido,
                parcelasPorPedido.getOrDefault(pedido.getIdPedido(), List.of()),
                comissaoPorPedido.getOrDefault(pedido.getIdPedido(), BigDecimal.ZERO)
        ));
    }

    private PedidoRecenteDTO criarPedido(Pedido pedido, List<Parcela> parcelas, BigDecimal comissao) {
        return new PedidoRecenteDTO(
                pedido.getIdPedido(),
                "V" + pedido.getIdPedido(),
                pedido.getDataPedido(),
                pedido.getUsuario().getNome(),
                nomeCliente(pedido.getCliente()),
                pedido.getValorTotalFaturamento(),
                comissao,
                descreverPagamento(parcelas),
                descreverStatus(pedido, parcelas)
        );
    }

    private String nomeCliente(Cliente cliente) {
        String fantasia = cliente.getNomeFantasia();
        return fantasia != null && !fantasia.isBlank() ? fantasia : cliente.getRazaoSocial();
    }

    private String descreverPagamento(List<Parcela> parcelas) {
        if (parcelas.isEmpty()) {
            return "-";
        }
        Pagamento pagamento = parcelas.get(0).getPagamento();
        Integer total = pagamento.getQuantidadeParcelas();
        return total == null || total <= 1 ? "À vista" : "Parcelado " + total + "x";
    }

    /** EM_ANDAMENTO ainda não gerou pagamento; depois disso vale o status das parcelas não canceladas. */
    private String descreverStatus(Pedido pedido, List<Parcela> parcelas) {
        if ("EM_ANDAMENTO".equalsIgnoreCase(pedido.getStatusPedido())) {
            return STATUS_EM_ANALISE;
        }
        List<Parcela> ativas = parcelas.stream()
                .filter(parcela -> parcela.getStatusParcela() != StatusParcela.CANCELADO)
                .toList();
        boolean quitado = !ativas.isEmpty()
                && ativas.stream().allMatch(parcela -> parcela.getStatusParcela() == StatusParcela.PAGO);
        return quitado ? STATUS_PAGO : STATUS_PENDENTE;
    }
}
