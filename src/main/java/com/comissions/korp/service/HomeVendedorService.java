package com.comissions.korp.service;

import com.comissions.korp.DTO.HomeVendedorDTO.FiltroStatusVenda;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO.ResumoDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.DetalheVendaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.ParcelaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.PedidoVendaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.PessoaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.ProdutoVendaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.VendaResumoDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.ResumoStatusDTO;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Comissao;
import com.comissions.korp.entity.Contato;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ENUM.StatusParcela;
import com.comissions.korp.entity.Endereco;
import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.entity.Pagamento;
import com.comissions.korp.entity.Parcela;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HomeVendedorService {

    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] MESES = {
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    /** Teto de itens por página (o front usa 5; o PDF pede páginas maiores). */
    private static final int TAMANHO_MAXIMO_PAGINA = 100;
    /** Ordenação fixa: o cliente não escolhe o campo, só a página e o tamanho. */
    private static final Sort ORDENACAO_VENDAS = Sort.by(Sort.Direction.DESC, "dataPedido", "idPedido");

    private static final List<StatusComissao> STATUS_LIBERADAS = FiltroStatusVenda.LIBERADAS.getStatusComissoes();

    private final PedidoRepository pedidoPainelRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ComissaoRepository comissaoRepository;
    private final EnderecoRepository enderecoRepository;
    private final ContatoRepository contatoRepository;
    private final PagamentoRepository pagamentoRepository;

    public HomeVendedorService(
            PedidoRepository pedidoPainelRepository,
            ItemPedidoRepository itemPedidoRepository,
            ComissaoRepository comissaoRepository,
            EnderecoRepository enderecoRepository,
            ContatoRepository contatoRepository,
            PagamentoRepository pagamentoRepository
    ) {
        this.pedidoPainelRepository = pedidoPainelRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.comissaoRepository = comissaoRepository;
        this.enderecoRepository = enderecoRepository;
        this.contatoRepository = contatoRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    private record Intervalo(LocalDate inicio, LocalDate fim) {
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Painel (listagem paginada + resumo)
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public HomeVendedorPainelDTO buscarPainel(
            Integer idVendedor,
            Integer ano,
            Integer mes,
            Integer dia,
            FiltroStatusVenda filtro,
            Pageable pageable
    ) {
        YearMonth periodo = resolverPeriodo(ano, mes);
        Intervalo intervalo = resolverIntervalo(periodo, dia);

        Pageable paginacao = PageRequest.of(
                Math.max(pageable.getPageNumber(), 0),
                Math.min(Math.max(pageable.getPageSize(), 1), TAMANHO_MAXIMO_PAGINA),
                ORDENACAO_VENDAS
        );

        // 1 query: só os pedidos da página (count vem do countQuery do repositório)
        Page<Pedido> paginaPedidos = pedidoPainelRepository.buscarPedidosDoPainel(
                idVendedor,
                intervalo.inicio(),
                intervalo.fim(),
                filtro.getStatusComissoes(),
                StatusParcela.CANCELADO,
                filtro.isIncluiEmAndamento(),
                filtro.isIncluiVendaDoPeriodo(),
                paginacao
        );

        // 1 query: comissões/parcelas de todos os pedidos da página
        Map<Integer, List<Comissao>> comissoesPorPedido = buscarComissoesPorPedido(idVendedor, paginaPedidos.getContent());

        Page<VendaResumoDTO> vendas = paginaPedidos.map(pedido -> {
            List<Comissao> todasDoPedido = comissoesPorPedido.getOrDefault(pedido.getIdPedido(), List.of());
            List<Comissao> doPainel = todasDoPedido.stream()
                    .filter(comissao -> comissaoEntraNoPainel(comissao, intervalo, filtro))
                    .toList();
            return criarVendaResumo(pedido, doPainel, todasDoPedido);
        });

        long totalVendas = filtro == FiltroStatusVenda.TODAS
                ? paginaPedidos.getTotalElements()
                : pedidoPainelRepository.contarPedidosDoPainel(
                idVendedor,
                intervalo.inicio(),
                intervalo.fim(),
                FiltroStatusVenda.TODAS.getStatusComissoes(),
                StatusParcela.CANCELADO,
                FiltroStatusVenda.TODAS.isIncluiEmAndamento(),
                FiltroStatusVenda.TODAS.isIncluiVendaDoPeriodo()
        );

        ResumoDTO resumo = montarResumo(idVendedor, periodo, intervalo, totalVendas);

        List<ParcelaDTO> parcelasLiberadas = comissaoRepository
                .buscarComissoesPorStatusNoPeriodo(
                        idVendedor,
                        STATUS_LIBERADAS,
                        intervalo.inicio(),
                        intervalo.fim(),
                        StatusParcela.CANCELADO
                )
                .stream()
                .map(this::criarParcela)
                .toList();

        return new HomeVendedorPainelDTO(
                periodo.getYear(),
                periodo.getMonthValue(),
                nomeMes(periodo),
                resumo,
                parcelasLiberadas,
                vendas
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Detalhe de uma venda (carregado só ao abrir o modal)
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public DetalheVendaDTO buscarDetalheVenda(Integer idVendedor, Integer idPedido) {
        boolean pertenceAoVendedor = pedidoPainelRepository.existsByIdPedidoAndUsuario_IdUsuario(idPedido, idVendedor)
                || comissaoRepository.existsByPedido_IdPedidoAndUsuario_IdUsuario(idPedido, idVendedor);

        // 404 também quando a venda é de outro vendedor, para não expor que ela existe
        Pedido pedido = pedidoPainelRepository.findByIdPedidoAndAtivoTrue(idPedido)
                .filter(p -> pertenceAoVendedor)
                .orElseThrow(() -> new RecursoNaoEncontrado("Venda não encontrada com ID: " + idPedido));

        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedido);
        return criarDetalheVenda(pedido, itens);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Período e filtros
    // ═══════════════════════════════════════════════════════════════════════════

    private YearMonth resolverPeriodo(Integer ano, Integer mes) {
        LocalDate hoje = LocalDate.now();
        int anoResolvido = ano == null ? hoje.getYear() : ano;
        int mesResolvido = mes == null ? hoje.getMonthValue() : mes;

        if (mesResolvido < 1 || mesResolvido > 12) {
            throw new IllegalArgumentException("Mês inválido: informe um valor entre 1 e 12.");
        }

        return YearMonth.of(anoResolvido, mesResolvido);
    }

    private Intervalo resolverIntervalo(YearMonth periodo, Integer dia) {
        if (dia == null) {
            return new Intervalo(periodo.atDay(1), periodo.atEndOfMonth());
        }
        if (dia < 1 || dia > periodo.lengthOfMonth()) {
            throw new IllegalArgumentException("Dia inválido: informe um valor entre 1 e " + periodo.lengthOfMonth() + ".");
        }
        LocalDate data = periodo.atDay(dia);
        return new Intervalo(data, data);
    }

    private Map<Integer, List<Comissao>> buscarComissoesPorPedido(Integer idVendedor, List<Pedido> pedidos) {
        if (pedidos.isEmpty()) {
            return Map.of();
        }
        List<Integer> ids = pedidos.stream().map(Pedido::getIdPedido).toList();
        return comissaoRepository.buscarComissoesDosPedidos(ids, idVendedor).stream()
                .collect(Collectors.groupingBy(comissao -> comissao.getPedido().getIdPedido()));
    }

    /** Mesma regra do EXISTS da query paginada, aplicada em memória às comissões dos 5 pedidos da página. */
    private boolean comissaoEntraNoPainel(Comissao comissao, Intervalo intervalo, FiltroStatusVenda filtro) {
        Parcela parcela = comissao.getParcela();
        LocalDate vencimento = parcela.getDataVencimento();
        return !vencimento.isBefore(intervalo.inicio())
                && !vencimento.isAfter(intervalo.fim())
                && parcela.getStatusParcela() != StatusParcela.CANCELADO
                && filtro.getStatusComissoes().contains(comissao.getStatusComissao());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Resumo (cards / projeção / tendência) — agregado no banco, sem carregar entidades
    // ═══════════════════════════════════════════════════════════════════════════

    private ResumoDTO montarResumo(Integer idVendedor, YearMonth periodo, Intervalo intervalo, long totalVendas) {
        Map<StatusComissao, ResumoStatusDTO> atual = resumirPorStatus(idVendedor, intervalo);

        YearMonth anterior = periodo.minusMonths(1);
        Map<StatusComissao, ResumoStatusDTO> mesAnterior = resumirPorStatus(
                idVendedor,
                new Intervalo(anterior.atDay(1), anterior.atEndOfMonth())
        );

        BigDecimal projecao = total(atual, StatusComissao.LIBERADA);
        BigDecimal projecaoMesAnterior = total(mesAnterior, StatusComissao.LIBERADA);
        int pendentes = (int) quantidade(atual, StatusComissao.PENDENTE);

        return new ResumoDTO(
                (int) totalVendas,
                (int) (quantidade(atual, StatusComissao.LIBERADA) + quantidade(atual, StatusComissao.PAGA)),
                pendentes,
                projecao,
                pendentes,
                calcularTendencia(projecao, projecaoMesAnterior)
        );
    }

    private Map<StatusComissao, ResumoStatusDTO> resumirPorStatus(Integer idVendedor, Intervalo intervalo) {
        return comissaoRepository
                .resumirPorStatus(idVendedor, intervalo.inicio(), intervalo.fim(), StatusParcela.CANCELADO)
                .stream()
                .collect(Collectors.toMap(ResumoStatusDTO::status, Function.identity()));
    }

    private long quantidade(Map<StatusComissao, ResumoStatusDTO> resumo, StatusComissao status) {
        ResumoStatusDTO linha = resumo.get(status);
        return linha == null || linha.quantidade() == null ? 0L : linha.quantidade();
    }

    private BigDecimal total(Map<StatusComissao, ResumoStatusDTO> resumo, StatusComissao status) {
        ResumoStatusDTO linha = resumo.get(status);
        return linha == null || linha.total() == null ? BigDecimal.ZERO : linha.total();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Montagem de DTOs
    // ═══════════════════════════════════════════════════════════════════════════

    private VendaResumoDTO criarVendaResumo(Pedido pedido, List<Comissao> comissoes, List<Comissao> todasComissoesDoPedido) {
        BigDecimal totalComissao = somarComissoes(comissoes);

        VendaResumoDTO venda = new VendaResumoDTO();
        venda.setId("V" + pedido.getIdPedido());
        venda.setIdPedido(pedido.getIdPedido());
        venda.setNome("VENDA " + pedido.getIdPedido());
        venda.setCliente(criarNomeClienteTabela(pedido.getCliente()));
        venda.setValorComissao(totalComissao);
        // A linha "Venda" mostra o total do período; o detalhamento por parcela agora é
        // uma linha "Pagamento" própria no front (ver criarParcelas/criarParcela abaixo).
        venda.setComissao(formatarMoeda(totalComissao));
        // Status da linha "Venda" reflete o pedido em si, não mais o status das comissões
        // (isso agora aparece separado, em cada linha "Pagamento").
        venda.setStatus(statusPedidoLabel(pedido));
        venda.setTipo(tipoPedido(pedido));
        venda.setDataVenda(pedido.getDataPedido());
        venda.setParcelas(criarParcelas(comissoes));
        venda.setParcelasDaVenda(criarParcelas(todasComissoesDoPedido));
        return venda;
    }

    /**
     * PedidoService só atribui dois valores a statusPedido: "EM_ANDAMENTO" na criação
     * (criarPedidoFromRequest) e "APROVADO" quando a comissão é criada (criarComissao /
     * editarPedidoFromPedidoEditRequest). O default 'PENDENTE' do banco nunca é usado na
     * prática, pois o service sempre sobrescreve para EM_ANDAMENTO. Por isso o fallback
     * abaixo cobre diretamente o caso APROVADO.
     */
    private String statusPedidoLabel(Pedido pedido) {
        return isEmAndamento(pedido) ? "Em andamento" : "Aprovada";
    }

    private String tipoPedido(Pedido pedido) {
        return isEmAndamento(pedido) ? "andamento" : "aprovada";
    }

    private boolean isEmAndamento(Pedido pedido) {
        return "EM_ANDAMENTO".equalsIgnoreCase(pedido.getStatusPedido());
    }

    private DetalheVendaDTO criarDetalheVenda(Pedido pedido, List<ItemPedido> itens) {
        DetalheVendaDTO detalhe = new DetalheVendaDTO();
        detalhe.setCliente(criarPessoaCliente(pedido.getCliente()));
        detalhe.setDistribuidor(criarPessoaDistribuidor(pedido.getDistribuidor()));
        detalhe.setProduto(criarProdutoVenda(pedido, itens));
        detalhe.setPedido(criarPedidoVenda(pedido));
        return detalhe;
    }

    private PedidoVendaDTO criarPedidoVenda(Pedido pedido) {
        PedidoVendaDTO dto = new PedidoVendaDTO();
        dto.setIdPedido(pedido.getIdPedido());
        dto.setStatusPedido(pedido.getStatusPedido());
        dto.setNumeroNotaDistribuidor(pedido.getNumeroNotaDistribuidor());
        dto.setObservacoes(pedido.getObservacoes());

        pagamentoRepository.findByPedido_IdPedido(pedido.getIdPedido()).ifPresent(pagamento -> {
            dto.setMetodoPagamento(pagamento.getMetodoPagamento().name());
            dto.setParcelado(pagamento.getParcelado());
            dto.setQuantidadeParcelas(pagamento.getQuantidadeParcelas());
        });

        return dto;
    }

    private PessoaDTO criarPessoaCliente(Cliente cliente) {
        PessoaDTO pessoa = new PessoaDTO();
        pessoa.setId(cliente.getIdCliente());
        pessoa.setRazaoSocial(cliente.getRazaoSocial());
        pessoa.setNomeFantasia(cliente.getNomeFantasia());
        pessoa.setCnpj(cliente.getCnpj());
        pessoa.setInscricaoEstadual(cliente.getInscricaoEstadual());
        pessoa.setTelefone(cliente.getTelefone());
        pessoa.setEmail(cliente.getEmail());

        enderecoRepository.findFirstByCliente_IdCliente(cliente.getIdCliente())
                .ifPresent(endereco -> preencherEndereco(pessoa, endereco));
        contatoRepository.findByClienteAndAtivoTrue(cliente).stream()
                .findFirst()
                .ifPresent(contato -> preencherContato(pessoa, contato));

        return pessoa;
    }

    private PessoaDTO criarPessoaDistribuidor(Distribuidor distribuidor) {
        PessoaDTO pessoa = new PessoaDTO();
        pessoa.setId(distribuidor.getIdDistribuidor());
        pessoa.setRazaoSocial(distribuidor.getRazaoSocial());
        pessoa.setNomeFantasia(distribuidor.getNomeFantasia());
        pessoa.setCnpj(distribuidor.getCnpj());
        pessoa.setInscricaoEstadual(distribuidor.getInscricaoEstadual());
        pessoa.setTelefone(distribuidor.getTelefone());
        pessoa.setEmail(distribuidor.getEmail());

        enderecoRepository.findFirstByDistribuidor_IdDistribuidor(distribuidor.getIdDistribuidor())
                .ifPresent(endereco -> preencherEndereco(pessoa, endereco));
        contatoRepository.findByDistribuidorAndAtivoTrue(distribuidor).stream()
                .findFirst()
                .ifPresent(contato -> preencherContato(pessoa, contato));

        return pessoa;
    }

    private ProdutoVendaDTO criarProdutoVenda(Pedido pedido, List<ItemPedido> itens) {
        Optional<ItemPedido> primeiroItem = itens.stream()
                .min(Comparator.comparing(ItemPedido::getIdItemPedido));

        ProdutoVendaDTO produto = new ProdutoVendaDTO();
        produto.setEntrega(pedido.getDataPedido() == null ? null : pedido.getDataPedido().format(DATA_BR));

        if (primeiroItem.isEmpty()) {
            produto.setValorTotal(pedido.getValorTotalDistr());
            produto.setTotalFaturado(pedido.getValorTotalCliente());
            return produto;
        }

        ItemPedido item = primeiroItem.get();
        produto.setIdItemPedido(item.getIdItemPedido());
        produto.setIdProduto(item.getProduto().getIdProduto());
        produto.setDescricao(item.getProduto().getNome());
        produto.setPn(item.getProduto().getCodigoProduto());
        produto.setQuantidade(item.getQuantidade());
        produto.setValorUnitario(item.getVlrUnitDistr());
        produto.setValorTotal(item.getVlrTotalDistr());
        produto.setValorUnitarioFaturado(item.getVlrUnitCliente());
        produto.setTotalFaturado(item.getVlrTotalCliente());
        return produto;
    }

    private List<ParcelaDTO> criarParcelas(List<Comissao> comissoes) {
        return comissoes.stream()
                .sorted(Comparator.comparing(comissao -> comissao.getParcela().getNumeroParcela()))
                .map(this::criarParcela)
                .toList();
    }

    private ParcelaDTO criarParcela(Comissao comissao) {
        Parcela parcela = comissao.getParcela();
        Pagamento pagamento = parcela.getPagamento();
        Integer totalParcelas = pagamento.getQuantidadeParcelas();

        ParcelaDTO dto = new ParcelaDTO();
        dto.setIdParcela(parcela.getId());
        dto.setPedidoId(comissao.getPedido().getIdPedido());
        dto.setNumeroParcela(parcela.getNumeroParcela());
        dto.setTotalParcelas(totalParcelas);
        dto.setLabel(totalParcelas == null || totalParcelas <= 1
                ? "À vista"
                : "Parcela " + parcela.getNumeroParcela() + "/" + totalParcelas);
        dto.setValor(comissao.getValorComissao());
        // Status bruto do enum (PENDENTE/LIBERADA/PAGA); o rótulo em português
        // e a cor de cada linha "Pagamento" ficam a cargo do front.
        dto.setStatus(comissao.getStatusComissao().name());
        dto.setDataVencimento(parcela.getDataVencimento());
        return dto;
    }

    private String criarNomeClienteTabela(Cliente cliente) {
        String contato = contatoRepository.findByClienteAndAtivoTrue(cliente).stream()
                .findFirst()
                .map(Contato::getNome)
                .orElse(cliente.getNomeFantasia());
        String empresa = cliente.getNomeFantasia() != null && !cliente.getNomeFantasia().isBlank()
                ? cliente.getNomeFantasia()
                : cliente.getRazaoSocial();

        if (contato == null || contato.isBlank()) {
            return empresa;
        }

        return contato + " - " + empresa;
    }

    private void preencherEndereco(PessoaDTO pessoa, Endereco endereco) {
        pessoa.setCep(endereco.getCep());
        pessoa.setEndereco(formatarEndereco(endereco));
        pessoa.setCidade(endereco.getCidade());
        pessoa.setUf(endereco.getEstado());
    }

    private void preencherContato(PessoaDTO pessoa, Contato contato) {
        pessoa.setContato(contato.getNome());
        if (contato.getEmail() != null && !contato.getEmail().isBlank()) {
            pessoa.setEmail(contato.getEmail());
        }
        if (contato.getTelefone() != null && !contato.getTelefone().isBlank()) {
            pessoa.setTelefone(contato.getTelefone());
        }
    }

    private String formatarEndereco(Endereco endereco) {
        List<String> partes = new ArrayList<>();
        partes.add(endereco.getLogradouro());
        if (endereco.getNumero() != null && !endereco.getNumero().isBlank()) {
            partes.add(endereco.getNumero());
        }
        if (endereco.getComplemento() != null && !endereco.getComplemento().isBlank()) {
            partes.add(endereco.getComplemento());
        }
        if (endereco.getBairro() != null && !endereco.getBairro().isBlank()) {
            partes.add(endereco.getBairro());
        }
        return partes.stream()
                .filter(parte -> parte != null && !parte.isBlank())
                .collect(Collectors.joining(", "));
    }

    private BigDecimal somarComissoes(List<Comissao> comissoes) {
        return comissoes.stream()
                .map(Comissao::getValorComissao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String calcularTendencia(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || BigDecimal.ZERO.compareTo(anterior) == 0) {
            return atual.compareTo(BigDecimal.ZERO) > 0 ? "+100%" : "0%";
        }

        BigDecimal variacao = atual.subtract(anterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(anterior, 0, RoundingMode.HALF_UP);

        return variacao.compareTo(BigDecimal.ZERO) > 0 ? "+" + variacao + "%" : variacao + "%";
    }

    private String formatarMoeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(LOCALE_BR).format(valor == null ? BigDecimal.ZERO : valor);
    }

    private String nomeMes(YearMonth periodo) {
        return MESES[periodo.getMonthValue() - 1];
    }
}