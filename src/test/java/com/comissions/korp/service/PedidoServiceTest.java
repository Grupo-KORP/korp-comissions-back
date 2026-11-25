package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteDTO.ClientePedidoResponseDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorPedidoResponseDTO;
import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoRequest;
import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoResumoResponse;
import com.comissions.korp.DTO.PagamentoDTO;
import com.comissions.korp.DTO.PedidoDTO.PedidoEditRequest;
import com.comissions.korp.DTO.PedidoDTO.PedidoRequest;
import com.comissions.korp.DTO.PedidoDTO.PedidoResponse;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Comissao;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.ENUM.MetodoPagamento;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ENUM.StatusParcela;
import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.entity.Pagamento;
import com.comissions.korp.entity.Parcela;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.entity.Produto;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.OperacaoNaoPermitida;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.RequisicaoInvalida;
import com.comissions.korp.repository.ParcelaRepository;
import com.comissions.korp.repository.ComissaoRepository;
import com.comissions.korp.repository.ItemPedidoRepository;
import com.comissions.korp.repository.PagamentoRepository;
import com.comissions.korp.repository.PedidoRepository;
import com.comissions.korp.repository.ProdutoRepository;
import com.comissions.korp.repository.UsuarioRepository;
import com.comissions.korp.service.Email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ItemPedidoRepository itemPedidoRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ClienteService clienteService;
    @Mock
    private DistribuidorService distribuidorService;
    @Mock
    private ItemPedidoService itemPedidoService;
    @Mock
    private EmailService emailService;
    @Mock
    private PagamentoRepository pagamentoRepository;
    @Mock
    private ParcelaRepository parcelaRepository;
    @Mock
    private ComissaoRepository comissaoRepository;
    @Mock
    private PedidoRascunhoService rascunhoService;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                pedidoRepository,
                itemPedidoRepository,
                produtoRepository,
                clienteService,
                distribuidorService,
                itemPedidoService,
                usuarioRepository,
                emailService,
                pagamentoRepository,
                parcelaRepository,
                comissaoRepository,
                rascunhoService
        );
    }

    @Test
    void cadastrarPedido_deveSalvarPedidoEItensComTotaisCalculados_quandoRequestValido() {
        // Arrange
        PedidoRequest request = pedidoRequest(List.of(itemRequest(1, 2, "10.00", "15.00")));
        Usuario vendedor = usuario(99);
        Cliente cliente = cliente(10);
        Distribuidor distribuidor = distribuidor(20);
        Produto produto = produto(1);

        when(usuarioRepository.findById(99)).thenReturn(Optional.of(vendedor));
        when(clienteService.buscarClientePorId(10)).thenReturn(cliente);
        when(distribuidorService.buscarDistribuidorPorId(20)).thenReturn(distribuidor);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setIdPedido(30);
            return pedido;
        });
        when(produtoRepository.findByIdProdutoIn(List.of(1))).thenReturn(List.of(produto));
        when(itemPedidoRepository.save(any(ItemPedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoService.convertToItemPedidoResumoResponse(any(ItemPedido.class))).thenReturn(new ItemPedidoResumoResponse());

        // Act
        PedidoResponse response = pedidoService.cadastrarPedido(request, 99);

        // Assert
        assertThat(response.getIdPedido()).isEqualTo(30);
        assertThat(response.getStatusPedido()).isEqualTo("EM_ANDAMENTO");
        assertThat(response.getMetodoPagamento()).isEqualTo(MetodoPagamento.BOLETO);
        assertThat(response.getValorTotalDistr()).isEqualByComparingTo("20.00");
        assertThat(response.getValorTotalCliente()).isEqualByComparingTo("30.00");

        ArgumentCaptor<ItemPedido> itemCaptor = ArgumentCaptor.forClass(ItemPedido.class);
        verify(itemPedidoRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getPedido().getIdPedido()).isEqualTo(30);
        assertThat(itemCaptor.getValue().getProduto()).isSameAs(produto);
    }

    @Test
    void cadastrarPedido_deveGerarCodigoComProximoNumeroDoDia() {
        PedidoRequest request = pedidoRequest(List.of(itemRequest(1, 2, "10.00", "15.00")));
        LocalDate hoje = LocalDate.now();

        when(usuarioRepository.findById(99)).thenReturn(Optional.of(usuario(99)));
        when(clienteService.buscarClientePorId(10)).thenReturn(cliente(10));
        when(distribuidorService.buscarDistribuidorPorId(20)).thenReturn(distribuidor(20));
        when(produtoRepository.findByIdProdutoIn(List.of(1))).thenReturn(List.of(produto(1)));
        when(pedidoRepository.maiorNumeroDoDia(hoje)).thenReturn(2);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoRepository.save(any(ItemPedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoService.convertToItemPedidoResumoResponse(any(ItemPedido.class))).thenReturn(new ItemPedidoResumoResponse());

        PedidoResponse response = pedidoService.cadastrarPedido(request, 99);

        String esperado = hoje.format(java.time.format.DateTimeFormatter.ofPattern("ddMMyy")) + "-P03";
        assertThat(response.getCodigo()).isEqualTo(esperado);
        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertThat(pedidoCaptor.getValue().getNumeroDia()).isEqualTo(3);
    }

    @Test
    void cadastrarPedido_deveFalhar_quandoMetodoPagamentoNaoInformado() {
        // Arrange
        PedidoRequest request = pedidoRequest(List.of(itemRequest(1, 2, "10.00", "15.00")));
        request.setMetodoPagamento(null);

        // Act / Assert
        assertThatThrownBy(() -> pedidoService.cadastrarPedido(request, 99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("método de pagamento");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void cadastrarPedido_deveGravarMetodoPagamentoNoPedido() {
        // Arrange
        PedidoRequest request = pedidoRequest(List.of(itemRequest(1, 2, "10.00", "15.00")));
        request.setMetodoPagamento(MetodoPagamento.AVISTA);

        when(usuarioRepository.findById(99)).thenReturn(Optional.of(usuario(99)));
        when(clienteService.buscarClientePorId(10)).thenReturn(cliente(10));
        when(distribuidorService.buscarDistribuidorPorId(20)).thenReturn(distribuidor(20));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produtoRepository.findByIdProdutoIn(List.of(1))).thenReturn(List.of(produto(1)));
        when(itemPedidoRepository.save(any(ItemPedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoService.convertToItemPedidoResumoResponse(any(ItemPedido.class))).thenReturn(new ItemPedidoResumoResponse());

        // Act
        pedidoService.cadastrarPedido(request, 99);

        // Assert
        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertThat(pedidoCaptor.getValue().getMetodoPagamento()).isEqualTo(MetodoPagamento.AVISTA);
    }

    @Test
    void cadastrarPedido_deveExcluirRascunhoDeOrigem_quandoRequestTemIdRascunho() {
        // Arrange
        PedidoRequest request = pedidoRequest(List.of(itemRequest(1, 2, "10.00", "10.00")));
        request.setIdRascunho(7);

        when(usuarioRepository.findById(99)).thenReturn(Optional.of(usuario(99)));
        when(clienteService.buscarClientePorId(10)).thenReturn(cliente(10));
        when(distribuidorService.buscarDistribuidorPorId(20)).thenReturn(distribuidor(20));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produtoRepository.findByIdProdutoIn(List.of(1))).thenReturn(List.of(produto(1)));
        when(itemPedidoRepository.save(any(ItemPedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoService.convertToItemPedidoResumoResponse(any(ItemPedido.class))).thenReturn(new ItemPedidoResumoResponse());

        // Act (valor faturado igual ao do distribuidor é permitido)
        pedidoService.cadastrarPedido(request, 99);

        // Assert
        verify(rascunhoService).excluirAposCriarPedido(99, 7);
    }

    @Test
    void cadastrarPedido_deveFalhar_quandoValorFaturadoMenorQueValorDoDistribuidor() {
        // Arrange
        PedidoRequest request = pedidoRequest(List.of(
                itemRequest(1, 2, "10.00", "15.00"),
                itemRequest(2, 1, "20.00", "19.99")));

        // Act / Assert
        assertThatThrownBy(() -> pedidoService.cadastrarPedido(request, 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("Item 2")
                .hasMessageContaining("não pode ser menor");

        verify(pedidoRepository, never()).save(any());
        verify(rascunhoService, never()).excluirAposCriarPedido(any(), any());
    }

    @Test
    void cadastrarPedido_deveFalhar_quandoNaoHaItens() {
        for (List<ItemPedidoRequest> itens : java.util.Arrays.asList(null, List.<ItemPedidoRequest>of())) {
            PedidoRequest request = pedidoRequest(itens);

            assertThatThrownBy(() -> pedidoService.cadastrarPedido(request, 99))
                    .isInstanceOf(RequisicaoInvalida.class)
                    .hasMessage("Informe ao menos um item no pedido.");
        }
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void cadastrarPedido_deveFalhar_quandoItemTemQuantidadeInvalida() {
        ItemPedidoRequest semQuantidade = itemRequest(2, 1, "10.00", "12.00");
        semQuantidade.setQuantidade(null);

        for (ItemPedidoRequest invalido : List.of(itemRequest(2, 0, "10.00", "12.00"), itemRequest(2, -3, "10.00", "12.00"), semQuantidade)) {
            PedidoRequest request = pedidoRequest(List.of(itemRequest(1, 2, "10.00", "15.00"), invalido));

            assertThatThrownBy(() -> pedidoService.cadastrarPedido(request, 99))
                    .isInstanceOf(RequisicaoInvalida.class)
                    .hasMessage("Item 2: a quantidade deve ser maior que zero.");
        }
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void cadastrarPedido_deveFalhar_quandoItemNaoTemProdutoOuTemValorNegativo() {
        ItemPedidoRequest semProduto = itemRequest(1, 1, "10.00", "12.00");
        semProduto.setFkProduto(null);

        assertThatThrownBy(() -> pedidoService.cadastrarPedido(pedidoRequest(List.of(semProduto)), 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessage("Item 1: informe o produto.");
        assertThatThrownBy(() -> pedidoService.cadastrarPedido(pedidoRequest(List.of(itemRequest(1, 1, "-5.00", "12.00"))), 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessage("Item 1: os valores unitários não podem ser negativos.");
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void cadastrarPedido_deveFalhar_quandoClienteOuDistribuidorNaoInformado() {
        PedidoRequest semCliente = pedidoRequest(List.of(itemRequest(1, 1, "10.00", "12.00")));
        semCliente.setCliente(null);
        PedidoRequest semDistribuidor = pedidoRequest(List.of(itemRequest(1, 1, "10.00", "12.00")));
        semDistribuidor.getDistribuidor().setId(null);

        assertThatThrownBy(() -> pedidoService.cadastrarPedido(semCliente, 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessage("Informe o cliente do pedido.");
        assertThatThrownBy(() -> pedidoService.cadastrarPedido(semDistribuidor, 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessage("Informe o distribuidor do pedido.");
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void cadastrarPedido_deveRecalcularTotaisDoItem_ignorandoOsEnviadosNoRequest() {
        ItemPedidoRequest item = itemRequest(1, 2, "10.00", "15.00");
        item.setVlrTotalDistr(new BigDecimal("999.00"));
        item.setVlrTotalCliente(new BigDecimal("1.00"));
        PedidoRequest request = pedidoRequest(List.of(item));

        when(usuarioRepository.findById(99)).thenReturn(Optional.of(usuario(99)));
        when(clienteService.buscarClientePorId(10)).thenReturn(cliente(10));
        when(distribuidorService.buscarDistribuidorPorId(20)).thenReturn(distribuidor(20));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produtoRepository.findByIdProdutoIn(List.of(1))).thenReturn(List.of(produto(1)));
        when(itemPedidoRepository.save(any(ItemPedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoService.convertToItemPedidoResumoResponse(any(ItemPedido.class))).thenReturn(new ItemPedidoResumoResponse());

        pedidoService.cadastrarPedido(request, 99);

        ArgumentCaptor<ItemPedido> itemCaptor = ArgumentCaptor.forClass(ItemPedido.class);
        verify(itemPedidoRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getVlrTotalDistr()).isEqualByComparingTo("20.00");
        assertThat(itemCaptor.getValue().getVlrTotalCliente()).isEqualByComparingTo("30.00");
    }

    @Test
    void editarPedido_deveFalhar_quandoItemDoPedidoTemValorFaturadoMenorQueDoDistribuidor() {
        // Arrange
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        ItemPedido item = new ItemPedido();
        item.setProduto(produto(1));
        item.setVlrUnitDistr(new BigDecimal("10.00"));
        item.setVlrUnitCliente(new BigDecimal("9.00"));

        PedidoEditRequest edicao = new PedidoEditRequest();
        edicao.setIdPedido(30);

        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(itemPedidoRepository.findByPedido(pedido)).thenReturn(List.of(item));

        // Act / Assert
        assertThatThrownBy(() -> pedidoService.editarPedidoFromPedidoEditRequest(edicao, 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("Produto 1");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void salvarItensDoPedido_deveFalhar_quandoProdutoNaoFoiEncontradoNoMapa() {
        // Arrange
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        List<ItemPedidoRequest> itens = List.of(itemRequest(999, 1, "10.00", "12.00"));

        // Act / Assert
        assertThatThrownBy(() -> pedidoService.salvarItensDoPedido(pedido, itens, Map.of()))
                .isInstanceOf(RecursoNaoEncontrado.class)
                .hasMessageContaining("999");

        verify(itemPedidoRepository, never()).save(any());
    }

    @Test
    void calcularValorTotal_deveSomarValoresUnitariosMultiplicadosPelaQuantidade() {
        // Arrange
        List<ItemPedidoRequest> itens = List.of(
                itemRequest(1, 2, "10.00", "15.00"),
                itemRequest(2, 3, "7.50", "9.00")
        );

        // Act
        BigDecimal totalRevenda = pedidoService.calcularValorTotalRevenda(itens);
        BigDecimal totalFaturamento = pedidoService.calcularValorTotalFaturamento(itens);

        // Assert
        assertThat(totalRevenda).isEqualByComparingTo("42.50");
        assertThat(totalFaturamento).isEqualByComparingTo("57.00");
    }

    @Test
    void criarComissao_deveCriarPagamentoParcelasEComissoesEAprovarPedido_quandoPedidoEstaEmAndamento() {
        // Arrange
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        PagamentoDTO pagamentoDTO = pagamentoDTO(MetodoPagamento.BOLETO, 10, 30, 60);

        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(pagamentoRepository.existsByPedido_IdPedido(30)).thenReturn(false);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String mensagem = pedidoService.criarComissao(30, pagamentoDTO, 99);

        // Assert
        assertThat(mensagem).contains("30");
        assertThat(pedido.getStatusPedido()).isEqualTo("APROVADO");
        assertThat(pedido.getMetodoPagamento()).isEqualTo(MetodoPagamento.BOLETO);
        verify(pedidoRepository).save(pedido);

        ArgumentCaptor<List<Parcela>> parcelasCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Comissao>> comissoesCaptor = ArgumentCaptor.forClass(List.class);
        verify(parcelaRepository).saveAll(parcelasCaptor.capture());
        verify(comissaoRepository).saveAll(comissoesCaptor.capture());

        List<Parcela> parcelas = parcelasCaptor.getValue();
        List<Comissao> comissoes = comissoesCaptor.getValue();
        assertThat(parcelas).hasSize(3);
        assertThat(parcelas).extracting(Parcela::getNumeroParcela).containsExactly(1, 2, 3);
        assertThat(parcelas).extracting(Parcela::getPrazoDias).containsExactly(10, 30, 60);
        assertThat(parcelas).extracting(Parcela::getDataVencimento).containsExactly(
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(30), LocalDate.now().plusDays(60));
        assertThat(parcelas).allSatisfy(parcela -> assertThat(parcela.getStatusParcela()).isEqualTo(StatusParcela.PENDENTE));
        assertThat(comissoes).hasSize(3);
        assertThat(comissoes).allSatisfy(comissao -> {
            assertThat(comissao.getPedido()).isSameAs(pedido);
            assertThat(comissao.getStatusComissao()).isEqualTo(StatusComissao.PENDENTE);
            assertThat(comissao.getValorComissao()).isEqualByComparingTo("1.00");
        });
    }

    @Test
    void criarComissao_deveFalhar_quandoPedidoNaoEstaEmAndamento() {
        // Arrange
        Pedido pedido = pedido(30, "APROVADO");
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));

        // Act / Assert
        assertThatThrownBy(() -> pedidoService.criarComissao(30, pagamentoDTO(MetodoPagamento.BOLETO, 30, 60), 99))
                .isInstanceOf(OperacaoNaoPermitida.class)
                .hasMessageContaining("em andamento");

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void criarComissao_deveFalhar_quandoPedidoJaPossuiPagamento() {
        // Arrange
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(pagamentoRepository.existsByPedido_IdPedido(30)).thenReturn(true);

        // Act / Assert
        assertThatThrownBy(() -> pedidoService.criarComissao(30, pagamentoDTO(MetodoPagamento.BOLETO, 30, 60), 99))
                .isInstanceOf(OperacaoNaoPermitida.class)
                .hasMessageContaining("pagamento");

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void criarComissao_deveCriarUmaParcelaComPrazoInformado_quandoPagamentoAVista() {
        // Arrange
        Pedido pedido = pedido(30, "EM_ANDAMENTO");

        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(pagamentoRepository.existsByPedido_IdPedido(30)).thenReturn(false);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        pedidoService.criarComissao(30, pagamentoDTO(MetodoPagamento.AVISTA, 0), 99);

        // Assert
        ArgumentCaptor<List<Parcela>> parcelasCaptor = ArgumentCaptor.forClass(List.class);
        verify(parcelaRepository).saveAll(parcelasCaptor.capture());
        List<Parcela> parcelas = parcelasCaptor.getValue();
        assertThat(parcelas).hasSize(1);
        assertThat(parcelas.getFirst().getDataVencimento()).isEqualTo(LocalDate.now());
        assertThat(parcelas.getFirst().getValorParcela()).isEqualByComparingTo("100.00");
        assertThat(pedido.getMetodoPagamento()).isEqualTo(MetodoPagamento.AVISTA);
    }

    @Test
    void criarComissao_deveUsarPercentualDoVendedorDoPedido() {
        // Arrange
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        pedido.getUsuario().setPercentualComissao(new BigDecimal("20"));

        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(pagamentoRepository.existsByPedido_IdPedido(30)).thenReturn(false);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        pedidoService.criarComissao(30, pagamentoDTO(MetodoPagamento.BOLETO, 30, 60), 99);

        // Assert
        ArgumentCaptor<List<Comissao>> comissoesCaptor = ArgumentCaptor.forClass(List.class);
        verify(comissaoRepository).saveAll(comissoesCaptor.capture());
        assertThat(comissoesCaptor.getValue()).extracting(Comissao::getValorComissao)
                .allSatisfy(valor -> assertThat(valor).isEqualByComparingTo("3.00"));
    }

    @Test
    void validarPrazosPagamento_deveAceitarBoletoComAteDezPrazosCrescentesIncluindoZero() {
        pedidoService.validarPrazosPagamento(MetodoPagamento.BOLETO, List.of(0, 10, 30, 60, 90, 120, 150, 180, 360, 999));
    }

    @Test
    void validarPrazosPagamento_deveFalhar_quandoPrazosInvalidos() {
        assertThatThrownBy(() -> pedidoService.validarPrazosPagamento(MetodoPagamento.BOLETO, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("máximo");
        assertThatThrownBy(() -> pedidoService.validarPrazosPagamento(MetodoPagamento.BOLETO, List.of(30, 10)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("crescentes");
        assertThatThrownBy(() -> pedidoService.validarPrazosPagamento(MetodoPagamento.BOLETO, List.of(30, 30)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("crescentes");
        assertThatThrownBy(() -> pedidoService.validarPrazosPagamento(MetodoPagamento.BOLETO, List.of(1000)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("entre 0 e 999");
        assertThatThrownBy(() -> pedidoService.validarPrazosPagamento(MetodoPagamento.BOLETO, List.of(-1)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("entre 0 e 999");
        assertThatThrownBy(() -> pedidoService.validarPrazosPagamento(MetodoPagamento.BOLETO, List.of()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ao menos um");
        assertThatThrownBy(() -> pedidoService.validarPrazosPagamento(MetodoPagamento.AVISTA, List.of(10, 20)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("exatamente um");
        assertThatThrownBy(() -> pedidoService.validarPrazosPagamento(null, List.of(10)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("método de pagamento");
    }

    @Test
    void criarParcelasComComissao_deveFalhar_quandoNaoHaPrazos() {
        // Arrange
        Pagamento pagamento = new Pagamento();

        // Act / Assert
        assertThatThrownBy(() -> pedidoService.criarParcelasComComissao(pagamento, pedido(30, "EM_ANDAMENTO"), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("prazo");
    }

    @Test
    void editarPedido_deveTrocarMetodoEFinalizarComPrazosVariados() {
        // Arrange
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        pedido.setMetodoPagamento(MetodoPagamento.AVISTA);
        ItemPedido item = new ItemPedido();
        item.setQuantidade(1);
        item.setVlrUnitDistr(new BigDecimal("70.00"));
        item.setVlrUnitCliente(new BigDecimal("100.00"));
        item.setVlrTotalDistr(new BigDecimal("70.00"));
        item.setVlrTotalCliente(new BigDecimal("100.00"));

        PedidoEditRequest edicao = new PedidoEditRequest();
        edicao.setIdPedido(30);
        edicao.setNumeroNotaDistribuidor("NF-1");
        edicao.setObservacoes("Obs");
        edicao.setMetodoPagamento(MetodoPagamento.BOLETO);
        edicao.setDatasPagamento(List.of(10, 30, 60, 120, 180));
        edicao.setFinalizarPedido(true);

        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(itemPedidoRepository.findByPedido(pedido)).thenReturn(List.of(item));
        when(pagamentoRepository.existsByPedido_IdPedido(30)).thenReturn(false);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoService.convertToItemPedidoResumoResponse(any(ItemPedido.class))).thenReturn(new ItemPedidoResumoResponse());

        // Act
        PedidoResponse response = pedidoService.editarPedidoFromPedidoEditRequest(edicao, 99);

        // Assert
        assertThat(response.getStatusPedido()).isEqualTo("APROVADO");
        assertThat(response.getMetodoPagamento()).isEqualTo(MetodoPagamento.BOLETO);

        ArgumentCaptor<Pagamento> pagamentoCaptor = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentoRepository).save(pagamentoCaptor.capture());
        assertThat(pagamentoCaptor.getValue().getQuantidadeParcelas()).isEqualTo(5);
        assertThat(pagamentoCaptor.getValue().getParcelado()).isTrue();

        ArgumentCaptor<List<Parcela>> parcelasCaptor = ArgumentCaptor.forClass(List.class);
        verify(parcelaRepository).saveAll(parcelasCaptor.capture());
        assertThat(parcelasCaptor.getValue()).extracting(Parcela::getPrazoDias).containsExactly(10, 30, 60, 120, 180);

        // Editar o pedido não altera o cadastro do cliente nem do distribuidor
        org.mockito.Mockito.verifyNoInteractions(clienteService, distribuidorService);
    }

    @Test
    void editarPedido_deveFalhar_quandoFinalizaSemMetodoDePagamentoNoPedido() {
        // Arrange
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        ItemPedido item = new ItemPedido();
        item.setQuantidade(1);
        item.setVlrUnitDistr(new BigDecimal("70.00"));
        item.setVlrUnitCliente(new BigDecimal("100.00"));
        item.setVlrTotalDistr(new BigDecimal("70.00"));
        item.setVlrTotalCliente(new BigDecimal("100.00"));

        PedidoEditRequest edicao = new PedidoEditRequest();
        edicao.setIdPedido(30);
        edicao.setNumeroNotaDistribuidor("NF-1");
        edicao.setObservacoes("Obs");
        edicao.setDatasPagamento(List.of(10));
        edicao.setFinalizarPedido(true);

        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(itemPedidoRepository.findByPedido(pedido)).thenReturn(List.of(item));
        when(pagamentoRepository.existsByPedido_IdPedido(30)).thenReturn(false);

        // Act / Assert
        assertThatThrownBy(() -> pedidoService.editarPedidoFromPedidoEditRequest(edicao, 99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("método de pagamento");

        verify(pagamentoRepository, never()).save(any());
    }

    // ─── Entrega do pedido ──────────────────────────────────────────────────────

    @Test
    void cadastrarPedido_deveGravarEntregaSemEspacosNasPontas() {
        PedidoRequest request = pedidoRequest(List.of(itemRequest(1, 1, "10.00", "12.00")));
        request.setEntrega("  15 dias  ");
        when(usuarioRepository.findById(99)).thenReturn(Optional.of(usuario(99)));
        when(clienteService.buscarClientePorId(10)).thenReturn(cliente(10));
        when(distribuidorService.buscarDistribuidorPorId(20)).thenReturn(distribuidor(20));
        when(produtoRepository.findByIdProdutoIn(List.of(1))).thenReturn(List.of(produto(1)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoRepository.save(any(ItemPedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoService.convertToItemPedidoResumoResponse(any(ItemPedido.class))).thenReturn(new ItemPedidoResumoResponse());

        PedidoResponse response = pedidoService.cadastrarPedido(request, 99);

        assertThat(response.getEntrega()).isEqualTo("15 dias");
    }

    @Test
    void cadastrarPedido_deveFalhar_quandoEntregaPassaDe100Caracteres() {
        PedidoRequest request = pedidoRequest(List.of(itemRequest(1, 1, "10.00", "12.00")));
        request.setEntrega("x".repeat(101));
        when(usuarioRepository.findById(99)).thenReturn(Optional.of(usuario(99)));
        when(clienteService.buscarClientePorId(10)).thenReturn(cliente(10));
        when(distribuidorService.buscarDistribuidorPorId(20)).thenReturn(distribuidor(20));
        when(produtoRepository.findByIdProdutoIn(List.of(1))).thenReturn(List.of(produto(1)));

        assertThatThrownBy(() -> pedidoService.cadastrarPedido(request, 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessage("A entrega deve ter no máximo 100 caracteres.");
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void editarPedido_deveAtualizarLimparOuManterEntrega() {
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        pedido.setEntrega("Imediata");
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));

        PedidoEditRequest semEntrega = new PedidoEditRequest();
        semEntrega.setIdPedido(30);
        pedidoService.editarPedidoFromPedidoEditRequest(semEntrega, 99);
        assertThat(pedido.getEntrega()).isEqualTo("Imediata");

        PedidoEditRequest novaEntrega = new PedidoEditRequest();
        novaEntrega.setIdPedido(30);
        novaEntrega.setEntrega("30 dias");
        pedidoService.editarPedidoFromPedidoEditRequest(novaEntrega, 99);
        assertThat(pedido.getEntrega()).isEqualTo("30 dias");

        PedidoEditRequest entregaVazia = new PedidoEditRequest();
        entregaVazia.setIdPedido(30);
        entregaVazia.setEntrega("");
        pedidoService.editarPedidoFromPedidoEditRequest(entregaVazia, 99);
        assertThat(pedido.getEntrega()).isNull();
    }

    // ─── Quantidade de cada item na edição ──────────────────────────────────────

    @Test
    void editarPedido_deveAtualizarQuantidadeDeCadaItemERecalcularTotais() {
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        ItemPedido item1 = itemDoPedido(1, 1, "10.00", "15.00");
        ItemPedido item2 = itemDoPedido(2, 1, "20.00", "25.00");
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(itemPedidoRepository.findByPedido(pedido)).thenReturn(List.of(item1, item2));
        when(itemPedidoService.convertToItemPedidoResumoResponse(any(ItemPedido.class))).thenReturn(new ItemPedidoResumoResponse());

        PedidoEditRequest edicao = new PedidoEditRequest();
        edicao.setIdPedido(30);
        edicao.setItens(List.of(new PedidoEditRequest.ItemQuantidade(1, 3), new PedidoEditRequest.ItemQuantidade(2, 2)));

        pedidoService.editarPedidoFromPedidoEditRequest(edicao, 99);

        assertThat(item1.getQuantidade()).isEqualTo(3);
        assertThat(item1.getVlrTotalDistr()).isEqualByComparingTo("30.00");
        assertThat(item1.getVlrTotalCliente()).isEqualByComparingTo("45.00");
        assertThat(item2.getQuantidade()).isEqualTo(2);
        assertThat(item2.getVlrTotalDistr()).isEqualByComparingTo("40.00");
        assertThat(item2.getVlrTotalCliente()).isEqualByComparingTo("50.00");
        assertThat(pedido.getValorTotalDistr()).isEqualByComparingTo("70.00");
        assertThat(pedido.getValorTotalCliente()).isEqualByComparingTo("95.00");
        verify(itemPedidoRepository).save(item1);
        verify(itemPedidoRepository).save(item2);
    }

    @Test
    void editarPedido_deveFalharSemAlterarNada_quandoItemNaoEDoPedidoOuQuantidadeInvalida() {
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        ItemPedido item1 = itemDoPedido(1, 1, "10.00", "15.00");
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(itemPedidoRepository.findByPedido(pedido)).thenReturn(List.of(item1));

        PedidoEditRequest itemDeOutroPedido = new PedidoEditRequest();
        itemDeOutroPedido.setIdPedido(30);
        itemDeOutroPedido.setItens(List.of(new PedidoEditRequest.ItemQuantidade(1, 5), new PedidoEditRequest.ItemQuantidade(77, 2)));

        PedidoEditRequest quantidadeZero = new PedidoEditRequest();
        quantidadeZero.setIdPedido(30);
        quantidadeZero.setItens(List.of(new PedidoEditRequest.ItemQuantidade(1, 0)));

        assertThatThrownBy(() -> pedidoService.editarPedidoFromPedidoEditRequest(itemDeOutroPedido, 99))
                .isInstanceOf(RecursoNaoEncontrado.class)
                .hasMessageContaining("77");
        assertThatThrownBy(() -> pedidoService.editarPedidoFromPedidoEditRequest(quantidadeZero, 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("quantidade deve ser maior que zero");

        assertThat(item1.getQuantidade()).isEqualTo(1);
        verify(itemPedidoRepository, never()).save(any());
        verify(pedidoRepository, never()).save(any());
    }

    private ItemPedido itemDoPedido(Integer id, Integer quantidade, String unitDistr, String unitCliente) {
        ItemPedido item = new ItemPedido();
        item.setIdItemPedido(id);
        item.setProduto(produto(id));
        item.setQuantidade(quantidade);
        item.setVlrUnitDistr(new BigDecimal(unitDistr));
        item.setVlrUnitCliente(new BigDecimal(unitCliente));
        item.setVlrTotalDistr(new BigDecimal(unitDistr).multiply(BigDecimal.valueOf(quantidade)));
        item.setVlrTotalCliente(new BigDecimal(unitCliente).multiply(BigDecimal.valueOf(quantidade)));
        return item;
    }

    // ─── Dono do pedido ─────────────────────────────────────────────────────────

    @Test
    void criarComissao_deveResponderNaoEncontrado_quandoPedidoEDeOutroVendedor() {
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido(30, "EM_ANDAMENTO")));

        assertThatThrownBy(() -> pedidoService.criarComissao(30, pagamentoDTO(MetodoPagamento.AVISTA, 0), 7))
                .isInstanceOf(RecursoNaoEncontrado.class)
                .hasMessageContaining("30");

        verify(pagamentoRepository, never()).save(any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void editarPedido_deveResponderNaoEncontrado_quandoPedidoEDeOutroVendedor() {
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido(30, "EM_ANDAMENTO")));
        PedidoEditRequest edicao = new PedidoEditRequest();
        edicao.setIdPedido(30);

        assertThatThrownBy(() -> pedidoService.editarPedidoFromPedidoEditRequest(edicao, 7))
                .isInstanceOf(RecursoNaoEncontrado.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deletarPedido_deveInativarApenasPedidoDoProprioVendedor() {
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.deletarPedido(30, 7))
                .isInstanceOf(RecursoNaoEncontrado.class);
        assertThat(pedido.getAtivo()).isTrue();

        pedidoService.deletarPedido(30, 99);
        assertThat(pedido.getAtivo()).isFalse();
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void buscarPedidoPorId_deveLiberarQualquerPedidoSomenteComAcessoTotal() {
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(itemPedidoRepository.findByPedido(pedido)).thenReturn(List.of());

        assertThatThrownBy(() -> pedidoService.buscarPedidoPorId(30, 7, false))
                .isInstanceOf(RecursoNaoEncontrado.class);
        assertThat(pedidoService.buscarPedidoPorId(30, 7, true).getIdPedido()).isEqualTo(30);
        assertThat(pedidoService.buscarPedidoPorId(30, 99, false).getIdPedido()).isEqualTo(30);
    }

    @Test
    void listarPedidosPorVendedor_deveImpedirVendedorDeConsultarOutroVendedor() {
        assertThatThrownBy(() -> pedidoService.listarPedidosPorVendedor(99, org.springframework.data.domain.Pageable.unpaged(), 7, false))
                .isInstanceOf(RecursoNaoEncontrado.class);
        verify(pedidoRepository, never()).listarPedidosPorVendedor(any(), any());
    }

    @Test
    void listarPedidos_deveTrazerSoOsAtivosDoVendedorSemAcessoTotal() {
        when(pedidoRepository.findByUsuario_IdUsuarioAndAtivoTrue(99)).thenReturn(List.of());

        pedidoService.listarPedidos(99, false);

        verify(pedidoRepository).findByUsuario_IdUsuarioAndAtivoTrue(99);
        verify(pedidoRepository, never()).findByAtivoTrue();
        verify(pedidoRepository, never()).findAll();
    }

    @Test
    void criarComissao_deveFalharComMensagemClara_quandoVendedorNaoTemPercentual() {
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        pedido.getUsuario().setPercentualComissao(null);
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(pagamentoRepository.existsByPedido_IdPedido(30)).thenReturn(false);

        assertThatThrownBy(() -> pedidoService.criarComissao(30, pagamentoDTO(MetodoPagamento.AVISTA, 0), 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("percentual de comissão");

        verify(pagamentoRepository, never()).save(any());
        verify(parcelaRepository, never()).saveAll(any());
        assertThat(pedido.getStatusPedido()).isEqualTo("EM_ANDAMENTO");
    }

    // ─── Cadastros inativos e pedido excluído ───────────────────────────────────

    @Test
    void cadastrarPedido_deveFalhar_quandoClienteOuDistribuidorEstaInativo() {
        Cliente clienteInativo = cliente(10);
        clienteInativo.setAtivo(false);
        Distribuidor distribuidorInativo = distribuidor(20);
        distribuidorInativo.setAtivo(false);
        when(usuarioRepository.findById(99)).thenReturn(Optional.of(usuario(99)));
        when(clienteService.buscarClientePorId(10)).thenReturn(clienteInativo, cliente(10));
        when(distribuidorService.buscarDistribuidorPorId(20)).thenReturn(distribuidorInativo);

        assertThatThrownBy(() -> pedidoService.cadastrarPedido(pedidoRequest(List.of(itemRequest(1, 1, "10.00", "12.00"))), 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("cliente selecionado está inativo");
        assertThatThrownBy(() -> pedidoService.cadastrarPedido(pedidoRequest(List.of(itemRequest(1, 1, "10.00", "12.00"))), 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("distribuidor selecionado está inativo");
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void cadastrarPedido_deveFalhar_quandoProdutoEstaInativo() {
        Produto inativo = produto(2);
        inativo.setAtivo(false);
        when(usuarioRepository.findById(99)).thenReturn(Optional.of(usuario(99)));
        when(clienteService.buscarClientePorId(10)).thenReturn(cliente(10));
        when(distribuidorService.buscarDistribuidorPorId(20)).thenReturn(distribuidor(20));
        when(produtoRepository.findByIdProdutoIn(List.of(1, 2))).thenReturn(List.of(produto(1), inativo));

        PedidoRequest request = pedidoRequest(List.of(itemRequest(1, 1, "10.00", "12.00"), itemRequest(2, 1, "10.00", "12.00")));

        assertThatThrownBy(() -> pedidoService.cadastrarPedido(request, 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("Item 2")
                .hasMessageContaining("inativo");
        verify(pedidoRepository, never()).save(any());
        verify(itemPedidoRepository, never()).save(any());
    }

    @Test
    void pedidoExcluido_deveResponderNaoEncontradoMesmoParaODono() {
        Pedido excluido = pedido(30, "EM_ANDAMENTO");
        excluido.setAtivo(false);
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(excluido));

        assertThatThrownBy(() -> pedidoService.criarComissao(30, pagamentoDTO(MetodoPagamento.AVISTA, 0), 99))
                .isInstanceOf(RecursoNaoEncontrado.class);
        assertThatThrownBy(() -> pedidoService.deletarPedido(30, 99))
                .isInstanceOf(RecursoNaoEncontrado.class);
        assertThatThrownBy(() -> pedidoService.buscarPedidoPorId(30, 99, false))
                .isInstanceOf(RecursoNaoEncontrado.class);
        assertThatThrownBy(() -> pedidoService.buscarPedidoPorId(30, 7, true))
                .isInstanceOf(RecursoNaoEncontrado.class);
        verify(pagamentoRepository, never()).save(any());
        verify(pedidoRepository, never()).save(any());
    }

    private PedidoRequest pedidoRequest(List<ItemPedidoRequest> itens) {
        ClientePedidoResponseDTO clienteDTO = new ClientePedidoResponseDTO();
        clienteDTO.setId(10);

        DistribuidorPedidoResponseDTO distribuidorDTO = new DistribuidorPedidoResponseDTO();
        distribuidorDTO.setId(20);

        PedidoRequest request = new PedidoRequest();
        request.setCliente(clienteDTO);
        request.setDistribuidor(distribuidorDTO);
        request.setItens(itens);
        request.setMetodoPagamento(MetodoPagamento.BOLETO);
        return request;
    }

    private ItemPedidoRequest itemRequest(Integer produtoId, Integer quantidade, String valorDistr, String valorCliente) {
        ItemPedidoRequest item = new ItemPedidoRequest();
        item.setFkProduto(produtoId);
        item.setQuantidade(quantidade);
        item.setVlrUnitDistr(new BigDecimal(valorDistr));
        item.setVlrUnitCliente(new BigDecimal(valorCliente));
        item.setVlrTotalDistr(new BigDecimal(valorDistr).multiply(BigDecimal.valueOf(quantidade)));
        item.setVlrTotalCliente(new BigDecimal(valorCliente).multiply(BigDecimal.valueOf(quantidade)));
        return item;
    }

    private Pedido pedido(Integer id, String status) {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(id);
        pedido.setStatusPedido(status);
        pedido.setDataPedido(LocalDate.of(2026, 6, 10));
        pedido.setValorTotalDistr(new BigDecimal("70.00"));
        pedido.setValorTotalCliente(new BigDecimal("100.00"));
        pedido.setUsuario(usuario(99));
        pedido.setCliente(cliente(10));
        pedido.setDistribuidor(distribuidor(20));
        return pedido;
    }

    private PagamentoDTO pagamentoDTO(MetodoPagamento metodo, Integer... prazos) {
        PagamentoDTO pagamento = new PagamentoDTO();
        pagamento.setMetodoPagamento(metodo);
        pagamento.setDatasPagamento(List.of(prazos));
        return pagamento;
    }

    private Produto produto(Integer id) {
        Produto produto = new Produto();
        produto.setIdProduto(id);
        produto.setNome("Produto " + id);
        produto.setCodigoProduto("PN-" + id);
        return produto;
    }

    private Usuario usuario(Integer id) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNome("Vendedor");
        usuario.setPercentualComissao(new BigDecimal("10"));
        return usuario;
    }

    private Cliente cliente(Integer id) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(id);
        cliente.setRazaoSocial("Cliente Razao");
        cliente.setNomeFantasia("Cliente Fantasia");
        cliente.setCnpj("11222333000144");
        cliente.setInscricaoEstadual("IE-1");
        return cliente;
    }

    private Distribuidor distribuidor(Integer id) {
        Distribuidor distribuidor = new Distribuidor();
        distribuidor.setIdDistribuidor(id);
        distribuidor.setRazaoSocial("Distribuidor Razao");
        distribuidor.setNomeFantasia("Distribuidor Fantasia");
        distribuidor.setCnpj("55666777000188");
        distribuidor.setInscricaoEstadual("IE-2");
        return distribuidor;
    }
}
