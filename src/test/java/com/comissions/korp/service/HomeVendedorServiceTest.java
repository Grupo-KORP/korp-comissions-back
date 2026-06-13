package com.comissions.korp.service;

import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Comissao;
import com.comissions.korp.entity.Contato;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.ENUM.MetodoPagamento;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.Endereco;
import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.entity.Pagamento;
import com.comissions.korp.entity.Parcela;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.entity.Produto;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeVendedorServiceTest {

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
        // Arrange / Act / Assert
        assertThatThrownBy(() -> homeVendedorService.buscarPainel(99, 2026, 13, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("12");

        verify(comissaoRepository, never()).buscarComissoesDoPainelPorVencimento(any(), any(), any());
    }

    @Test
    void buscarPainel_deveRetornarResumoZerado_quandoNaoHaVendasNemComissoesNoPeriodo() {
        // Arrange
        when(comissaoRepository.buscarComissoesDoPainelPorVencimento(eq(99), any(), any()))
                .thenReturn(List.of(), List.of());
        when(pedidoRepository.findByUsuario_IdUsuarioAndAtivoTrueAndStatusPedidoAndDataPedidoBetweenOrderByDataPedidoDescIdPedidoDesc(
                eq(99), eq("EM_ANDAMENTO"), any(), any()))
                .thenReturn(List.of());

        // Act
        HomeVendedorResponseDTO response = homeVendedorService.buscarPainel(99, 2026, 6, null);

        // Assert
        assertThat(response.getAno()).isEqualTo(2026);
        assertThat(response.getMes()).isEqualTo(6);
        assertThat(response.getNomeMes()).isEqualTo("Junho");
        assertThat(response.getTotalVendas()).isZero();
        assertThat(response.getComissoesLiberadas()).isZero();
        assertThat(response.getPagamentosPendentes()).isZero();
        assertThat(response.getProjecao()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTendencia()).isEqualTo("0%");
        assertThat(response.getVendas()).isEmpty();
        assertThat(response.getDetalhesVenda()).isEmpty();
    }

    @Test
    void buscarPainel_deveCombinarPedidosSemDuplicarESomarIndicadoresDoPeriodo() {
        // Arrange
        Cliente cliente = cliente(10);
        Distribuidor distribuidor = distribuidor(20);
        Pedido pedidoComComissao = pedido(1, "APROVADO", cliente, distribuidor);
        Pedido pedidoEmAndamento = pedido(2, "EM_ANDAMENTO", cliente, distribuidor);

        Comissao comissaoLiberada = comissao(pedidoComComissao, 1, 2, "100.00", StatusComissao.LIBERADA);
        Comissao comissaoPendente = comissao(pedidoComComissao, 2, 2, "50.00", StatusComissao.PENDENTE);
        Comissao comissaoMesAnterior = comissao(pedidoComComissao, 1, 1, "50.00", StatusComissao.LIBERADA);

        when(comissaoRepository.buscarComissoesDoPainelPorVencimento(eq(99), any(), any()))
                .thenReturn(List.of(comissaoLiberada, comissaoPendente), List.of(comissaoMesAnterior));
        when(pedidoRepository.findByUsuario_IdUsuarioAndAtivoTrueAndStatusPedidoAndDataPedidoBetweenOrderByDataPedidoDescIdPedidoDesc(
                eq(99), eq("EM_ANDAMENTO"), any(), any()))
                .thenReturn(List.of(pedidoComComissao, pedidoEmAndamento));
        when(comissaoRepository.findByPedidoIn(List.of(pedidoComComissao, pedidoEmAndamento)))
                .thenReturn(List.of(comissaoLiberada, comissaoPendente));
        when(itemPedidoRepository.findByPedido(pedidoComComissao)).thenReturn(List.of(itemPedido(101)));
        when(itemPedidoRepository.findByPedido(pedidoEmAndamento)).thenReturn(List.of());
        when(enderecoRepository.findFirstByCliente_IdCliente(10)).thenReturn(Optional.of(endereco()));
        when(enderecoRepository.findFirstByDistribuidor_IdDistribuidor(20)).thenReturn(Optional.of(endereco()));
        when(contatoRepository.findByClienteAndAtivoTrue(cliente)).thenReturn(List.of(contato("Contato Cliente")));
        when(contatoRepository.findByDistribuidorAndAtivoTrue(distribuidor)).thenReturn(List.of(contato("Contato Distribuidor")));
        when(pagamentoRepository.findByPedido_IdPedido(1)).thenReturn(Optional.of(pagamento(2)));
        when(pagamentoRepository.findByPedido_IdPedido(2)).thenReturn(Optional.empty());

        // Act
        HomeVendedorResponseDTO response = homeVendedorService.buscarPainel(99, 2026, 6, null);

        // Assert
        assertThat(response.getTotalVendas()).isEqualTo(2);
        assertThat(response.getComissoesLiberadas()).isEqualTo(1);
        assertThat(response.getPagamentosPendentes()).isEqualTo(1);
        assertThat(response.getParcelas()).isEqualTo(1);
        assertThat(response.getProjecao()).isEqualByComparingTo("100.00");
        assertThat(response.getTendencia()).isEqualTo("+100%");

        assertThat(response.getVendas()).hasSize(2);
        HomeVendedorResponseDTO.VendaResumoDTO vendaComComissao = response.getVendas().getFirst();
        assertThat(vendaComComissao.getId()).isEqualTo("V1");
        assertThat(vendaComComissao.getValorComissao()).isEqualByComparingTo("150.00");
        assertThat(vendaComComissao.getStatus()).isEqualTo("LIBERADA 1 PARCELAS");
        assertThat(vendaComComissao.getTipo()).isEqualTo("liberada");
        assertThat(vendaComComissao.getParcelas()).extracting(HomeVendedorResponseDTO.ParcelaDTO::getNumeroParcela)
                .containsExactly(1, 2);

        HomeVendedorResponseDTO.DetalheVendaDTO detalhe = response.getDetalhesVenda().get("Junho-V1");
        assertThat(detalhe).isNotNull();
        assertThat(detalhe.getCliente().getContato()).isEqualTo("Contato Cliente");
        assertThat(detalhe.getDistribuidor().getContato()).isEqualTo("Contato Distribuidor");
        assertThat(detalhe.getProduto().getIdItemPedido()).isEqualTo(101);
        assertThat(detalhe.getPedido().getMetodoPagamento()).isEqualTo("BOLETO");
    }

    private Pedido pedido(Integer id, String status, Cliente cliente, Distribuidor distribuidor) {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(id);
        pedido.setStatusPedido(status);
        pedido.setDataPedido(LocalDate.of(2026, 6, 10));
        pedido.setNumeroNotaDistribuidor("NF-" + id);
        pedido.setObservacoes("Obs");
        pedido.setValorTotalDistr(new BigDecimal("70.00"));
        pedido.setValorTotalCliente(new BigDecimal("100.00"));
        pedido.setCliente(cliente);
        pedido.setDistribuidor(distribuidor);
        return pedido;
    }

    private Comissao comissao(Pedido pedido, Integer numeroParcela, Integer totalParcelas, String valor, StatusComissao status) {
        Parcela parcela = new Parcela();
        parcela.setId(numeroParcela);
        parcela.setNumeroParcela(numeroParcela);
        parcela.setPagamento(pagamento(totalParcelas));

        Comissao comissao = new Comissao();
        comissao.setPedido(pedido);
        comissao.setParcela(parcela);
        comissao.setValorComissao(new BigDecimal(valor));
        comissao.setStatusComissao(status);
        return comissao;
    }

    private Pagamento pagamento(Integer quantidadeParcelas) {
        Pagamento pagamento = new Pagamento();
        pagamento.setMetodoPagamento(MetodoPagamento.BOLETO);
        pagamento.setParcelado(true);
        pagamento.setQuantidadeParcelas(quantidadeParcelas);
        return pagamento;
    }

    private ItemPedido itemPedido(Integer id) {
        Produto produto = new Produto();
        produto.setIdProduto(501);
        produto.setNome("Produto Teste");
        produto.setCodigoProduto("PN-501");

        ItemPedido item = new ItemPedido();
        item.setIdItemPedido(id);
        item.setProduto(produto);
        item.setQuantidade(2);
        item.setVlrUnitDistr(new BigDecimal("10.00"));
        item.setVlrTotalDistr(new BigDecimal("20.00"));
        item.setVlrUnitCliente(new BigDecimal("15.00"));
        item.setVlrTotalCliente(new BigDecimal("30.00"));
        return item;
    }

    private Cliente cliente(Integer id) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(id);
        cliente.setRazaoSocial("Cliente Razao");
        cliente.setNomeFantasia("Cliente Fantasia");
        cliente.setCnpj("11222333000144");
        cliente.setInscricaoEstadual("IE-1");
        cliente.setTelefone("1111-1111");
        cliente.setEmail("cliente@email.com");
        return cliente;
    }

    private Distribuidor distribuidor(Integer id) {
        Distribuidor distribuidor = new Distribuidor();
        distribuidor.setIdDistribuidor(id);
        distribuidor.setRazaoSocial("Distribuidor Razao");
        distribuidor.setNomeFantasia("Distribuidor Fantasia");
        distribuidor.setCnpj("55666777000188");
        distribuidor.setInscricaoEstadual("IE-2");
        distribuidor.setTelefone("2222-2222");
        distribuidor.setEmail("distribuidor@email.com");
        return distribuidor;
    }

    private Endereco endereco() {
        Endereco endereco = new Endereco();
        endereco.setLogradouro("Rua Teste");
        endereco.setNumero("123");
        endereco.setBairro("Centro");
        endereco.setCidade("Sao Paulo");
        endereco.setEstado("SP");
        endereco.setCep("01000-000");
        return endereco;
    }

    private Contato contato(String nome) {
        Contato contato = new Contato();
        contato.setNome(nome);
        contato.setEmail(nome.replace(" ", ".").toLowerCase() + "@email.com");
        contato.setTelefone("9999-9999");
        return contato;
    }
}
