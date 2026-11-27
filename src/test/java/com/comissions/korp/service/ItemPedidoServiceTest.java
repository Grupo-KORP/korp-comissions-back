package com.comissions.korp.service;

import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.entity.Produto;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.OperacaoNaoPermitida;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.RequisicaoInvalida;
import com.comissions.korp.repository.ItemPedidoRepository;
import com.comissions.korp.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemPedidoServiceTest {

    @Mock
    private ItemPedidoRepository itemPedidoRepository;
    @Mock
    private ProdutoService produtoService;
    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private com.comissions.korp.config.PainelVendedorCache painelVendedorCache;

    private ItemPedidoService itemPedidoService;
    private Pedido pedido;
    private ItemPedido item;

    @BeforeEach
    void setUp() {
        itemPedidoService = new ItemPedidoService(itemPedidoRepository, produtoService, pedidoRepository, painelVendedorCache);

        Usuario dono = new Usuario();
        dono.setIdUsuario(99);
        pedido = new Pedido();
        pedido.setIdPedido(30);
        pedido.setUsuario(dono);
        pedido.setStatusPedido("EM_ANDAMENTO");
        pedido.setValorTotalDistr(new BigDecimal("50.00"));
        pedido.setValorTotalCliente(new BigDecimal("70.00"));

        item = item(5, "20.00", "30.00");
    }

    private ItemPedido item(Integer id, String totalDistr, String totalCliente) {
        Produto produto = new Produto();
        produto.setIdProduto(id);
        produto.setNome("Produto " + id);

        ItemPedido novo = new ItemPedido();
        novo.setIdItemPedido(id);
        novo.setPedido(pedido);
        novo.setProduto(produto);
        novo.setQuantidade(1);
        novo.setVlrTotalDistr(new BigDecimal(totalDistr));
        novo.setVlrTotalCliente(new BigDecimal(totalCliente));
        return novo;
    }

    @Test
    void deletar_deveResponderNaoEncontrado_quandoItemEDePedidoDeOutroVendedor() {
        when(itemPedidoRepository.findById(5)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemPedidoService.deletar(5, 7))
                .isInstanceOf(RecursoNaoEncontrado.class)
                .hasMessageContaining("5");

        verify(itemPedidoRepository, never()).deleteById(any());
    }

    @Test
    void deletar_deveRemoverItemERecalcularTotaisDoPedido() {
        // pedido: item 5 (20/30) + item 6 (30/40) = 50/70; sobra só o item 6
        when(itemPedidoRepository.findById(5)).thenReturn(Optional.of(item));
        when(itemPedidoRepository.findByPedido(pedido)).thenReturn(List.of(item, item(6, "30.00", "40.00")));

        itemPedidoService.deletar(5, 99);

        verify(itemPedidoRepository).deleteById(5);
        verify(pedidoRepository).save(pedido);
        assertThat(pedido.getValorTotalDistr()).isEqualByComparingTo("30.00");
        assertThat(pedido.getValorTotalCliente()).isEqualByComparingTo("40.00");
    }

    @Test
    void deletar_deveRecusar_quandoPedidoJaFoiAprovado() {
        pedido.setStatusPedido("APROVADO");
        when(itemPedidoRepository.findById(5)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemPedidoService.deletar(5, 99))
                .isInstanceOf(OperacaoNaoPermitida.class)
                .hasMessage("Pedido aprovado não pode ser alterado.");

        verify(itemPedidoRepository, never()).deleteById(any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deletar_deveRecusar_quandoEOUltimoItemDoPedido() {
        when(itemPedidoRepository.findById(5)).thenReturn(Optional.of(item));
        when(itemPedidoRepository.findByPedido(pedido)).thenReturn(List.of(item));

        assertThatThrownBy(() -> itemPedidoService.deletar(5, 99))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("pelo menos um item");

        verify(itemPedidoRepository, never()).deleteById(any());
        assertThat(pedido.getValorTotalCliente()).isEqualByComparingTo("70.00");
    }

    @Test
    void buscarDtoPorId_deveResponderNaoEncontrado_quandoItemEDeOutroVendedor() {
        when(itemPedidoRepository.findById(5)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemPedidoService.buscarDtoPorId(5, 7))
                .isInstanceOf(RecursoNaoEncontrado.class);
    }

    @Test
    void buscarPorPedido_deveResponderNaoEncontrado_quandoPedidoEDeOutroVendedor() {
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> itemPedidoService.buscarPorPedido(30, 7))
                .isInstanceOf(RecursoNaoEncontrado.class);
        verify(itemPedidoRepository, never()).findByPedido(any());
    }

    @Test
    void deletar_deveResponderNaoEncontrado_quandoPedidoDoItemFoiExcluido() {
        pedido.setAtivo(false);
        when(itemPedidoRepository.findById(5)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemPedidoService.deletar(5, 99))
                .isInstanceOf(RecursoNaoEncontrado.class);
        verify(itemPedidoRepository, never()).deleteById(any());
    }

    @Test
    void buscarPorPedido_deveListarItens_quandoPedidoEDoVendedor() {
        when(pedidoRepository.findById(30)).thenReturn(Optional.of(pedido));
        when(itemPedidoRepository.findByPedido(pedido)).thenReturn(List.of(item));

        assertThat(itemPedidoService.buscarPorPedido(30, 99)).hasSize(1);
    }
}
