package com.comissions.korp.service;

import com.comissions.korp.DTO.ProdutoDTO.ProdutoRequest;
import com.comissions.korp.DTO.ProdutoDTO.ProdutoResponse;
import com.comissions.korp.entity.Produto;
import com.comissions.korp.exception.RequisicaoInvalida;
import com.comissions.korp.exception.UsuarioJaExistente;
import com.comissions.korp.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    private ProdutoService produtoService;

    @BeforeEach
    void setUp() {
        produtoService = new ProdutoService(produtoRepository);
    }

    private ProdutoRequest request(String nome, String pn) {
        ProdutoRequest request = new ProdutoRequest();
        request.setNome(nome);
        request.setPn(pn);
        return request;
    }

    private Produto produto(int id, String pn, boolean ativo) {
        Produto produto = new Produto();
        produto.setIdProduto(id);
        produto.setNome("Produto " + id);
        produto.setPartNumber(pn);
        produto.setAtivo(ativo);
        return produto;
    }

    private void salvarRetornandoArgumento() {
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
            Produto produto = invocation.getArgument(0);
            if (produto.getIdProduto() == null) produto.setIdProduto(1);
            return produto;
        });
    }

    @Test
    void cadastrar_deveGravarPartNumberSemEspacos() {
        salvarRetornandoArgumento();
        when(produtoRepository.existsByPartNumberIgnoreCaseAndAtivoTrue("ABC-001")).thenReturn(false);

        ProdutoResponse response = produtoService.cadastrarProduto(request("Mouse", "  ABC-001  "));

        assertThat(response.getPn()).isEqualTo("ABC-001");
        assertThat(response.getCodigoProduto()).isEqualTo("#P0001");
    }

    @Test
    void cadastrar_deveAceitarProdutoSemPartNumber() {
        salvarRetornandoArgumento();

        ProdutoResponse response = produtoService.cadastrarProduto(request("Mouse", "   "));

        assertThat(response.getPn()).isNull();
        verify(produtoRepository, never()).existsByPartNumberIgnoreCaseAndAtivoTrue(anyString());
    }

    @Test
    void cadastrar_deveRecusarPartNumberComCaracterInvalido() {
        assertThatThrownBy(() -> produtoService.cadastrarProduto(request("Mouse", "AB C#1")))
                .isInstanceOf(RequisicaoInvalida.class);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void cadastrar_deveRecusarPartNumberComMaisDe50Caracteres() {
        assertThatThrownBy(() -> produtoService.cadastrarProduto(request("Mouse", "A".repeat(51))))
                .isInstanceOf(RequisicaoInvalida.class);
    }

    @Test
    void cadastrar_deveRecusarPartNumberJaUsadoPorProdutoAtivo() {
        when(produtoRepository.existsByPartNumberIgnoreCaseAndAtivoTrue("ABC-001")).thenReturn(true);

        assertThatThrownBy(() -> produtoService.cadastrarProduto(request("Mouse", "ABC-001")))
                .isInstanceOf(UsuarioJaExistente.class);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void atualizar_deveManterPartNumber_quandoCampoNaoVem() {
        Produto existente = produto(5, "ABC-001", true);
        when(produtoRepository.findById(5)).thenReturn(Optional.of(existente));
        salvarRetornandoArgumento();

        ProdutoResponse response = produtoService.atualizarProduto(5, request("Novo nome", null));

        assertThat(response.getPn()).isEqualTo("ABC-001");
        assertThat(response.getNome()).isEqualTo("Novo nome");
        verify(produtoRepository, never()).existsByPartNumberIgnoreCaseAndAtivoTrueAndIdProdutoNot(anyString(), anyInt());
    }

    @Test
    void atualizar_deveLimparPartNumber_quandoVemVazio() {
        Produto existente = produto(5, "ABC-001", true);
        when(produtoRepository.findById(5)).thenReturn(Optional.of(existente));
        salvarRetornandoArgumento();

        ProdutoResponse response = produtoService.atualizarProduto(5, request(null, ""));

        assertThat(response.getPn()).isNull();
    }

    @Test
    void atualizar_naoDeveConferirDuplicidade_quandoPartNumeroNaoMudou() {
        Produto existente = produto(5, "ABC-001", true);
        when(produtoRepository.findById(5)).thenReturn(Optional.of(existente));
        salvarRetornandoArgumento();

        produtoService.atualizarProduto(5, request("Outro nome", "abc-001"));

        verify(produtoRepository, never()).existsByPartNumberIgnoreCaseAndAtivoTrueAndIdProdutoNot(anyString(), anyInt());
    }

    @Test
    void atualizar_deveRecusarPartNumberJaUsadoPorOutroProdutoAtivo() {
        Produto existente = produto(5, "ABC-001", true);
        when(produtoRepository.findById(5)).thenReturn(Optional.of(existente));
        when(produtoRepository.existsByPartNumberIgnoreCaseAndAtivoTrueAndIdProdutoNot("XYZ-9", 5)).thenReturn(true);

        assertThatThrownBy(() -> produtoService.atualizarProduto(5, request(null, "XYZ-9")))
                .isInstanceOf(UsuarioJaExistente.class);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void atualizar_deveRecusarReativacao_quandoPartNumberJaEstaEmUso() {
        Produto inativo = produto(5, "ABC-001", false);
        when(produtoRepository.findById(5)).thenReturn(Optional.of(inativo));
        when(produtoRepository.existsByPartNumberIgnoreCaseAndAtivoTrueAndIdProdutoNot("ABC-001", 5)).thenReturn(true);

        ProdutoRequest request = request(null, null);
        request.setAtivo(true);

        assertThatThrownBy(() -> produtoService.atualizarProduto(5, request))
                .isInstanceOf(UsuarioJaExistente.class);
    }
}
