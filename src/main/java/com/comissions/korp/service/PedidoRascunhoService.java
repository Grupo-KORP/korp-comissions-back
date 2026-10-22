package com.comissions.korp.service;

import com.comissions.korp.DTO.RascunhoDTO.RascunhoRequest;
import com.comissions.korp.DTO.RascunhoDTO.RascunhoResponse;
import com.comissions.korp.DTO.RascunhoDTO.RascunhoResumoResponse;
import com.comissions.korp.entity.PedidoRascunho;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.LimiteRascunhosAtingido;
import com.comissions.korp.exception.RascunhoDesatualizado;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.RequisicaoInvalida;
import com.comissions.korp.repository.PedidoRascunhoRepository;
import com.comissions.korp.repository.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class PedidoRascunhoService {

    public static final int LIMITE_RASCUNHOS = 10;
    public static final int DIAS_EXPIRACAO = 30;
    private static final int VERSAO_SCHEMA = 1;
    private static final int TAMANHO_MAXIMO_JSON = 200_000;
    private static final int TAMANHO_MAXIMO_NOME = 150;
    private static final Set<String> METODOS_PAGAMENTO = Set.of("AVISTA", "BOLETO");

    private final PedidoRascunhoRepository rascunhoRepository;
    private final UsuarioRepository usuarioRepository;
    private final JsonMapper jsonMapper;

    public PedidoRascunhoService(PedidoRascunhoRepository rascunhoRepository,
                                 UsuarioRepository usuarioRepository,
                                 JsonMapper jsonMapper) {
        this.rascunhoRepository = rascunhoRepository;
        this.usuarioRepository = usuarioRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional(readOnly = true)
    public List<RascunhoResumoResponse> listar(Integer idVendedor) {
        return rascunhoRepository
                .findByUsuario_IdUsuarioAndDtAtualizacaoAfterOrderByDtAtualizacaoDesc(idVendedor, limiteExpiracao())
                .stream()
                .map(this::toResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public RascunhoResponse buscar(Integer idVendedor, Integer idRascunho) {
        return toResponse(buscarEntidade(idVendedor, idRascunho));
    }

    @Transactional
    public RascunhoResponse criar(Integer idVendedor, RascunhoRequest request) {
        Dados dados = validarDados(request.getDados());

        long ativos = rascunhoRepository.countByUsuario_IdUsuarioAndDtAtualizacaoAfter(idVendedor, limiteExpiracao());
        if (ativos >= LIMITE_RASCUNHOS) {
            throw new LimiteRascunhosAtingido(
                    "Limite de " + LIMITE_RASCUNHOS + " rascunhos atingido. Exclua um para salvar outro.");
        }

        Usuario vendedor = usuarioRepository.findById(idVendedor)
                .orElseThrow(() -> new RecursoNaoEncontrado("Vendedor não encontrado com id: " + idVendedor));

        LocalDateTime agora = LocalDateTime.now();
        PedidoRascunho rascunho = new PedidoRascunho();
        rascunho.setUsuario(vendedor);
        rascunho.setDtCriacao(agora);
        rascunho.setDtAtualizacao(agora);
        preencher(rascunho, dados);

        return toResponse(rascunhoRepository.saveAndFlush(rascunho));
    }

    @Transactional
    public RascunhoResponse atualizar(Integer idVendedor, Integer idRascunho, RascunhoRequest request) {
        Dados dados = validarDados(request.getDados());
        if (request.getVersao() == null) {
            throw new RequisicaoInvalida("Informe a versão do rascunho.");
        }

        PedidoRascunho rascunho = buscarEntidade(idVendedor, idRascunho);
        if (!request.getVersao().equals(rascunho.getVersao())) {
            throw new RascunhoDesatualizado(
                    "Este rascunho foi alterado em outra aba ou dispositivo. Reabra-o pela lista de rascunhos.");
        }

        preencher(rascunho, dados);
        rascunho.setDtAtualizacao(LocalDateTime.now());

        return toResponse(rascunhoRepository.saveAndFlush(rascunho));
    }

    @Transactional
    public void excluir(Integer idVendedor, Integer idRascunho) {
        rascunhoRepository.delete(buscarEntidade(idVendedor, idRascunho));
    }

    /** Remove o rascunho que originou o pedido; ignora ids inexistentes ou de outro vendedor. */
    @Transactional
    public void excluirAposCriarPedido(Integer idVendedor, Integer idRascunho) {
        if (idRascunho != null) {
            rascunhoRepository.deleteByIdRascunhoAndUsuario_IdUsuario(idRascunho, idVendedor);
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void apagarExpirados() {
        rascunhoRepository.apagarExpirados(limiteExpiracao());
    }

    private LocalDateTime limiteExpiracao() {
        return LocalDateTime.now().minusDays(DIAS_EXPIRACAO);
    }

    // 404 também para rascunho de outro vendedor ou expirado, para não revelar que ele existe
    private PedidoRascunho buscarEntidade(Integer idVendedor, Integer idRascunho) {
        return rascunhoRepository
                .findByIdRascunhoAndUsuario_IdUsuarioAndDtAtualizacaoAfter(idRascunho, idVendedor, limiteExpiracao())
                .orElseThrow(() -> new RecursoNaoEncontrado("Rascunho não encontrado com id: " + idRascunho));
    }

    private record Dados(ObjectNode no, String json) {
    }

    private Dados validarDados(JsonNode dados) {
        if (dados == null || !dados.isObject()) {
            throw new RequisicaoInvalida("Informe os dados do rascunho.");
        }
        if (!dados.path("cliente").isObject()
                || !dados.path("distribuidor").isObject()
                || !dados.path("produtos").isArray()) {
            throw new RequisicaoInvalida("Rascunho inválido: cliente, distribuidor e produtos são obrigatórios.");
        }

        ObjectNode copia = ((ObjectNode) dados).deepCopy();
        copia.remove("pdfBase64");

        String json = jsonMapper.writeValueAsString(copia);
        if (json.length() > TAMANHO_MAXIMO_JSON) {
            throw new RequisicaoInvalida("Rascunho muito grande.");
        }

        String metodo = texto(copia, "formaPagamento");
        if (metodo != null && !METODOS_PAGAMENTO.contains(metodo)) {
            throw new RequisicaoInvalida("Forma de pagamento inválida: use AVISTA ou BOLETO.");
        }

        return new Dados(copia, json);
    }

    private void preencher(PedidoRascunho rascunho, Dados dados) {
        ObjectNode no = dados.no();
        rascunho.setDados(dados.json());
        rascunho.setVersaoSchema(VERSAO_SCHEMA);
        rascunho.setNomeCliente(nomeExibicao(no.path("cliente")));
        rascunho.setNomeDistribuidor(nomeExibicao(no.path("distribuidor")));
        rascunho.setQtdProdutos(no.path("produtos").size());
        rascunho.setMetodoPagamento(texto(no, "formaPagamento"));
    }

    private String nomeExibicao(JsonNode pessoa) {
        String nome = texto(pessoa, "nomeFantasia");
        if (nome == null) {
            nome = texto(pessoa, "razaoSocial");
        }
        return nome != null && nome.length() > TAMANHO_MAXIMO_NOME ? nome.substring(0, TAMANHO_MAXIMO_NOME) : nome;
    }

    private String texto(JsonNode no, String campo) {
        JsonNode valor = no.path(campo);
        if (!valor.isString()) {
            return null;
        }
        String texto = valor.stringValue().trim();
        return texto.isEmpty() ? null : texto;
    }

    private RascunhoResumoResponse toResumo(PedidoRascunho rascunho) {
        RascunhoResumoResponse resumo = new RascunhoResumoResponse();
        preencherResumo(resumo, rascunho);
        return resumo;
    }

    private RascunhoResponse toResponse(PedidoRascunho rascunho) {
        RascunhoResponse response = new RascunhoResponse();
        preencherResumo(response, rascunho);
        response.setDados(jsonMapper.readTree(rascunho.getDados()));
        response.setVersao(rascunho.getVersao());
        response.setVersaoSchema(rascunho.getVersaoSchema());
        return response;
    }

    private void preencherResumo(RascunhoResumoResponse resumo, PedidoRascunho rascunho) {
        resumo.setId(rascunho.getIdRascunho());
        resumo.setNomeCliente(rascunho.getNomeCliente());
        resumo.setNomeDistribuidor(rascunho.getNomeDistribuidor());
        resumo.setQtdProdutos(rascunho.getQtdProdutos());
        resumo.setMetodoPagamento(rascunho.getMetodoPagamento());
        resumo.setDtAtualizacao(rascunho.getDtAtualizacao());
        resumo.setDtExpiracao(rascunho.getDtAtualizacao().plusDays(DIAS_EXPIRACAO));
    }
}
