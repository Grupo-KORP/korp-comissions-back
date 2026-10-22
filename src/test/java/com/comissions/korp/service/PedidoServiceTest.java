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
import com.comissions.korp.repository.ClienteRepository;
import com.comissions.korp.repository.DistribuidorRepository;
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
    private ClienteRepository clienteRepository;
    @Mock
    private DistribuidorRepository distribuidorRepository;

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
                clienteRepository,
                distribuidorRepository
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
        String mensagem = pedidoService.criarComissao(30, pagamentoDTO);

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
        assertThatThrownBy(() -> pedidoService.criarComissao(30, pagamentoDTO(MetodoPagamento.BOLETO, 30, 60)))
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
        assertThatThrownBy(() -> pedidoService.criarComissao(30, pagamentoDTO(MetodoPagamento.BOLETO, 30, 60)))
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
        pedidoService.criarComissao(30, pagamentoDTO(MetodoPagamento.AVISTA, 0));

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
        pedidoService.criarComissao(30, pagamentoDTO(MetodoPagamento.BOLETO, 30, 60));

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
        item.setVlrTotalDistr(new BigDecimal("70.00"));
        item.setVlrTotalCliente(new BigDecimal("100.00"));

        PedidoEditRequest edicao = new PedidoEditRequest();
        edicao.setIdPedido(30);
        edicao.setIdCliente(10);
        edicao.setIdDistribuidor(20);
        edicao.setNumeroNotaDistribuidor("NF-1");
        edicao.setObservacoes("Obs");
        edicao.setMetodoPagamento(MetodoPagamento.BOLETO);
        edicao.setDatasPagamento(List.of(10, 30, 60, 120, 180));
        edicao.setFinalizarPedido(true);

        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(clienteService.buscarClientePorId(10)).thenReturn(cliente(10));
        when(distribuidorService.buscarDistribuidorPorId(20)).thenReturn(distribuidor(20));
        when(itemPedidoRepository.findByPedido(pedido)).thenReturn(List.of(item));
        when(pagamentoRepository.existsByPedido_IdPedido(30)).thenReturn(false);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoService.convertToItemPedidoResumoResponse(any(ItemPedido.class))).thenReturn(new ItemPedidoResumoResponse());

        // Act
        PedidoResponse response = pedidoService.editarPedidoFromPedidoEditRequest(edicao);

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
    }

    @Test
    void editarPedido_deveFalhar_quandoFinalizaSemMetodoDePagamentoNoPedido() {
        // Arrange
        Pedido pedido = pedido(30, "EM_ANDAMENTO");
        ItemPedido item = new ItemPedido();
        item.setVlrTotalDistr(new BigDecimal("70.00"));
        item.setVlrTotalCliente(new BigDecimal("100.00"));

        PedidoEditRequest edicao = new PedidoEditRequest();
        edicao.setIdPedido(30);
        edicao.setIdCliente(10);
        edicao.setIdDistribuidor(20);
        edicao.setNumeroNotaDistribuidor("NF-1");
        edicao.setObservacoes("Obs");
        edicao.setDatasPagamento(List.of(10));
        edicao.setFinalizarPedido(true);

        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(clienteService.buscarClientePorId(10)).thenReturn(cliente(10));
        when(distribuidorService.buscarDistribuidorPorId(20)).thenReturn(distribuidor(20));
        when(itemPedidoRepository.findByPedido(pedido)).thenReturn(List.of(item));
        when(pagamentoRepository.existsByPedido_IdPedido(30)).thenReturn(false);

        // Act / Assert
        assertThatThrownBy(() -> pedidoService.editarPedidoFromPedidoEditRequest(edicao))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("método de pagamento");

        verify(pagamentoRepository, never()).save(any());
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
