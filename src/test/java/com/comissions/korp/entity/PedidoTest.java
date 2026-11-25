package com.comissions.korp.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoTest {

    @Test
    void getCodigo_deveUsarDataDeCriacaoESequenciaDoDiaComDoisDigitos() {
        assertThat(pedido(LocalDate.of(2026, 11, 25), 3).getCodigo()).isEqualTo("251126-P03");
        assertThat(pedido(LocalDate.of(2026, 1, 5), 12).getCodigo()).isEqualTo("050126-P12");
    }

    @Test
    void getCodigo_naoDeveCortarSequenciaAcimaDe99() {
        assertThat(pedido(LocalDate.of(2026, 11, 25), 100).getCodigo()).isEqualTo("251126-P100");
    }

    @Test
    void getCodigo_deveSerNulo_quandoPedidoAindaNaoTemDataOuNumero() {
        assertThat(pedido(null, 1).getCodigo()).isNull();
        assertThat(pedido(LocalDate.of(2026, 11, 25), null).getCodigo()).isNull();
    }

    private Pedido pedido(LocalDate data, Integer numeroDia) {
        Pedido pedido = new Pedido();
        pedido.setDataPedido(data);
        pedido.setNumeroDia(numeroDia);
        return pedido;
    }
}
