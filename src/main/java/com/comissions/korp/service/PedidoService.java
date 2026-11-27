package com.comissions.korp.service;

import com.comissions.korp.config.PainelVendedorCache;
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
import com.comissions.korp.DTO.HomeFinanceiroDTO.ComissaoPedidoDTO;
import com.comissions.korp.exception.OperacaoNaoPermitida;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.RequisicaoInvalida;
import com.comissions.korp.repository.*;
import com.comissions.korp.service.Email.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private static final int MAX_PARCELAS_BOLETO = 10;
    private static final int PRAZO_MAXIMO_DIAS = 999;

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteService clienteService;
    private final DistribuidorService distribuidorService;
    private final ItemPedidoService itemPedidoService;
    private final EmailService emailService;
    private final PagamentoRepository pagamentoRepository;
    private final ParcelaRepository parcelaRepository;
    private final ComissaoRepository comissaoRepository;
    private final PedidoRascunhoService rascunhoService;
    private final PainelVendedorCache painelVendedorCache;


    public PedidoService(PedidoRepository pedidoRepository
            , ItemPedidoRepository itemPedidoRepository
            , ProdutoRepository produtoRepository
            , ClienteService clienteService
            , DistribuidorService distribuidorService,
                         ItemPedidoService itemPedidoService, UsuarioRepository usuarioRepository, EmailService emailService, PagamentoRepository pagamentoRepository, ParcelaRepository parcelaRepository, ComissaoRepository comissaoRepository, PedidoRascunhoService rascunhoService, PainelVendedorCache painelVendedorCache) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteService = clienteService;
        this.distribuidorService = distribuidorService;
        this.itemPedidoService = itemPedidoService;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.pagamentoRepository = pagamentoRepository;
        this.parcelaRepository = parcelaRepository;
        this.comissaoRepository = comissaoRepository;
        this.rascunhoService = rascunhoService;
        this.painelVendedorCache = painelVendedorCache;
    }

    @Transactional
    public PedidoResponse cadastrarPedido(PedidoRequest pedidoRequest, Integer vendedorId) {
// Painel do vendedor muda com esta escrita: apaga o cache depois do commit
painelVendedorCache.invalidarAposCommit();


        if (pedidoRequest.getMetodoPagamento() == null) {
            throw new IllegalArgumentException("Informe o método de pagamento do pedido.");
        }
        if (pedidoRequest.getCliente() == null || pedidoRequest.getCliente().getId() == null) {
            throw new RequisicaoInvalida("Informe o cliente do pedido.");
        }
        if (pedidoRequest.getDistribuidor() == null || pedidoRequest.getDistribuidor().getId() == null) {
            throw new RequisicaoInvalida("Informe o distribuidor do pedido.");
        }

        validarItensDoPedido(pedidoRequest.getItens());

        Usuario vendedor = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new RuntimeException("Vendedor não encontrado com id: " + vendedorId));

        Cliente cliente = clienteService.buscarClientePorId(pedidoRequest.getCliente().getId());
        if (!Boolean.TRUE.equals(cliente.getAtivo())) {
            throw new RequisicaoInvalida("O cliente selecionado está inativo e não pode receber pedidos.");
        }
        Distribuidor distribuidor = distribuidorService.buscarDistribuidorPorId(pedidoRequest.getDistribuidor().getId());
        if (!Boolean.TRUE.equals(distribuidor.getAtivo())) {
            throw new RequisicaoInvalida("O distribuidor selecionado está inativo e não pode receber pedidos.");
        }
        Map<Integer, Produto> produtoMap = mapearProdutosPorId(pedidoRequest.getItens());
        validarProdutosDosItens(pedidoRequest.getItens(), produtoMap);

        Pedido pedidoSalvo = pedidoRepository.save(criarPedidoFromRequest(pedidoRequest, cliente, distribuidor, vendedor));
        List<ItemPedido> itensSalvos = salvarItensDoPedido(pedidoSalvo, pedidoRequest.getItens(), produtoMap);

        rascunhoService.excluirAposCriarPedido(vendedorId, pedidoRequest.getIdRascunho());

        if ( pedidoRequest.getPdfBase64() != null
                && pedidoRequest.getDistribuidor().getEmail() != null) {

            byte[] pdfBytes = Base64.getDecoder().decode(pedidoRequest.getPdfBase64());

            emailService.enviarPedidoDistribuidor(
                    pedidoRequest.getDistribuidor().getEmail(),
                    pedidoRequest.getDistribuidor().getNomeFantasia(),
                    pedidoSalvo.getCodigo(),
                    pedidoRequest.getCliente().getRazaoSocial(),
                    pdfBytes
            );
        }

        return convertToPedidoResponse(pedidoSalvo, itensSalvos);
    }

    /** Financeiro/admin (acessoTotal) vê todos os pedidos; o vendedor vê só os próprios. */
    public List<PedidoResponse> listarPedidos(Integer idUsuarioLogado, boolean acessoTotal) {
        List<Pedido> pedidos = acessoTotal
                ? pedidoRepository.findByAtivoTrue()
                : pedidoRepository.findByUsuario_IdUsuarioAndAtivoTrue(idUsuarioLogado);
        List<PedidoResponse> response = new ArrayList<>();

        for (Pedido pedido : pedidos) {
            List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedido);
            response.add(convertToPedidoResponse(pedido, itens));
        }

        return response;
    }

    /** Financeiro/admin (acessoTotal) consulta qualquer vendedor; o vendedor só consulta o próprio histórico. */
    @Transactional(readOnly = true)
    public Page<PedidoResponse> listarPedidosPorVendedor(Integer idVendedor, Pageable pageable,
                                                         Integer idUsuarioLogado, boolean acessoTotal) {
        if (!acessoTotal && !idVendedor.equals(idUsuarioLogado)) {
            throw new RecursoNaoEncontrado("Vendedor não encontrado com id: " + idVendedor);
        }
        Page<Pedido> pedidos = pedidoRepository.listarPedidosPorVendedor(idVendedor, pageable);
        List<Integer> idsPedido = pedidos.getContent().stream().map(Pedido::getIdPedido).toList();
        Map<Integer, BigDecimal> comissoesPorPedido = idsPedido.isEmpty()
            ? Map.of()
            : pedidoRepository.somarComissoesPorPedido(idsPedido, StatusComissao.CANCELADA).stream()
            .collect(Collectors.toMap(ComissaoPedidoDTO::idPedido, ComissaoPedidoDTO::total));

        return pedidos.map(pedido -> {
            PedidoResponse response = convertToPedidoResponse(pedido, itemPedidoRepository.findByPedido(pedido));
            response.setCliente(pedido.getCliente().getNomeFantasia() != null
                ? pedido.getCliente().getNomeFantasia()
                : pedido.getCliente().getRazaoSocial());
            response.setValorFaturado(pedido.getValorTotalFaturamento());
            response.setComissao(comissoesPorPedido.getOrDefault(pedido.getIdPedido(), BigDecimal.ZERO));
            response.setPagamento(pedido.getMetodoPagamento() != null
                ? pedido.getMetodoPagamento().name()
                : null);
            response.setStatus(pedido.getStatusPedido());
            return response;
        });
    }

    /** Financeiro/admin (acessoTotal) busca qualquer pedido; o vendedor só os próprios. */
    public PedidoResponse buscarPedidoPorId(Integer id, Integer idUsuarioLogado, boolean acessoTotal) {
        Pedido pedido = acessoTotal
                ? pedidoRepository.findById(id)
                        .filter(encontrado -> Boolean.TRUE.equals(encontrado.getAtivo()))
                        .orElseThrow(() -> new RecursoNaoEncontrado("Pedido não encontrado com id: " + id))
                : buscarPedidoDoVendedor(id, idUsuarioLogado);

        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedido);
        return convertToPedidoResponse(pedido, itens);
    }

    /**
     * Busca o pedido conferindo se ele pertence ao vendedor e não foi excluído. Pedido de outro
     * vendedor ou excluído responde igual a pedido inexistente (404), para não revelar que ele existe.
     */
    private Pedido buscarPedidoDoVendedor(Integer idPedido, Integer idVendedor) {
        return pedidoRepository.findById(idPedido)
                .filter(pedido -> Boolean.TRUE.equals(pedido.getAtivo()))
                .filter(pedido -> pedido.getUsuario() != null
                        && pedido.getUsuario().getIdUsuario().equals(idVendedor))
                .orElseThrow(() -> new RecursoNaoEncontrado("Pedido não encontrado com id: " + idPedido));
    }

    @Transactional
    public void deletarPedido(Integer id, Integer idVendedor) {
        // Painel do vendedor muda com esta escrita: apaga o cache depois do commit
        painelVendedorCache.invalidarAposCommit();

        Pedido pedido = buscarPedidoDoVendedor(id, idVendedor);

        pedido.setAtivo(false);

        pedidoRepository.save(pedido);
    }

    //Convertendo entidades em DTO

    private PedidoResponse convertToPedidoResponse(Pedido pedido, List<ItemPedido> itens) {
        PedidoResponse pedidoResponse = new PedidoResponse();
        pedidoResponse.setIdPedido(pedido.getIdPedido());
        pedidoResponse.setCodigo(pedido.getCodigo());
        pedidoResponse.setEntrega(pedido.getEntrega());
        pedidoResponse.setDataPedido(pedido.getDataPedido());
        pedidoResponse.setNumeroNotaDistribuidor(pedido.getNumeroNotaDistribuidor());
        pedidoResponse.setValorTotalDistr(pedido.getValorTotalRevenda());
        pedidoResponse.setValorTotalCliente(pedido.getValorTotalFaturamento());
        pedidoResponse.setStatusPedido(pedido.getStatusPedido());
        pedidoResponse.setFrete(pedido.getFrete());
        pedidoResponse.setTransportadora(pedido.getTransportadora());
        pedidoResponse.setObservacoes(pedido.getObservacoes());
        pedidoResponse.setMetodoPagamento(pedido.getMetodoPagamento());
        pedidoResponse.setFkVendedor(pedido.getUsuario() != null ? pedido.getUsuario().getIdUsuario(): null);
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
        pedido.setMetodoPagamento(pedidoRequest.getMetodoPagamento());
        pedido.setEntrega(normalizarEntrega(pedidoRequest.getEntrega()));
        atualizarDadosPedido(pedido, pedidoRequest, cliente, distribuidor, vendedor);
        // Próximo número do dia para o código (ex.: 251126-P03). A constraint uc_pedido_data_numero
        // impede repetição se dois pedidos forem criados ao mesmo tempo.
        pedido.setNumeroDia(pedidoRepository.maiorNumeroDoDia(pedido.getDataPedido()) + 1);
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

    private static final int TAMANHO_MAXIMO_ENTREGA = 100;

    /** Entrega é opcional: sem espaços nas pontas, texto vazio vira "sem entrega" e o limite é 100 caracteres. */
    private String normalizarEntrega(String entrega) {
        if (entrega == null || entrega.isBlank()) {
            return null;
        }
        String texto = entrega.trim();
        if (texto.length() > TAMANHO_MAXIMO_ENTREGA) {
            throw new RequisicaoInvalida("A entrega deve ter no máximo " + TAMANHO_MAXIMO_ENTREGA + " caracteres.");
        }
        return texto;
    }

    /** Confere, antes de gravar o pedido, que todo produto dos itens existe e está ativo. */
    private void validarProdutosDosItens(List<ItemPedidoRequest> itens, Map<Integer, Produto> produtoMap) {
        for (int i = 0; i < itens.size(); i++) {
            Produto produto = buscarProdutoPorId(itens.get(i).getFkProduto(), produtoMap);
            if (!Boolean.TRUE.equals(produto.getAtivo())) {
                throw new RequisicaoInvalida("Item " + (i + 1) + ": o produto " + produto.getNome()
                        + " está inativo e não pode ser usado no pedido.");
            }
        }
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
        itemPedido.setVlrUnitCliente(itemRequest.getVlrUnitCliente());
        // Totais do item calculados aqui (quantidade × unitário), com a mesma regra do total do pedido.
        // Os totais enviados no request são ignorados para os dois nunca divergirem.
        BigDecimal quantidade = BigDecimal.valueOf(itemRequest.getQuantidade());
        itemPedido.setVlrTotalDistr(itemRequest.getVlrUnitDistr().multiply(quantidade));
        itemPedido.setVlrTotalCliente(itemRequest.getVlrUnitCliente().multiply(quantidade));
        return itemPedido;
    }

    @Transactional
    public String criarComissao(Integer idPedido, PagamentoDTO pagamento, Integer idVendedor) {
        // Painel do vendedor muda com esta escrita: apaga o cache depois do commit
        painelVendedorCache.invalidarAposCommit();

        Pedido pedido = buscarPedidoDoVendedor(idPedido, idVendedor);

        if (!"EM_ANDAMENTO".equals(pedido.getStatusPedido())) {
            throw new OperacaoNaoPermitida("Não é possível criar comissão para um pedido que não está em andamento.");
        }
        if (pagamentoRepository.existsByPedido_IdPedido(idPedido)) {
            throw new OperacaoNaoPermitida("Pedido já possui pagamento e comissões cadastrados.");
        }

        Pagamento pagamentoPedido = criarPagamentoFromPagamentoDTOAndPedido(pagamento, pedido);
        pagamentoRepository.save(pagamentoPedido);

        criarParcelasComComissao(pagamentoPedido, pedido, pagamento.getDatasPagamento());

        pedido.setMetodoPagamento(pagamento.getMetodoPagamento());
        pedido.setStatusPedido("APROVADO");
        pedidoRepository.save(pedido);

        return "Comissão criada com sucesso para o pedido ID: " + idPedido;
    }

    public Pagamento criarPagamentoFromPagamentoDTOAndPedido(PagamentoDTO pagamento, Pedido pedido) {
        validarPercentualComissaoDoVendedor(pedido);
        MetodoPagamento metodo = getMetodoPagamentoFromPagamentoDTO(pagamento);
        validarPrazosPagamento(metodo, pagamento.getDatasPagamento());

        int quantidadeParcelas = pagamento.getDatasPagamento().size();

        Pagamento pagamentoPedido = new Pagamento();
        pagamentoPedido.setPedido(pedido);
        pagamentoPedido.setParcelado(quantidadeParcelas > 1);
        pagamentoPedido.setMetodoPagamento(metodo);
        pagamentoPedido.setQuantidadeParcelas(quantidadeParcelas);
        return pagamentoPedido;
    }

    /**
     * A comissão é calculada com o percentual do vendedor no momento da finalização; sem ele não há
     * como gerar as comissões. Roda antes de gravar o pagamento, nos dois fluxos de finalização.
     */
    private void validarPercentualComissaoDoVendedor(Pedido pedido) {
        if (pedido.getUsuario() == null || pedido.getUsuario().getPercentualComissao() == null) {
            throw new RequisicaoInvalida("O vendedor deste pedido não tem percentual de comissão cadastrado. "
                    + "Peça ao financeiro para cadastrá-lo antes de finalizar o pedido.");
        }
    }

    public MetodoPagamento getMetodoPagamentoFromPagamentoDTO(PagamentoDTO pagamento) {
        return pagamento.getMetodoPagamento();
    }

    /** À vista tem 1 prazo; boleto de 1 a 10 prazos. Em ambos: 0 a 999 dias, estritamente crescentes. */
    public void validarPrazosPagamento(MetodoPagamento metodo, List<Integer> prazos) {
        if (metodo == null) {
            throw new IllegalArgumentException("Informe o método de pagamento.");
        }
        if (prazos == null || prazos.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um prazo de pagamento.");
        }
        if (metodo == MetodoPagamento.AVISTA && prazos.size() != 1) {
            throw new IllegalArgumentException("Pagamento à vista deve ter exatamente um prazo.");
        }
        if (metodo == MetodoPagamento.BOLETO && prazos.size() > MAX_PARCELAS_BOLETO) {
            throw new IllegalArgumentException("Boleto aceita no máximo " + MAX_PARCELAS_BOLETO + " parcelas.");
        }

        Integer anterior = null;
        for (Integer prazo : prazos) {
            if (prazo == null || prazo < 0 || prazo > PRAZO_MAXIMO_DIAS) {
                throw new IllegalArgumentException("Cada prazo de pagamento deve estar entre 0 e " + PRAZO_MAXIMO_DIAS + " dias.");
            }
            if (anterior != null && prazo <= anterior) {
                throw new IllegalArgumentException("Os prazos de pagamento devem ser crescentes e sem repetição.");
            }
            anterior = prazo;
        }
    }

    public List<Parcela> criarParcelasComComissao(Pagamento pagamento, Pedido pedido, List<Integer> prazosDias) {
        if (prazosDias == null || prazosDias.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um prazo de pagamento.");
        }
        int qtdParcelas = prazosDias.size();

        BigDecimal percentualComissao = pedido.getUsuario().getPercentualComissao().divide(BigDecimal.valueOf(100));
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

        LocalDate dataFinalizacao = LocalDate.now();

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
            parcela.setDataVencimento(dataFinalizacao.plusDays(prazosDias.get(i - 1)));
            parcela.setPrazoDias(prazosDias.get(i - 1));

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
    public PedidoResponse editarPedidoFromPedidoEditRequest(PedidoEditRequest pedido, Integer idVendedor) {
        // Painel do vendedor muda com esta escrita: apaga o cache depois do commit
        painelVendedorCache.invalidarAposCommit();

        Pedido pedidoAtual = buscarPedidoDoVendedor(pedido.getIdPedido(), idVendedor);

        if (!"EM_ANDAMENTO".equals(pedidoAtual.getStatusPedido())) {
            throw new OperacaoNaoPermitida("Pedido aprovado não pode ser editado.");
        }

        // Cliente e distribuidor não são alterados por aqui: o pedido mostra sempre os dados atuais
        // do cadastro, que se edita nas telas de Clientes e Distribuidores.
        pedidoAtual.setNumeroNotaDistribuidor(pedido.getNumeroNotaDistribuidor());
        pedidoAtual.setObservacoes(pedido.getObservacoes());
        if (pedido.getEntrega() != null) {
            pedidoAtual.setEntrega(normalizarEntrega(pedido.getEntrega()));
        }
        if (pedido.getMetodoPagamento() != null) {
            pedidoAtual.setMetodoPagamento(pedido.getMetodoPagamento());
        }

        if (pedido.getItens() != null && !pedido.getItens().isEmpty()) {
            atualizarQuantidadesDosItens(pedidoAtual, pedido.getItens());
        }

        validarMargemItensPedido(itemPedidoRepository.findByPedido(pedidoAtual));
        recalcularTotaisPedido(pedidoAtual);

        if (Boolean.TRUE.equals(pedido.getFinalizarPedido())) {
            validarPedidoParaFinalizacao(pedido, pedidoAtual);
            PagamentoDTO pagamentoDTO = new PagamentoDTO();
            pagamentoDTO.setMetodoPagamento(pedidoAtual.getMetodoPagamento());
            pagamentoDTO.setDatasPagamento(pedido.getDatasPagamento());

            Pagamento pagamentoPedido = criarPagamentoFromPagamentoDTOAndPedido(pagamentoDTO, pedidoAtual);
            pagamentoRepository.save(pagamentoPedido);
            criarParcelasComComissao(pagamentoPedido, pedidoAtual, pedido.getDatasPagamento());
            pedidoAtual.setStatusPedido("APROVADO");
        }

        pedidoRepository.save(pedidoAtual);

        return convertToPedidoResponse(pedidoAtual, itemPedidoRepository.findByPedido(pedidoAtual));
    }

    /**
     * O pedido precisa de ao menos um item, e cada item precisa de produto, quantidade inteira maior
     * que zero e valores unitários válidos (ver validarMargem).
     */
    private void validarItensDoPedido(List<ItemPedidoRequest> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new RequisicaoInvalida("Informe ao menos um item no pedido.");
        }
        for (int i = 0; i < itens.size(); i++) {
            ItemPedidoRequest item = itens.get(i);
            String identificacao = "Item " + (i + 1);
            if (item == null || item.getFkProduto() == null) {
                throw new RequisicaoInvalida(identificacao + ": informe o produto.");
            }
            if (item.getQuantidade() == null || item.getQuantidade() < 1) {
                throw new RequisicaoInvalida(identificacao + ": a quantidade deve ser maior que zero.");
            }
            validarMargem(item.getVlrUnitDistr(), item.getVlrUnitCliente(), identificacao);
        }
    }

    private void validarMargemItensPedido(List<ItemPedido> itens) {
        for (ItemPedido item : itens) {
            String nome = item.getProduto() != null ? item.getProduto().getNome() : "Item";
            validarMargem(item.getVlrUnitDistr(), item.getVlrUnitCliente(), nome);
        }
    }

    // Comissão nunca negativa: em cada item o valor faturado pode ser igual, jamais menor que o do distribuidor.
    private void validarMargem(BigDecimal valorUnitDistr, BigDecimal valorUnitCliente, String identificacao) {
        if (valorUnitDistr == null || valorUnitCliente == null) {
            throw new RequisicaoInvalida(identificacao + ": informe os valores unitários do distribuidor e do cliente.");
        }
        if (valorUnitDistr.signum() < 0 || valorUnitCliente.signum() < 0) {
            throw new RequisicaoInvalida(identificacao + ": os valores unitários não podem ser negativos.");
        }
        if (valorUnitCliente.compareTo(valorUnitDistr) < 0) {
            throw new RequisicaoInvalida(identificacao
                    + ": o valor unitário faturado não pode ser menor que o valor unitário do distribuidor.");
        }
    }

    /**
     * Aplica a nova quantidade de cada item enviado e recalcula os totais desses itens.
     * Valida tudo antes de alterar: item que não é deste pedido responde 404 e quantidade
     * menor que 1 responde 400, sem mudar nenhum item.
     */
    private void atualizarQuantidadesDosItens(Pedido pedidoAtual, List<PedidoEditRequest.ItemQuantidade> novasQuantidades) {
        Map<Integer, ItemPedido> itensDoPedido = itemPedidoRepository.findByPedido(pedidoAtual).stream()
                .collect(Collectors.toMap(ItemPedido::getIdItemPedido, item -> item));

        for (PedidoEditRequest.ItemQuantidade novo : novasQuantidades) {
            if (novo == null || novo.getIdItemPedido() == null || !itensDoPedido.containsKey(novo.getIdItemPedido())) {
                Integer id = novo == null ? null : novo.getIdItemPedido();
                throw new RecursoNaoEncontrado("Item do pedido não encontrado com id: " + id);
            }
            if (novo.getQuantidade() == null || novo.getQuantidade() < 1) {
                ItemPedido item = itensDoPedido.get(novo.getIdItemPedido());
                String nome = item.getProduto() != null ? item.getProduto().getNome() : "Item " + item.getIdItemPedido();
                throw new RequisicaoInvalida(nome + ": a quantidade deve ser maior que zero.");
            }
        }

        for (PedidoEditRequest.ItemQuantidade novo : novasQuantidades) {
            ItemPedido item = itensDoPedido.get(novo.getIdItemPedido());
            BigDecimal quantidade = BigDecimal.valueOf(novo.getQuantidade());
            item.setQuantidade(novo.getQuantidade());
            item.setVlrTotalDistr(item.getVlrUnitDistr().multiply(quantidade));
            item.setVlrTotalCliente(item.getVlrUnitCliente().multiply(quantidade));
            itemPedidoRepository.save(item);
        }
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

    private void validarPedidoParaFinalizacao(PedidoEditRequest pedido, Pedido pedidoAtual) {
        if (pagamentoRepository.existsByPedido_IdPedido(pedido.getIdPedido())) {
            throw new OperacaoNaoPermitida("Pedido já foi finalizado.");
        }
        if (pedido.getNumeroNotaDistribuidor() == null || pedido.getNumeroNotaDistribuidor().isBlank()) {
            throw new IllegalArgumentException("Informe o número da nota para finalizar o pedido.");
        }
        if (pedido.getObservacoes() == null || pedido.getObservacoes().isBlank()) {
            throw new IllegalArgumentException("Informe a observação para finalizar o pedido.");
        }
        if (pedidoAtual.getMetodoPagamento() == null) {
            throw new IllegalArgumentException("Informe o método de pagamento para finalizar o pedido.");
        }
    }
}
