package com.comissions.korp.service;

import com.comissions.korp.DTO.HomeVendedorDTO.FiltroStatusVenda;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO.LinhaPainelDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO.TipoLinha;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.ParcelaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.ResumoStatusDTO;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Comissao;
import com.comissions.korp.entity.ENUM.MetodoPagamento;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ENUM.StatusParcela;
import com.comissions.korp.entity.Pagamento;
import com.comissions.korp.entity.Parcela;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.repository.ContatoRepository;
import com.comissions.korp.repository.EnderecoRepository;
import com.comissions.korp.repository.ItemPedidoRepository;
import com.comissions.korp.repository.PagamentoRepository;
import com.comissions.korp.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeVendedorServiceTest {

    private static final Integer VENDEDOR = 99;
    private static final Pageable PRIMEIRA_PAGINA = PageRequest.of(0, 5);

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ItemPedidoRepository itemPedidoRepository;
    @Mock
    private ComissaoRepository comissaoRepository;
    @Mock
    private EnderecoRepository enderecoRepository;
    @Mock
    private ContatoRepository contatoRepository;
    @Mock
    private PagamentoRepository pagamentoRepository;

    private HomeVendedorService homeVendedorService;

    @BeforeEach
    void setUp() {
        homeVendedorService = new HomeVendedorService(
                pedidoRepository,
                itemPedidoRepository,
                comissaoRepository,
                enderecoRepository,
                contatoRepository,
                pagamentoRepository
        );
    }

    @Test
    void buscarPainel_deveFalhar_quandoMesEhInvalido() {
        assertThatThrownBy(() -> homeVendedorService.buscarPainel(
                VENDEDOR, 2026, 13, null, FiltroStatusVenda.TODAS, PRIMEIRA_PAGINA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("12");

        verifyNoInteractions(pedidoRepository, comissaoRepository);
    }

    @Test
    void buscarPainel_deveRetornarResumoZerado_quandoNaoHaVendasNemComissoes() {
        prepararPaginaVazia();
        when(pedidoRepository.contarVendasDoPeriodo(eq(VENDEDOR), any(), any()))
                .thenReturn(0L);
        when(comissaoRepository.resumirPorStatus(eq(VENDEDOR), any(), any(), eq(StatusParcela.CANCELADO)))
                .thenReturn(List.of());
        when(comissaoRepository.buscarComissoesPorStatusNoPeriodo(eq(VENDEDOR), anyList(), any(), any(), eq(StatusParcela.CANCELADO)))
                .thenReturn(List.of());

        HomeVendedorPainelDTO painel = homeVendedorService.buscarPainel(
                VENDEDOR, 2026, 6, null, FiltroStatusVenda.TODAS, PRIMEIRA_PAGINA);

        HomeVendedorPainelDTO.ResumoDTO resumo = painel.resumo();
        assertThat(painel.nomeMes()).isEqualTo("Junho");
        assertThat(resumo.totalVendas()).isZero();
        assertThat(resumo.comissoesLiberadas()).isZero();
        assertThat(resumo.pagamentosPendentes()).isZero();
        assertThat(resumo.parcelasPagas()).isZero();
        assertThat(resumo.totalParcelas()).isZero();
        assertThat(resumo.projecao()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.tendencia()).isEqualTo("0%");
        assertThat(resumo.tendenciaVendas()).isEqualTo("0%");
        assertThat(resumo.tendenciaLiberadas()).isEqualTo("0%");
        assertThat(painel.parcelasDoPeriodo()).isEmpty();
        assertThat(painel.linhas()).isEmpty();
    }

    @Test
    void buscarPainel_devePaginarPorLinha_contandoVendaEParcelas() {
        prepararDuasVendasComParcelas();

        Page<LinhaPainelDTO> primeira = homeVendedorService.buscarPainel(
                VENDEDOR, 2026, 10, null, FiltroStatusVenda.TODAS, PRIMEIRA_PAGINA).linhas();

        // 1 linha de venda (só a feita no período) + 5 parcelas no período = 6 linhas
        assertThat(primeira.getTotalElements()).isEqualTo(6);
        assertThat(primeira.getTotalPages()).isEqualTo(2);
        assertThat(primeira.getContent())
                .extracting(linha -> linha.tipo(), linha -> linha.venda().getIdPedido(),
                        linha -> linha.parcela() == null ? null : linha.parcela().getNumeroParcela())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(TipoLinha.VENDA, 1, null),
                        org.assertj.core.groups.Tuple.tuple(TipoLinha.PAGAMENTO, 1, 1),
                        org.assertj.core.groups.Tuple.tuple(TipoLinha.PAGAMENTO, 1, 2),
                        org.assertj.core.groups.Tuple.tuple(TipoLinha.PAGAMENTO, 1, 3),
                        org.assertj.core.groups.Tuple.tuple(TipoLinha.PAGAMENTO, 2, 1)
                );
        // A venda leva todas as parcelas (modal), não só as do período
        assertThat(primeira.getContent().get(4).venda().getParcelasDaVenda()).hasSize(3);

        Page<LinhaPainelDTO> segunda = homeVendedorService.buscarPainel(
                VENDEDOR, 2026, 10, null, FiltroStatusVenda.TODAS, PageRequest.of(1, 5)).linhas();

        assertThat(segunda.getContent())
                .extracting(linha -> linha.tipo(), linha -> linha.venda().getIdPedido(), linha -> linha.parcela().getNumeroParcela())
                .containsExactly(org.assertj.core.groups.Tuple.tuple(TipoLinha.PAGAMENTO, 2, 2));
    }

    @Test
    void buscarPainel_filtroVendas_deveListarSoVendasFeitasNoPeriodo_paginando() {
        Pedido venda1 = pedido(1, LocalDate.of(2026, 10, 10));
        Pedido venda3 = pedido(3, LocalDate.of(2026, 10, 12));
        when(pedidoRepository.listarVendasDoPeriodo(
                eq(VENDEDOR), eq(LocalDate.of(2026, 10, 1)), eq(LocalDate.of(2026, 10, 31)), any(Sort.class)))
                .thenReturn(List.of(venda1, venda3));
        when(pedidoRepository.contarVendasDoPeriodo(VENDEDOR, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(2L);

        HomeVendedorPainelDTO painel = homeVendedorService.buscarPainel(
                VENDEDOR, 2026, 10, null, FiltroStatusVenda.VENDAS, PageRequest.of(0, 1));

        assertThat(painel.resumo().totalVendas()).isEqualTo(2);
        assertThat(painel.linhas().getTotalElements()).isEqualTo(2);
        assertThat(painel.linhas().getTotalPages()).isEqualTo(2);
        assertThat(painel.linhas().getContent())
                .extracting(LinhaPainelDTO::tipo, linha -> linha.venda().getIdPedido(), LinhaPainelDTO::parcela)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(TipoLinha.VENDA, 1, null));
        // Pedidos de outros meses que só têm parcela no período não entram na lista de vendas
        verify(pedidoRepository, never()).listarPedidosDoPainel(
                any(), any(), any(), anyList(), any(), anyBoolean(), anyBoolean(), any(Sort.class));
    }

    @Test
    void buscarPainel_vendaDeOutroMesComParcelaNoPeriodo_apareceComoParcelaMasNaoContaComoVenda() {
        Pedido vendaDeOutubro = pedido(1, LocalDate.of(2026, 10, 10));
        when(pedidoRepository.listarPedidosDoPainel(
                eq(VENDEDOR), any(), any(), anyList(), any(), anyBoolean(), anyBoolean(), any(Sort.class)))
                .thenReturn(List.of(vendaDeOutubro));
        when(comissaoRepository.buscarComissoesDosPedidos(List.of(1), VENDEDOR))
                .thenReturn(List.of(
                        comissao(vendaDeOutubro, 2, 2, "100.00", StatusComissao.PENDENTE, LocalDate.of(2026, 11, 10))
                ));

        HomeVendedorPainelDTO painel = homeVendedorService.buscarPainel(
                VENDEDOR, 2026, 11, null, FiltroStatusVenda.PENDENTES, PRIMEIRA_PAGINA);

        assertThat(painel.resumo().totalVendas()).isZero();
        assertThat(painel.linhas().getContent())
                .extracting(LinhaPainelDTO::tipo, linha -> linha.venda().getIdPedido(), linha -> linha.parcela().getNumeroParcela())
                .containsExactly(org.assertj.core.groups.Tuple.tuple(TipoLinha.PAGAMENTO, 1, 2));
        verify(pedidoRepository).contarVendasDoPeriodo(VENDEDOR, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
    }

    @Test
    void buscarPainel_filtroLiberadas_deveListarSoParcelasLiberadasEPagas() {
        prepararDuasVendasComParcelas();

        Page<LinhaPainelDTO> linhas = homeVendedorService.buscarPainel(
                VENDEDOR, 2026, 10, null, FiltroStatusVenda.LIBERADAS, PRIMEIRA_PAGINA).linhas();

        assertThat(linhas.getTotalElements()).isEqualTo(2);
        assertThat(linhas.getContent())
                .extracting(LinhaPainelDTO::tipo, linha -> linha.venda().getIdPedido(), linha -> linha.parcela().getStatus())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(TipoLinha.PAGAMENTO, 2, "PAGA"),
                        org.assertj.core.groups.Tuple.tuple(TipoLinha.PAGAMENTO, 2, "LIBERADA")
                );
    }

    @Test
    void buscarPainel_filtroPendentes_deveListarSoParcelasPendentes_paginando() {
        prepararDuasVendasComParcelas();

        Page<LinhaPainelDTO> segunda = homeVendedorService.buscarPainel(
                VENDEDOR, 2026, 10, null, FiltroStatusVenda.PENDENTES, PageRequest.of(1, 2)).linhas();

        // 3 parcelas pendentes no período (a de novembro fica de fora), 2 por página
        assertThat(segunda.getTotalElements()).isEqualTo(3);
        assertThat(segunda.getTotalPages()).isEqualTo(2);
        assertThat(segunda.getContent())
                .extracting(LinhaPainelDTO::tipo, linha -> linha.parcela().getNumeroParcela(), linha -> linha.parcela().getStatus())
                .containsExactly(org.assertj.core.groups.Tuple.tuple(TipoLinha.PAGAMENTO, 3, "PENDENTE"));
    }

    /** Venda 1 (feita no período, 3 parcelas pendentes) e venda 2 (mês anterior, 1 paga e 1 liberada no período). */
    private void prepararDuasVendasComParcelas() {
        Pedido vendaDoPeriodo = pedido(1, LocalDate.of(2026, 10, 10));
        Pedido vendaDoMesAnterior = pedido(2, LocalDate.of(2026, 9, 5));
        when(pedidoRepository.listarPedidosDoPainel(
                eq(VENDEDOR), any(), any(), anyList(), any(), anyBoolean(), anyBoolean(), any(Sort.class)))
                .thenReturn(List.of(vendaDoPeriodo, vendaDoMesAnterior));
        when(comissaoRepository.buscarComissoesDosPedidos(List.of(1, 2), VENDEDOR))
                .thenReturn(List.of(
                        comissao(vendaDoPeriodo, 1, 3, "100.00", StatusComissao.PENDENTE, LocalDate.of(2026, 10, 10)),
                        comissao(vendaDoPeriodo, 2, 3, "100.00", StatusComissao.PENDENTE, LocalDate.of(2026, 10, 20)),
                        comissao(vendaDoPeriodo, 3, 3, "100.00", StatusComissao.PENDENTE, LocalDate.of(2026, 10, 30)),
                        comissao(vendaDoMesAnterior, 2, 3, "50.00", StatusComissao.LIBERADA, LocalDate.of(2026, 10, 25)),
                        comissao(vendaDoMesAnterior, 1, 3, "50.00", StatusComissao.PAGA, LocalDate.of(2026, 10, 5)),
                        // vence fora do período: não vira linha
                        comissao(vendaDoMesAnterior, 3, 3, "50.00", StatusComissao.PENDENTE, LocalDate.of(2026, 11, 5))
                ));
    }

    @Test
    void buscarPainel_deveSomarTodasAsParcelasECompararCadaCardComOMesAnterior() {
        prepararPaginaVazia();
        when(pedidoRepository.contarVendasDoPeriodo(VENDEDOR, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(4L);
        when(pedidoRepository.contarVendasDoPeriodo(VENDEDOR, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(2L);
        when(comissaoRepository.resumirPorStatus(
                VENDEDOR, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), StatusParcela.CANCELADO))
                .thenReturn(List.of(
                        new ResumoStatusDTO(StatusComissao.PENDENTE, 1L, new BigDecimal("2000.00")),
                        new ResumoStatusDTO(StatusComissao.LIBERADA, 1L, new BigDecimal("150.00")),
                        new ResumoStatusDTO(StatusComissao.PAGA, 2L, new BigDecimal("350.00"))
                ));
        when(comissaoRepository.resumirPorStatus(
                VENDEDOR, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), StatusParcela.CANCELADO))
                .thenReturn(List.of(
                        new ResumoStatusDTO(StatusComissao.PAGA, 2L, new BigDecimal("2000.00"))
                ));
        when(comissaoRepository.buscarComissoesPorStatusNoPeriodo(
                eq(VENDEDOR), eq(FiltroStatusVenda.TODAS.getStatusComissoes()), any(), any(), eq(StatusParcela.CANCELADO)))
                .thenReturn(List.of(
                        comissao(5, "Amazon", 1, 4, "2000.00", StatusComissao.PENDENTE),
                        comissao(9, "", 1, 1, "300.00", StatusComissao.PAGA)
                ));

        HomeVendedorPainelDTO painel = homeVendedorService.buscarPainel(
                VENDEDOR, 2026, 10, null, FiltroStatusVenda.TODAS, PRIMEIRA_PAGINA);

        HomeVendedorPainelDTO.ResumoDTO resumo = painel.resumo();
        assertThat(resumo.totalVendas()).isEqualTo(4);
        assertThat(resumo.comissoesLiberadas()).isEqualTo(3);
        assertThat(resumo.pagamentosPendentes()).isEqualTo(1);
        assertThat(resumo.parcelasPagas()).isEqualTo(2);
        assertThat(resumo.totalParcelas()).isEqualTo(4);
        // Comissão bruta inclui pendentes, liberadas e pagas: não diminui quando uma parcela é paga
        assertThat(resumo.projecao()).isEqualByComparingTo("2500.00");
        assertThat(resumo.tendencia()).isEqualTo("+25%");
        assertThat(resumo.tendenciaVendas()).isEqualTo("+100%");
        assertThat(resumo.tendenciaLiberadas()).isEqualTo("+50%");

        assertThat(painel.parcelasDoPeriodo())
                .extracting(ParcelaDTO::getPedidoId, ParcelaDTO::getCliente, ParcelaDTO::getStatus, ParcelaDTO::getLabel)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(5, "Amazon", "PENDENTE", "Parcela 1/4"),
                        org.assertj.core.groups.Tuple.tuple(9, "Razao 9", "PAGA", "Parcela 1/1")
                );
    }

    @Test
    void buscarPainel_deveRetornarNovo_quandoMesAnteriorNaoTemBase() {
        prepararPaginaVazia();
        when(pedidoRepository.contarVendasDoPeriodo(VENDEDOR, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(1L);
        when(pedidoRepository.contarVendasDoPeriodo(VENDEDOR, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(0L);
        when(comissaoRepository.resumirPorStatus(
                VENDEDOR, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), StatusParcela.CANCELADO))
                .thenReturn(List.of(new ResumoStatusDTO(StatusComissao.LIBERADA, 1L, new BigDecimal("150.00"))));
        when(comissaoRepository.resumirPorStatus(
                VENDEDOR, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), StatusParcela.CANCELADO))
                .thenReturn(List.of());
        when(comissaoRepository.buscarComissoesPorStatusNoPeriodo(eq(VENDEDOR), anyList(), any(), any(), any()))
                .thenReturn(List.of());

        HomeVendedorPainelDTO.ResumoDTO resumo = homeVendedorService.buscarPainel(
                VENDEDOR, 2026, 10, null, FiltroStatusVenda.TODAS, PRIMEIRA_PAGINA).resumo();

        assertThat(resumo.tendencia()).isEqualTo("Novo");
        assertThat(resumo.tendenciaVendas()).isEqualTo("Novo");
        assertThat(resumo.tendenciaLiberadas()).isEqualTo("Novo");
    }

    @Test
    void buscarPainel_deveCompararComOMesmoDiaDoMesAnterior_quandoDiaEhInformado() {
        prepararPaginaVazia();
        when(pedidoRepository.contarVendasDoPeriodo(eq(VENDEDOR), any(), any()))
                .thenReturn(0L);
        when(comissaoRepository.resumirPorStatus(eq(VENDEDOR), any(), any(), eq(StatusParcela.CANCELADO)))
                .thenReturn(List.of());
        when(comissaoRepository.buscarComissoesPorStatusNoPeriodo(eq(VENDEDOR), anyList(), any(), any(), any()))
                .thenReturn(List.of());

        homeVendedorService.buscarPainel(VENDEDOR, 2026, 3, 31, FiltroStatusVenda.TODAS, PRIMEIRA_PAGINA);

        // 31/03 compara com o último dia de fevereiro
        LocalDate diaAnterior = LocalDate.of(2026, 2, 28);
        verify(comissaoRepository).resumirPorStatus(VENDEDOR, diaAnterior, diaAnterior, StatusParcela.CANCELADO);
        verify(pedidoRepository).contarVendasDoPeriodo(VENDEDOR, diaAnterior, diaAnterior);
    }

    private void prepararPaginaVazia() {
        when(pedidoRepository.listarPedidosDoPainel(
                eq(VENDEDOR), any(), any(), anyList(), any(), anyBoolean(), anyBoolean(), any(Sort.class)))
                .thenReturn(List.of());
    }

    private Pedido pedido(Integer idPedido, LocalDate dataPedido) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(idPedido);
        cliente.setNomeFantasia("Cliente " + idPedido);
        cliente.setRazaoSocial("Razao " + idPedido);

        Pedido pedido = new Pedido();
        pedido.setIdPedido(idPedido);
        pedido.setCliente(cliente);
        pedido.setDataPedido(dataPedido);
        pedido.setStatusPedido("APROVADO");
        return pedido;
    }

    private Comissao comissao(Pedido pedido, Integer numeroParcela, Integer totalParcelas,
                              String valor, StatusComissao status, LocalDate vencimento) {
        Comissao comissao = comissao(pedido.getIdPedido(), "", numeroParcela, totalParcelas, valor, status);
        comissao.setPedido(pedido);
        comissao.getParcela().setDataVencimento(vencimento);
        comissao.getParcela().setStatusParcela(StatusParcela.PENDENTE);
        return comissao;
    }

    private Comissao comissao(Integer idPedido, String nomeFantasia, Integer numeroParcela, Integer totalParcelas,
                              String valor, StatusComissao status) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(idPedido);
        cliente.setNomeFantasia(nomeFantasia);
        cliente.setRazaoSocial("Razao " + idPedido);

        Pedido pedido = new Pedido();
        pedido.setIdPedido(idPedido);
        pedido.setCliente(cliente);

        Pagamento pagamento = new Pagamento();
        pagamento.setMetodoPagamento(MetodoPagamento.BOLETO);
        pagamento.setQuantidadeParcelas(totalParcelas);

        Parcela parcela = new Parcela();
        parcela.setId(idPedido * 10 + numeroParcela);
        parcela.setNumeroParcela(numeroParcela);
        parcela.setPagamento(pagamento);
        parcela.setDataVencimento(LocalDate.of(2026, 10, 15));

        Comissao comissao = new Comissao();
        comissao.setPedido(pedido);
        comissao.setParcela(parcela);
        comissao.setValorComissao(new BigDecimal(valor));
        comissao.setStatusComissao(status);
        return comissao;
    }
}
