package com.comissions.korp.service;

import com.comissions.korp.DTO.HomeFinanceiroDTO.ComissaoParcelaDTO;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Comissao;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ENUM.StatusParcela;
import com.comissions.korp.entity.Pagamento;
import com.comissions.korp.entity.Parcela;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.repository.ComissaoRepository;
import com.comissions.korp.repository.FinanceiroRepository;
import com.comissions.korp.repository.ItemPedidoRepository;
import com.comissions.korp.service.financeiro.ComissoesFinanceiroService;
import com.comissions.korp.service.financeiro.PeriodoFinanceiro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Código do pedido (DDMMAA-P01) na tela de Comissões: exibição e busca por pedaços. */
@ExtendWith(MockitoExtension.class)
class ComissoesFinanceiroServiceTest {

    @Mock
    private FinanceiroRepository financeiroRepository;
    @Mock
    private ItemPedidoRepository itemPedidoRepository;
    @Mock
    private ComissaoRepository comissaoRepository;

    private ComissoesFinanceiroService service;

    @BeforeEach
    void setUp() {
        service = new ComissoesFinanceiroService(financeiroRepository, itemPedidoRepository, comissaoRepository);
        // 251126-P03 e 261126-P14
        when(financeiroRepository.buscarComissoesDoPeriodo(any(), any(), any(), any())).thenReturn(List.of(
                comissao(30, LocalDate.of(2026, 11, 25), 3),
                comissao(31, LocalDate.of(2026, 11, 26), 14)
        ));
    }

    @Test
    void buscarPainel_deveExibirCodigoNoNovoFormato() {
        assertThat(codigos(null)).containsExactly("251126-P03", "261126-P14");
    }

    @Test
    void buscarPainel_deveAceitarPedacosDoCodigo() {
        assertThat(codigos("251126-P03")).containsExactly("251126-P03");
        assertThat(codigos("P03")).containsExactly("251126-P03");
        assertThat(codigos("p3")).containsExactly("251126-P03");
        assertThat(codigos("P14")).containsExactly("261126-P14");
        assertThat(codigos("251126")).containsExactly("251126-P03");
        assertThat(codigos("25/11/26")).containsExactly("251126-P03");
        assertThat(codigos("26/11")).containsExactly("261126-P14");
        assertThat(codigos("1126")).containsExactly("251126-P03", "261126-P14");
    }

    @Test
    void buscarPainel_naoDeveEncontrarCodigoInexistente() {
        assertThat(codigos("P99")).isEmpty();
        assertThat(codigos("VD-30")).isEmpty();
    }

    private List<String> codigos(String busca) {
        return service.buscarPainel(PeriodoFinanceiro.de(2026, 11), busca, null, 0, 10)
                .pagamentosPeriodo().stream()
                .map(ComissaoParcelaDTO::codigoPedido)
                .toList();
    }

    private Comissao comissao(Integer idPedido, LocalDate dataPedido, Integer numeroDia) {
        Cliente cliente = new Cliente();
        cliente.setRazaoSocial("Cliente LTDA");

        Usuario vendedor = new Usuario();
        vendedor.setNome("Vendedor");

        Pedido pedido = new Pedido();
        pedido.setIdPedido(idPedido);
        pedido.setDataPedido(dataPedido);
        pedido.setNumeroDia(numeroDia);
        pedido.setCliente(cliente);

        Pagamento pagamento = new Pagamento();
        pagamento.setQuantidadeParcelas(1);

        Parcela parcela = new Parcela();
        parcela.setNumeroParcela(1);
        parcela.setDataVencimento(dataPedido);
        parcela.setStatusParcela(StatusParcela.PENDENTE);
        parcela.setPagamento(pagamento);

        Comissao comissao = new Comissao();
        comissao.setPedido(pedido);
        comissao.setParcela(parcela);
        comissao.setUsuario(vendedor);
        comissao.setValorComissao(new BigDecimal("10.00"));
        comissao.setStatusComissao(StatusComissao.PENDENTE);
        return comissao;
    }
}
