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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
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
class PedidoRascunhoServiceTest {

    private static final String DADOS_VALIDOS = """
            {"cliente":{"nomeFantasia":"Acme","razaoSocial":"Acme Ltda"},
             "distribuidor":{"nomeFantasia":"","razaoSocial":"Distribuidora SA"},
             "produtos":[{"descricao":"A"},{"descricao":"B"}],
             "formaPagamento":"BOLETO","pdfBase64":"JVBERi0="}
            """;

    @Mock
    private PedidoRascunhoRepository rascunhoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private PedidoRascunhoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoRascunhoService(rascunhoRepository, usuarioRepository, jsonMapper);
    }

    @Test
    void criar_deveSalvarRascunhoComResumoEDescartarPdf() {
        // Arrange
        when(rascunhoRepository.countByUsuario_IdUsuarioAndDtAtualizacaoAfter(eq(5), any(LocalDateTime.class))).thenReturn(9L);
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(new Usuario()));
        when(rascunhoRepository.saveAndFlush(any(PedidoRascunho.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RascunhoResponse response = service.criar(5, request(DADOS_VALIDOS, null));

        // Assert
        ArgumentCaptor<PedidoRascunho> captor = ArgumentCaptor.forClass(PedidoRascunho.class);
        verify(rascunhoRepository).saveAndFlush(captor.capture());
        PedidoRascunho salvo = captor.getValue();
        assertThat(salvo.getNomeCliente()).isEqualTo("Acme");
        assertThat(salvo.getNomeDistribuidor()).isEqualTo("Distribuidora SA");
        assertThat(salvo.getQtdProdutos()).isEqualTo(2);
        assertThat(salvo.getMetodoPagamento()).isEqualTo("BOLETO");
        assertThat(salvo.getDados()).doesNotContain("pdfBase64").contains("Acme");
        assertThat(response.getDados().path("produtos").size()).isEqualTo(2);
        assertThat(response.getDtExpiracao()).isEqualTo(response.getDtAtualizacao().plusDays(30));
    }

    @Test
    void criar_deveFalhar_quandoVendedorJaTemDezRascunhos() {
        // Arrange
        when(rascunhoRepository.countByUsuario_IdUsuarioAndDtAtualizacaoAfter(eq(5), any(LocalDateTime.class))).thenReturn(10L);

        // Act / Assert
        assertThatThrownBy(() -> service.criar(5, request(DADOS_VALIDOS, null)))
                .isInstanceOf(LimiteRascunhosAtingido.class)
                .hasMessageContaining("Limite de 10 rascunhos atingido");

        verify(rascunhoRepository, never()).saveAndFlush(any());
    }

    @Test
    void criar_deveFalhar_quandoFormaPagamentoNaoEhAvistaNemBoleto() {
        String dados = "{\"cliente\":{},\"distribuidor\":{},\"produtos\":[],\"formaPagamento\":\"PIX\"}";

        assertThatThrownBy(() -> service.criar(5, request(dados, null)))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("AVISTA ou BOLETO");
    }

    @Test
    void criar_deveAceitarRascunhoIncompletoSemFormaDePagamento() {
        // Arrange
        String dados = "{\"cliente\":{},\"distribuidor\":{},\"produtos\":[],\"formaPagamento\":\"\"}";
        when(rascunhoRepository.countByUsuario_IdUsuarioAndDtAtualizacaoAfter(eq(5), any(LocalDateTime.class))).thenReturn(0L);
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(new Usuario()));
        when(rascunhoRepository.saveAndFlush(any(PedidoRascunho.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.criar(5, request(dados, null));

        // Assert
        ArgumentCaptor<PedidoRascunho> captor = ArgumentCaptor.forClass(PedidoRascunho.class);
        verify(rascunhoRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getMetodoPagamento()).isNull();
        assertThat(captor.getValue().getNomeCliente()).isNull();
        assertThat(captor.getValue().getQtdProdutos()).isZero();
    }

    @Test
    void criar_deveFalhar_quandoFaltaClienteDistribuidorOuProdutos() {
        assertThatThrownBy(() -> service.criar(5, request("{\"cliente\":{},\"produtos\":[]}", null)))
                .isInstanceOf(RequisicaoInvalida.class);
        assertThatThrownBy(() -> service.criar(5, request("[]", null)))
                .isInstanceOf(RequisicaoInvalida.class);
    }

    @Test
    void criar_deveFalhar_quandoRascunhoEhGrandeDemais() {
        String enorme = "x".repeat(200_001);
        String dados = "{\"cliente\":{},\"distribuidor\":{},\"produtos\":[],\"observacao\":\"" + enorme + "\"}";

        assertThatThrownBy(() -> service.criar(5, request(dados, null)))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("muito grande");
    }

    @Test
    void atualizar_deveFalhar_quandoVersaoDiverge() {
        // Arrange
        PedidoRascunho existente = rascunho(3, 4);
        when(rascunhoRepository.findByIdRascunhoAndUsuario_IdUsuarioAndDtAtualizacaoAfter(eq(3), eq(5), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existente));

        // Act / Assert
        assertThatThrownBy(() -> service.atualizar(5, 3, request(DADOS_VALIDOS, 3)))
                .isInstanceOf(RascunhoDesatualizado.class);

        verify(rascunhoRepository, never()).saveAndFlush(any());
    }

    @Test
    void atualizar_deveExigirVersao() {
        assertThatThrownBy(() -> service.atualizar(5, 3, request(DADOS_VALIDOS, null)))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("versão");
    }

    @Test
    void atualizar_deveSubstituirDadosEAtualizarData_quandoVersaoConfere() {
        // Arrange
        PedidoRascunho existente = rascunho(3, 4);
        LocalDateTime antes = existente.getDtAtualizacao();
        when(rascunhoRepository.findByIdRascunhoAndUsuario_IdUsuarioAndDtAtualizacaoAfter(eq(3), eq(5), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existente));
        when(rascunhoRepository.saveAndFlush(existente)).thenReturn(existente);

        // Act
        RascunhoResponse response = service.atualizar(5, 3, request(DADOS_VALIDOS, 4));

        // Assert
        assertThat(existente.getNomeCliente()).isEqualTo("Acme");
        assertThat(existente.getDtAtualizacao()).isAfter(antes);
        assertThat(response.getId()).isEqualTo(3);
    }

    @Test
    void buscar_deveRetornar404_quandoRascunhoNaoPertenceAoVendedorOuExpirou() {
        // Arrange
        when(rascunhoRepository.findByIdRascunhoAndUsuario_IdUsuarioAndDtAtualizacaoAfter(eq(3), eq(8), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> service.buscar(8, 3)).isInstanceOf(RecursoNaoEncontrado.class);
        assertThatThrownBy(() -> service.excluir(8, 3)).isInstanceOf(RecursoNaoEncontrado.class);
        verify(rascunhoRepository, never()).delete(any(PedidoRascunho.class));
    }

    @Test
    void listar_deveConsultarApenasRascunhosNaoExpiradosDoVendedor() {
        // Arrange
        PedidoRascunho recente = rascunho(3, 0);
        when(rascunhoRepository.findByUsuario_IdUsuarioAndDtAtualizacaoAfterOrderByDtAtualizacaoDesc(eq(5), any(LocalDateTime.class)))
                .thenReturn(List.of(recente));

        // Act
        List<RascunhoResumoResponse> lista = service.listar(5);

        // Assert
        ArgumentCaptor<LocalDateTime> limite = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(rascunhoRepository).findByUsuario_IdUsuarioAndDtAtualizacaoAfterOrderByDtAtualizacaoDesc(eq(5), limite.capture());
        assertThat(limite.getValue()).isBetween(LocalDateTime.now().minusDays(30).minusMinutes(1), LocalDateTime.now().minusDays(30).plusMinutes(1));
        assertThat(lista).hasSize(1);
        assertThat(lista.getFirst().getDtExpiracao()).isEqualTo(recente.getDtAtualizacao().plusDays(30));
    }

    @Test
    void apagarExpirados_deveRemoverRascunhosComMaisDeTrintaDiasSemAlteracao() {
        // Act
        service.apagarExpirados();

        // Assert
        ArgumentCaptor<LocalDateTime> limite = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(rascunhoRepository).apagarExpirados(limite.capture());
        assertThat(limite.getValue()).isBetween(LocalDateTime.now().minusDays(30).minusMinutes(1), LocalDateTime.now().minusDays(30).plusMinutes(1));
    }

    @Test
    void excluirAposCriarPedido_deveIgnorarQuandoNaoHaRascunho() {
        service.excluirAposCriarPedido(5, null);

        verify(rascunhoRepository, never()).deleteByIdRascunhoAndUsuario_IdUsuario(any(), any());
    }

    @Test
    void excluirAposCriarPedido_deveRemoverApenasRascunhoDoProprioVendedor() {
        service.excluirAposCriarPedido(5, 3);

        verify(rascunhoRepository).deleteByIdRascunhoAndUsuario_IdUsuario(3, 5);
    }

    private RascunhoRequest request(String json, Integer versao) {
        RascunhoRequest request = new RascunhoRequest();
        request.setDados(jsonMapper.readTree(json));
        request.setVersao(versao);
        return request;
    }

    private PedidoRascunho rascunho(Integer id, Integer versao) {
        PedidoRascunho rascunho = new PedidoRascunho();
        rascunho.setIdRascunho(id);
        rascunho.setVersao(versao);
        rascunho.setVersaoSchema(1);
        rascunho.setDados("{\"cliente\":{},\"distribuidor\":{},\"produtos\":[]}");
        rascunho.setQtdProdutos(0);
        rascunho.setDtCriacao(LocalDateTime.now().minusDays(2));
        rascunho.setDtAtualizacao(LocalDateTime.now().minusDays(1));
        return rascunho;
    }
}
