package com.comissions.korp.service;

import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoRequest;
import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoResumoResponse;
import com.comissions.korp.DTO.PagamentoDTO;
import com.comissions.korp.DTO.PedidoDTO.PedidoEditRequest;
import com.comissions.korp.DTO.PedidoDTO.PedidoRequest;
import com.comissions.korp.DTO.PedidoDTO.PedidoResponse;
import com.comissions.korp.entity.*;
import com.comissions.korp.entity.ENUM.MetodoPagamento;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ENUM.StatusParcela;
import com.comissions.korp.exception.OperacaoNaoPermitida;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.*;
import jdk.jshell.Snippet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteService clienteService;
    private final DistribuidorService distribuidorService;
    private final ItemPedidoService itemPedidoService;
    private final ComissaoService comissaoService;
    private final PagamentoRepository pagamentoRepository;
    private final ParcelaRepository parcelaRepository;
    private final ComissaoRepository comissaoRepository;
    private final ClienteRepository clienteRepository;
    private final DistribuidorRepository distribuidorRepository;


    public PedidoService(PedidoRepository pedidoRepository
            , ItemPedidoRepository itemPedidoRepository
            , ProdutoRepository produtoRepository
            , ClienteService clienteService
            , DistribuidorService distribuidorService,
                         ItemPedidoService itemPedidoService, UsuarioRepository usuarioRepository, ComissaoService comissaoService, PagamentoRepository pagamentoRepository, ParcelaRepository parcelaRepository, ComissaoRepository comissaoRepository, ClienteRepository clienteRepository, DistribuidorRepository distribuidorRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteService = clienteService;
        this.distribuidorService = distribuidorService;
        this.itemPedidoService = itemPedidoService;
        this.usuarioRepository = usuarioRepository;
        this.comissaoService = comissaoService;
        this.pagamentoRepository = pagamentoRepository;
        this.parcelaRepository = parcelaRepository;
        this.comissaoRepository = comissaoRepository;
        this.clienteRepository = clienteRepository;
        this.distribuidorRepository = distribuidorRepository;
    }


    @Transactional
    public PedidoResponse cadastrarPedido(PedidoRequest pedidoRequest, Integer vendedorId) {

        Usuario vendedor = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new RuntimeException("Vendedor não encontrado com id: " + vendedorId));

        Cliente cliente = clienteService.buscarClientePorId(pedidoRequest.getCliente().getId());
        Distribuidor distribuidor = distribuidorService.buscarDistribuidorPorId(pedidoRequest.getDistribuidor().getId());

        Pedido pedidoSalvo = pedidoRepository.save(criarPedidoFromRequest(pedidoRequest, cliente, distribuidor, vendedor));
        Map<Integer, Produto> produtoMap = mapearProdutosPorId(pedidoRequest.getItens());
        List<ItemPedido> itensSalvos = salvarItensDoPedido(pedidoSalvo, pedidoRequest.getItens(), produtoMap);

        return convertToPedidoResponse(pedidoSalvo, itensSalvos);
    }

    public List<PedidoResponse> listarPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        List<PedidoResponse> response = new ArrayList<>();

        for (Pedido pedido : pedidos) {
            List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedido);
            response.add(convertToPedidoResponse(pedido, itens));
        }

        return response;
    }

    public PedidoResponse buscarPedidoPorId(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado("Pedido não encontrado com id: " + id));

        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedido);
        return convertToPedidoResponse(pedido, itens);
    }


    @Transactional
    public PedidoResponse atualizarPedido(Integer id, PedidoRequest pedidoRequest, Integer vendedorId) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado("Pedido não encontrado com id: " + id));

        Usuario vendedor = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new RuntimeException("Vendedor não encontrado com id: " + vendedorId));

        Cliente cliente = clienteService.buscarClientePorId(pedidoRequest.getCliente().getId());
        Distribuidor distribuidor = distribuidorService.buscarDistribuidorPorId(pedidoRequest.getDistribuidor().getId());

        atualizarDadosPedido(pedido, pedidoRequest, cliente, distribuidor, vendedor);

        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

        if (pedidoRequest.getItens() != null) {
            atualizarItensDoPedido(pedidoAtualizado, pedidoRequest.getItens());
        }

        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedidoAtualizado);

        return convertToPedidoResponse(pedidoAtualizado, itens);
    }

    @Transactional
    public void deletarPedido(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado("Pedido não encontrado com id: " + id));

        pedido.setAtivo(false);

        pedidoRepository.save(pedido);
    }

    //Convertendo entidades em DTO

    private PedidoResponse convertToPedidoResponse(Pedido pedido, List<ItemPedido> itens) {
        PedidoResponse pedidoResponse = new PedidoResponse();
        pedidoResponse.setIdPedido(pedido.getIdPedido());
        pedidoResponse.setDataPedido(pedido.getDataPedido());
        pedidoResponse.setNumeroNotaDistribuidor(pedido.getNumeroNotaDistribuidor());
        pedidoResponse.setValorTotalDistr(pedido.getValorTotalRevenda());
        pedidoResponse.setValorTotalCliente(pedido.getValorTotalFaturamento());
        pedidoResponse.setStatusPedido(pedido.getStatusPedido());
        pedidoResponse.setFrete(pedido.getFrete());
        pedidoResponse.setTransportadora(pedido.getTransportadora());
        pedidoResponse.setObservacoes(pedido.getObservacoes());
        pedidoResponse.setFkVendedor(pedido.getUsuario() != null ? pedido.getUsuario().getIdUsuario() : null);
        pedidoResponse.setFkCliente(pedido.getCliente() != null ? pedido.getCliente().getIdCliente() : null);
        pedidoResponse.setFkDistribuidor(pedido.getDistribuidor() != null ? pedido.getDistribuidor().getIdDistribuidor() : null);

        List<ItemPedidoResumoResponse> itensResponse = itens
                .stream()
                .map(itemPedidoService::convertToItemPedidoResumoResponse)
                .collect(Collectors.toList());

        pedidoResponse.setItens(itensResponse);
        return pedidoResponse;
    }

    private void atualizarDadosPedido(Pedido pedido, PedidoRequest request, Cliente cliente, Distribuidor distribuidor, Usuario vendedor) {
        pedido.setDataPedido(LocalDate.now());
        pedido.setNumeroNotaDistribuidor(pedido.getNumeroNotaDistribuidor());
        pedido.setValorTotalRevenda(calcularValorTotalRevenda(request.getItens()));
        pedido.setValorTotalFaturamento(calcularValorTotalFaturamento(request.getItens()));
        pedido.setStatusPedido(pedido.getStatusPedido());
        pedido.setFrete(pedido.getFrete());
        pedido.setTransportadora(pedido.getTransportadora());
        pedido.setObservacoes(pedido.getObservacoes());
        pedido.setUsuario(vendedor);
        pedido.setCliente(cliente);
        pedido.setDistribuidor(distribuidor);
    }

    public BigDecimal calcularValorTotalRevenda(List<ItemPedidoRequest> itens) {
        return itens.stream()
                .map(item -> item.getVlrUnitDistr().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularValorTotalFaturamento(List<ItemPedidoRequest> itens) {
        return itens.stream()
                .map(item -> item.getVlrUnitCliente().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private Pedido criarPedidoFromRequest(PedidoRequest pedidoRequest, Cliente cliente, Distribuidor distribuidor, Usuario vendedor) {
        Pedido pedido = new Pedido();
        pedido.setStatusPedido("EM_ANDAMENTO");
        atualizarDadosPedido(pedido, pedidoRequest, cliente, distribuidor, vendedor);
        return pedido;
    }

    public Map<Integer, Produto> mapearProdutosPorId(List<ItemPedidoRequest> itensRequest) {
        List<Integer> idsProdutos = itensRequest.stream()
                .map(ItemPedidoRequest::getFkProduto)
                .collect(Collectors.toList());

        List<Produto> produtos = produtoRepository.findByIdProdutoIn(idsProdutos);

        return produtos.stream()
                .collect(Collectors.toMap(Produto::getIdProduto, produto -> produto));
    }

    public List<ItemPedido> salvarItensDoPedido(Pedido pedido, List<ItemPedidoRequest> itensRequest,
                                                Map<Integer, Produto> produtoMap) {
        List<ItemPedido> itensSalvos = new ArrayList<>();

        for (ItemPedidoRequest itemRequest : itensRequest) {
            Produto produto = buscarProdutoPorId(itemRequest.getFkProduto(), produtoMap);
            ItemPedido itemPedido = criarItemPedido(pedido, produto, itemRequest);
            itensSalvos.add(itemPedidoRepository.save(itemPedido));
        }

        return itensSalvos;
    }

    private void atualizarItensDoPedido(Pedido pedido, List<ItemPedidoRequest> itensRequest) {
        itemPedidoRepository.deleteByPedido(pedido);

        if (itensRequest.isEmpty()) {
            return;
        }

        Map<Integer, Produto> produtoMap = mapearProdutosPorId(itensRequest);
        salvarItensDoPedido(pedido, itensRequest, produtoMap);
    }

    private Produto buscarProdutoPorId(Integer idProduto, Map<Integer, Produto> produtoMap) {
        Produto produto = produtoMap.get(idProduto);
        if (produto == null) {
            throw new RecursoNaoEncontrado("Produto não encontrado com id: " + idProduto);
        }
        return produto;
    }

    private ItemPedido criarItemPedido(Pedido pedido, Produto produto, ItemPedidoRequest itemRequest) {
        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setPedido(pedido);
        itemPedido.setProduto(produto);
        itemPedido.setQuantidade(itemRequest.getQuantidade());
        itemPedido.setVlrUnitDistr(itemRequest.getVlrUnitDistr());
        itemPedido.setVlrTotalDistr(itemRequest.getVlrTotalDistr());
        itemPedido.setVlrUnitCliente(itemRequest.getVlrUnitCliente());
        itemPedido.setVlrTotalCliente(itemRequest.getVlrTotalCliente());
        return itemPedido;
    }

    @Transactional
    public String criarComissao(Integer idPedido, PagamentoDTO pagamento) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RecursoNaoEncontrado("Pedido não encontrado com id: " + idPedido));

        if (!"EM_ANDAMENTO".equals(pedido.getStatusPedido())) {
            throw new OperacaoNaoPermitida("Não é possível criar comissão para um pedido que não está em andamento.");
        }
        if (pagamentoRepository.existsByPedido_IdPedido(idPedido)) {
            throw new OperacaoNaoPermitida("Pedido já possui pagamento e comissões cadastrados.");
        }

        Pagamento pagamentoPedido = criarPagamentoFromPagamentoDTOAndPedido(pagamento, pedido);
        pagamentoRepository.save(pagamentoPedido);

        criarParcelasComComissao(pagamentoPedido, pedido);

        pedido.setStatusPedido("APROVADO");
        pedidoRepository.save(pedido);

        return "Comissão criada com sucesso para o pedido ID: " + idPedido;
    }
    public Pagamento criarPagamentoFromPagamentoDTOAndPedido(PagamentoDTO pagamento, Pedido pedido) {
        Pagamento pagamentoPedido = new Pagamento();

        MetodoPagamento metodo = getMetodoPagamentoFromPagamentoDTO(pagamento);

        pagamentoPedido.setPedido(pedido);
        pagamentoPedido.setParcelado(pagamento.getParcelado());
        pagamentoPedido.setMetodoPagamento(metodo);
        pagamentoPedido.setQuantidadeParcelas(pagamento.getQuantidadeParcelas());
        return pagamentoPedido;
    }

    public MetodoPagamento getMetodoPagamentoFromPagamentoDTO(PagamentoDTO pagamento) {
        return pagamento.getMetodoPagamento();
    }

    public List<Parcela> criarParcelasComComissao(Pagamento pagamento, Pedido pedido) {
        Integer qtdParcelas = pagamento.getQuantidadeParcelas();
        if (qtdParcelas == null || qtdParcelas <= 0) {
            throw new IllegalArgumentException(
                    "Quantidade de parcelas inválida: deve ser um número inteiro maior que zero."
            );
        }

        BigDecimal percentualComissao = comissaoService.pegarComissaoPorUsuario().divide(BigDecimal.valueOf(100));
        BigDecimal bdQtd = BigDecimal.valueOf(qtdParcelas); // 5
        BigDecimal valorTotal = pedido.getValorTotalFaturamento(); // 30000
        BigDecimal valorComissaoBruto = valorTotal.subtract(pedido.getValorTotalDistr());

        List<Parcela> parcelas = new ArrayList<>();
        List<Comissao> comissoes = new ArrayList<>();

        BigDecimal valorParcela = valorTotal // 6000
                .divide(bdQtd, 2, RoundingMode.HALF_UP);

        BigDecimal valorTotalComissao = valorComissaoBruto // 9000
                .multiply(percentualComissao);

        BigDecimal valorComissaoPorParcela = valorTotalComissao
                .divide(bdQtd, 2, RoundingMode.HALF_UP);

        BigDecimal somaParcelasBase = valorParcela.multiply(bdQtd);
        BigDecimal remainderParcela = valorTotal.subtract(somaParcelasBase);

        BigDecimal somaComissaoBase = valorComissaoPorParcela.multiply(bdQtd);
        BigDecimal remainderComissao = valorTotalComissao
                .setScale(2, RoundingMode.HALF_UP)
                .subtract(somaComissaoBase);

        for (int i = 1; i <= qtdParcelas; i++) {
            boolean isUltimaParcela = (i == qtdParcelas);

            BigDecimal valorParcelaFinal = isUltimaParcela
                    ? valorParcela.add(remainderParcela)
                    : valorParcela;

            BigDecimal valorComissaoFinal = isUltimaParcela
                    ? valorComissaoPorParcela.add(remainderComissao)
                    : valorComissaoPorParcela;

            Parcela parcela = new Parcela();
            parcela.setPagamento(pagamento);
            parcela.setStatusParcela(StatusParcela.PENDENTE);
            parcela.setNumeroParcela(i);
            parcela.setDataPagamento(null);
            parcela.setValorParcela(valorParcelaFinal);
            parcela.setDataVencimento(LocalDate.now().plusMonths(i));

            Comissao comissao = new Comissao();
            comissao.setPedido(pedido);
            comissao.setValorComissao(valorComissaoFinal);
            comissao.setStatusComissao(StatusComissao.PENDENTE);
            comissao.setUsuario(pedido.getUsuario());
            comissao.setDataPagamento(null);
            comissao.setParcela(parcela);
            comissoes.add(comissao);

            parcelas.add(parcela);
        }

        parcelaRepository.saveAll(parcelas);

        comissaoRepository.saveAll(comissoes);

        return parcelas;
    }

    @Transactional
    public PedidoResponse editarPedidoFromPedidoEditRequest(PedidoEditRequest pedido) {
        Pedido pedidoAtual = pedidoRepository.findById(pedido.getIdPedido())
                .orElseThrow(() -> new RecursoNaoEncontrado("Pedido não encontrado com id: " + pedido.getIdPedido()));

        if (!"EM_ANDAMENTO".equals(pedidoAtual.getStatusPedido())) {
            throw new OperacaoNaoPermitida("Pedido aprovado não pode ser editado.");
        }

        Cliente cliente = clienteService.buscarClientePorId(pedido.getIdCliente());
        cliente.setNomeFantasia(pedido.getNomeFantasiaCliente());
        clienteRepository.save(cliente);

        Distribuidor distribuidor = distribuidorService.buscarDistribuidorPorId(pedido.getIdDistribuidor());
        distribuidor.setNomeFantasia(pedido.getNomeFantasiaDistribuidor());
        distribuidorRepository.save(distribuidor);

        pedidoAtual.setNumeroNotaDistribuidor(pedido.getNumeroNotaDistribuidor());
        pedidoAtual.setObservacoes(pedido.getObservacoes());

        if (pedido.getQuantidade() != null) {
            atualizarQuantidadeItemPedido(pedidoAtual, pedido);
        }

        recalcularTotaisPedido(pedidoAtual);

        if (Boolean.TRUE.equals(pedido.getFinalizarPedido())) {
            validarPedidoParaFinalizacao(pedido);
            PagamentoDTO pagamentoDTO = new PagamentoDTO();
            pagamentoDTO.setMetodoPagamento(pedido.getMetodoPagamento());
            pagamentoDTO.setParcelado(Boolean.TRUE.equals(pedido.getParcelado()));
            pagamentoDTO.setQuantidadeParcelas(pedido.getQuantidadeParcelas());

            Pagamento pagamentoPedido = criarPagamentoFromPagamentoDTOAndPedido(pagamentoDTO, pedidoAtual);
            pagamentoRepository.save(pagamentoPedido);
            criarParcelasComComissao(pagamentoPedido, pedidoAtual);
            pedidoAtual.setStatusPedido("APROVADO");
        }

        pedidoRepository.save(pedidoAtual);

        return convertToPedidoResponse(pedidoAtual, itemPedidoRepository.findByPedido(pedidoAtual));
    }

    private void atualizarQuantidadeItemPedido(Pedido pedidoAtual, PedidoEditRequest pedido) {
        if (pedido.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }

        ItemPedido item = buscarItemEditavel(pedidoAtual, pedido.getIdItemPedido());
        item.setQuantidade(pedido.getQuantidade());
        item.setVlrTotalDistr(item.getVlrUnitDistr().multiply(BigDecimal.valueOf(pedido.getQuantidade())));
        item.setVlrTotalCliente(item.getVlrUnitCliente().multiply(BigDecimal.valueOf(pedido.getQuantidade())));
        itemPedidoRepository.save(item);
    }

    private ItemPedido buscarItemEditavel(Pedido pedidoAtual, Integer idItemPedido) {
        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedidoAtual);
        if (itens.isEmpty()) {
            throw new RecursoNaoEncontrado("Pedido não possui itens para edição.");
        }

        if (idItemPedido == null) {
            return itens.get(0);
        }

        return itens.stream()
                .filter(item -> idItemPedido.equals(item.getIdItemPedido()))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontrado("Item do pedido não encontrado com id: " + idItemPedido));
    }

    private void recalcularTotaisPedido(Pedido pedidoAtual) {
        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedidoAtual);
        BigDecimal totalDistr = itens.stream()
                .map(ItemPedido::getVlrTotalDistr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCliente = itens.stream()
                .map(ItemPedido::getVlrTotalCliente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedidoAtual.setValorTotalDistr(totalDistr);
        pedidoAtual.setValorTotalCliente(totalCliente);
    }

    private void validarPedidoParaFinalizacao(PedidoEditRequest pedido) {
        if (pagamentoRepository.existsByPedido_IdPedido(pedido.getIdPedido())) {
            throw new OperacaoNaoPermitida("Pedido já foi finalizado.");
        }
        if (pedido.getNumeroNotaDistribuidor() == null || pedido.getNumeroNotaDistribuidor().isBlank()) {
            throw new IllegalArgumentException("Informe o número da nota para finalizar o pedido.");
        }
        if (pedido.getObservacoes() == null || pedido.getObservacoes().isBlank()) {
            throw new IllegalArgumentException("Informe a observação para finalizar o pedido.");
        }
        if (pedido.getMetodoPagamento() == null) {
            throw new IllegalArgumentException("Informe o método de pagamento para finalizar o pedido.");
        }
        if (pedido.getQuantidadeParcelas() == null || pedido.getQuantidadeParcelas() <= 0) {
            throw new IllegalArgumentException("Informe uma quantidade de parcelas maior que zero.");
        }
    }
}
