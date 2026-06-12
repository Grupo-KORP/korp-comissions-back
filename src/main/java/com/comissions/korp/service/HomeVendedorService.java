package com.comissions.korp.service;

import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.DetalheVendaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.ParcelaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.PessoaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.PedidoVendaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.ProdutoVendaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.VendaResumoDTO;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Comissao;
import com.comissions.korp.entity.Contato;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.Endereco;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.entity.Pagamento;
import com.comissions.korp.entity.Parcela;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.repository.ContatoRepository;
import com.comissions.korp.repository.EnderecoRepository;
import com.comissions.korp.repository.ItemPedidoRepository;
import com.comissions.korp.repository.PagamentoRepository;
import com.comissions.korp.repository.PedidoRepository;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HomeVendedorService {

    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] MESES = {
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ComissaoRepository comissaoRepository;
    private final EnderecoRepository enderecoRepository;
    private final ContatoRepository contatoRepository;
    private final PagamentoRepository pagamentoRepository;

    public HomeVendedorService(
            PedidoRepository pedidoRepository,
            ItemPedidoRepository itemPedidoRepository,
            ComissaoRepository comissaoRepository,
            EnderecoRepository enderecoRepository,
            ContatoRepository contatoRepository,
            PagamentoRepository pagamentoRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.comissaoRepository = comissaoRepository;
        this.enderecoRepository = enderecoRepository;
        this.contatoRepository = contatoRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    @Transactional(readOnly = true)
    public HomeVendedorResponseDTO buscarPainel(Integer idVendedor, Integer ano, Integer mes) {
        YearMonth periodo = resolverPeriodo(ano, mes);
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.atEndOfMonth();

        List<Comissao> comissoes = comissaoRepository.buscarComissoesDoPainelPorVencimento(idVendedor, inicio, fim);
        List<Pedido> pedidosComComissaoNoPeriodo = comissoes.stream()
                .map(Comissao::getPedido)
                .collect(Collectors.toMap(
                        Pedido::getIdPedido,
                        pedido -> pedido,
                        (pedidoExistente, pedidoDuplicado) -> pedidoExistente,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
        List<Pedido> pedidosEmAndamento = pedidoRepository
                .findByUsuario_IdUsuarioAndAtivoTrueAndStatusPedidoAndDataPedidoBetweenOrderByDataPedidoDescIdPedidoDesc(
                        idVendedor,
                        "EM_ANDAMENTO",
                        inicio,
                        fim
                );
        List<Pedido> pedidos = combinarPedidos(pedidosComComissaoNoPeriodo, pedidosEmAndamento);

        List<Comissao> todasComissoesDosPedidos = pedidos.isEmpty()
                ? List.of()
                : comissaoRepository.findByPedidoIn(pedidos);

        Map<Integer, List<Comissao>> comissoesPorPedido = comissoes.stream()
                .collect(Collectors.groupingBy(comissao -> comissao.getPedido().getIdPedido()));
        Map<Integer, List<Comissao>> todasComissoesPorPedido = todasComissoesDosPedidos.stream()
                .collect(Collectors.groupingBy(comissao -> comissao.getPedido().getIdPedido()));

        List<VendaResumoDTO> vendas = new ArrayList<>();
        Map<String, DetalheVendaDTO> detalhesVenda = new HashMap<>();

        for (Pedido pedido : pedidos) {
            List<Comissao> comissoesDoPedido = comissoesPorPedido.getOrDefault(pedido.getIdPedido(), List.of());
            List<Comissao> todasComissoesDoPedido = todasComissoesPorPedido.getOrDefault(pedido.getIdPedido(), List.of());
            List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedido);
            VendaResumoDTO venda = criarVendaResumo(pedido, comissoesDoPedido, todasComissoesDoPedido);
            vendas.add(venda);
            detalhesVenda.put(periodoKey(periodo, venda.getId()), criarDetalheVenda(pedido, itens));
        }

        BigDecimal projecao = somaComissoesPorStatus(comissoes, StatusComissao.LIBERADA);
        BigDecimal projecaoMesAnterior = buscarProjecaoMesAnterior(idVendedor, periodo);

        HomeVendedorResponseDTO response = new HomeVendedorResponseDTO();
        response.setAno(periodo.getYear());
        response.setMes(periodo.getMonthValue());
        response.setNomeMes(nomeMes(periodo));
        response.setTotalVendas(pedidos.size());
        response.setComissoesLiberadas(contarComissoesLiberadas(comissoes));
        response.setPagamentosPendentes(contarComissoesPendentes(comissoes));
        response.setProjecao(projecao);
        response.setParcelas(contarComissoesPendentes(comissoes));
        response.setTendencia(calcularTendencia(projecao, projecaoMesAnterior));
        response.setVendas(vendas);
        response.setDetalhesVenda(detalhesVenda);

        return response;
    }

    private List<Pedido> combinarPedidos(List<Pedido> pedidosComComissaoNoPeriodo, List<Pedido> pedidosEmAndamento) {
        Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
        pedidosComComissaoNoPeriodo.forEach(pedido -> pedidos.put(pedido.getIdPedido(), pedido));
        pedidosEmAndamento.forEach(pedido -> pedidos.putIfAbsent(pedido.getIdPedido(), pedido));
        return new ArrayList<>(pedidos.values());
    }

    private YearMonth resolverPeriodo(Integer ano, Integer mes) {
        LocalDate hoje = LocalDate.now();
        int anoResolvido = ano == null ? hoje.getYear() : ano;
        int mesResolvido = mes == null ? hoje.getMonthValue() : mes;

        if (mesResolvido < 1 || mesResolvido > 12) {
            throw new IllegalArgumentException("Mês inválido: informe um valor entre 1 e 12.");
        }

        return YearMonth.of(anoResolvido, mesResolvido);
    }

    private VendaResumoDTO criarVendaResumo(Pedido pedido, List<Comissao> comissoes, List<Comissao> todasComissoesDoPedido) {
        BigDecimal totalComissao = somarComissoes(comissoes);
        long parcelasPagas = comissoes.stream()
                .filter(this::isComissaoPaga)
                .count();
        long parcelasLiberadas = comissoes.stream()
                .filter(this::isComissaoLiberada)
                .count();

        VendaResumoDTO venda = new VendaResumoDTO();
        venda.setId("V" + pedido.getIdPedido());
        venda.setIdPedido(pedido.getIdPedido());
        venda.setNome("VENDA " + pedido.getIdPedido());
        venda.setCliente(criarNomeClienteTabela(pedido.getCliente()));
        venda.setValorComissao(totalComissao);
        venda.setComissao(criarTextoComissao(totalComissao, comissoes));
        venda.setStatus(criarStatusVenda(comissoes, parcelasLiberadas, parcelasPagas));
        venda.setTipo(criarTipoVenda(parcelasLiberadas, parcelasPagas));
        venda.setParcelas(criarParcelas(comissoes));
        venda.setParcelasDaVenda(criarParcelas(todasComissoesDoPedido));
        return venda;
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

        ParcelaDTO dto = new ParcelaDTO();
        dto.setIdParcela(parcela.getId());
        dto.setNumeroParcela(parcela.getNumeroParcela());
        dto.setTotalParcelas(pagamento.getQuantidadeParcelas());
        dto.setLabel("Parcela " + parcela.getNumeroParcela() + "/" + pagamento.getQuantidadeParcelas());
        dto.setValor(comissao.getValorComissao());
        dto.setStatus(comissao.getStatusComissao().name());
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

    private String criarTextoComissao(BigDecimal totalComissao, List<Comissao> comissoes) {
        String valor = formatarMoeda(totalComissao);
        if (comissoes.size() == 1) {
            Parcela parcela = comissoes.get(0).getParcela();
            Integer totalParcelas = parcela.getPagamento().getQuantidadeParcelas();
            return parcela.getNumeroParcela() + "/" + totalParcelas + " - " + valor;
        }
        if (comissoes.size() > 1) {
            return comissoes.size() + " parcelas - " + valor;
        }
        return valor;
    }

    private String criarStatusVenda(List<Comissao> comissoes, long parcelasLiberadas, long parcelasPagas) {
        if (comissoes.isEmpty()) {
            return "AGUARDANDO";
        }

        if (parcelasPagas > 0) {
            if (comissoes.size() == 1) {
                return "PAGO " + comissoes.get(0).getParcela().getNumeroParcela() + "ª PARCELA";
            }
            return "PAGO " + parcelasPagas + " PARCELAS";
        }

        if (parcelasLiberadas > 0) {
            if (comissoes.size() == 1) {
                return "LIBERADA " + comissoes.get(0).getParcela().getNumeroParcela() + "ª PARCELA";
            }
            return "LIBERADA " + parcelasLiberadas + " PARCELAS";
        }

        return "AGUARDANDO";
    }

    private String criarTipoVenda(long parcelasLiberadas, long parcelasPagas) {
        if (parcelasPagas > 0) {
            return "paga";
        }
        if (parcelasLiberadas > 0) {
            return "liberada";
        }
        return "pendente";
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

    private BigDecimal buscarProjecaoMesAnterior(Integer idVendedor, YearMonth periodo) {
        YearMonth anterior = periodo.minusMonths(1);
        List<Comissao> comissoesAnteriores = comissaoRepository.buscarComissoesDoPainelPorVencimento(
                idVendedor,
                anterior.atDay(1),
                anterior.atEndOfMonth()
        );
        return somaComissoesPorStatus(comissoesAnteriores, StatusComissao.LIBERADA);
    }

    private BigDecimal somaComissoesPorStatus(List<Comissao> comissoes, StatusComissao status) {
        return comissoes.stream()
                .filter(comissao -> comissao.getStatusComissao() == status)
                .map(Comissao::getValorComissao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal somarComissoes(List<Comissao> comissoes) {
        return comissoes.stream()
                .map(Comissao::getValorComissao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer contarComissoesPendentes(List<Comissao> comissoes) {
        return (int) comissoes.stream()
                .filter(comissao -> comissao.getStatusComissao() == StatusComissao.PENDENTE)
                .count();
    }

    private Integer contarComissoesLiberadas(List<Comissao> comissoes) {
        return (int) comissoes.stream()
                .filter(this::isComissaoLiberada)
                .count();
    }

    private boolean isComissaoLiberada(Comissao comissao) {
        return comissao.getStatusComissao() == StatusComissao.LIBERADA
                || comissao.getStatusComissao() == StatusComissao.PAGA;
    }

    private boolean isComissaoPaga(Comissao comissao) {
        return comissao.getStatusComissao() == StatusComissao.PAGA;
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

    private String periodoKey(YearMonth periodo, String idVenda) {
        return nomeMes(periodo) + "-" + idVenda;
    }

    private String nomeMes(YearMonth periodo) {
        return MESES[periodo.getMonthValue() - 1];
    }
}
