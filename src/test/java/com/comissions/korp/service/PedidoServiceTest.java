package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteDTO.ClientePedidoResponseDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorPedidoResponseDTO;
import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoRequest;
import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoResumoResponse;
import com.comissions.korp.DTO.PagamentoDTO;
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
    private ComissaoService comissaoService;
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
                comissaoService,
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
        assertThat(response.getValorTotalDistr()).isEqualByComparingTo("20.00");
        assertThat(response.getValorTotalCliente()).isEqualByComparingTo("30.00");

        ArgumentCaptor<ItemPedido> itemCaptor = ArgumentCaptor.forClass(ItemPedido.class);
        verify(itemPedidoRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getPedido().getIdPedido()).isEqualTo(30);
        assertThat(itemCaptor.getValue().getProduto()).isSameAs(produto);
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
        PagamentoDTO pagamentoDTO = pagamentoDTO(3);

        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(pagamentoRepository.existsByPedido_IdPedido(30)).thenReturn(false);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(comissaoService.pegarComissaoPorUsuario()).thenReturn(new BigDecimal("10"));

        // Act
        String mensagem = pedidoService.criarComissao(30, pagamentoDTO);

        // Assert
        assertThat(mensagem).contains("30");
        assertThat(pedido.getStatusPedido()).isEqualTo("APROVADO");
        verify(pedidoRepository).save(pedido);

        ArgumentCaptor<List<Parcela>> parcelasCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Comissao>> comissoesCaptor = ArgumentCaptor.forClass(List.class);
        verify(parcelaRepository).saveAll(parcelasCaptor.capture());
        verify(comissaoRepository).saveAll(comissoesCaptor.capture());

        List<Parcela> parcelas = parcelasCaptor.getValue();
        List<Comissao> comissoes = comissoesCaptor.getValue();
        assertThat(parcelas).hasSize(3);
        assertThat(parcelas).extracting(Parcela::getNumeroParcela).containsExactly(1, 2, 3);
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
        assertThatThrownBy(() -> pedidoService.criarComissao(30, pagamentoDTO(2)))
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
        assertThatThrownBy(() -> pedidoService.criarComissao(30, pagamentoDTO(2)))
                .isInstanceOf(OperacaoNaoPermitida.class)
                .hasMessageContaining("pagamento");

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void criarParcelasComComissao_deveFalhar_quandoQuantidadeParcelasEhInvalida() {
        // Arrange
        Pagamento pagamento = new Pagamento();
        pagamento.setQuantidadeParcelas(0);

        // Act / Assert
        assertThatThrownBy(() -> pedidoService.criarParcelasComComissao(pagamento, pedido(30, "EM_ANDAMENTO")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maior que zero");
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

    private PagamentoDTO pagamentoDTO(Integer quantidadeParcelas) {
        PagamentoDTO pagamento = new PagamentoDTO();
        pagamento.setMetodoPagamento(MetodoPagamento.BOLETO);
        pagamento.setParcelado(true);
        pagamento.setQuantidadeParcelas(quantidadeParcelas);
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
