package com.comissions.korp.entity;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPedido;

    @Column(nullable = false)
    private LocalDate dataPedido;

    @Column(nullable = false)
    private Integer numeroNotaDistribuidor;

    @Column(nullable = false)
    private Double valorTotalRevenda;

    @Column(nullable = false)
    private Double valorTotalFaturamento;

    @Column(nullable = false, length = 50)
    private String statusPedido;

    @Column(nullable = false)
    private Boolean frete;

    @Column(nullable = false, length = 150)
    private String transportadora;

    @Column(length = 250)
    private String observacoes;

//        @ManyToOne
//        @JoinColumn(name = "fkVendedor")
//        private Vendedor vendedor;

    @ManyToOne
    @JoinColumn(name = "fkCliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "fkDistribuidor")
    private Distribuidor distribuidor;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<ItemPedido> itens;
}
