package com.comissions.korp.service;

import com.comissions.korp.DTO.HomeVendedorDTO.FiltroStatusVenda;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
        prepararPaginaVazia(0);
        when(pedidoRepository.contarPedidosDoPainel(eq(VENDEDOR), any(), any(), anyList(), any(), anyBoolean(), anyBoolean()))
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
        assertThat(painel.vendas()).isEmpty();
    }

    @Test
    void buscarPainel_deveSomarTodasAsParcelasECompararCadaCardComOMesAnterior() {
        prepararPaginaVazia(4);
        when(pedidoRepository.contarPedidosDoPainel(
                eq(VENDEDOR), eq(LocalDate.of(2026, 9, 1)), eq(LocalDate.of(2026, 9, 30)),
                anyList(), any(), anyBoolean(), anyBoolean()))
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
        prepararPaginaVazia(1);
        when(pedidoRepository.contarPedidosDoPainel(eq(VENDEDOR), any(), any(), anyList(), any(), anyBoolean(), anyBoolean()))
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
        prepararPaginaVazia(0);
        when(pedidoRepository.contarPedidosDoPainel(eq(VENDEDOR), any(), any(), anyList(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(0L);
        when(comissaoRepository.resumirPorStatus(eq(VENDEDOR), any(), any(), eq(StatusParcela.CANCELADO)))
                .thenReturn(List.of());
        when(comissaoRepository.buscarComissoesPorStatusNoPeriodo(eq(VENDEDOR), anyList(), any(), any(), any()))
                .thenReturn(List.of());

        homeVendedorService.buscarPainel(VENDEDOR, 2026, 3, 31, FiltroStatusVenda.TODAS, PRIMEIRA_PAGINA);

        // 31/03 compara com o último dia de fevereiro
        LocalDate diaAnterior = LocalDate.of(2026, 2, 28);
        verify(comissaoRepository).resumirPorStatus(VENDEDOR, diaAnterior, diaAnterior, StatusParcela.CANCELADO);
        verify(pedidoRepository).contarPedidosDoPainel(
                eq(VENDEDOR), eq(diaAnterior), eq(diaAnterior), anyList(), any(), anyBoolean(), anyBoolean());
    }

    private void prepararPaginaVazia(long totalVendas) {
        when(pedidoRepository.buscarPedidosDoPainel(
                eq(VENDEDOR), any(), any(), anyList(), any(), anyBoolean(), anyBoolean(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PRIMEIRA_PAGINA, totalVendas));
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
