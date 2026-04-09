package com.comissions.korp.entity;


import com.comissions.korp.entity.ENUM.StatusComissao;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "comissao")
public class Comissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comissao")
    private Integer id;

    @Column(name = "valor_comissao", nullable = false, precision = 10)
    private Double valorComissao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_comissao", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PENDENTE'")
    private StatusComissao statusComissao;

    private LocalDate dataPagamento;

    @ManyToOne
    @JoinColumn(name = "fk_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "fk_parcela", nullable = false)
    private Parcela parcela;

    @ManyToOne
    @JoinColumn(name = "fk_vendedor", nullable = false)
    private Usuario usuario;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getValorComissao() {
        return valorComissao;
    }

    public void setValorComissao(Double valorComissao) {
        this.valorComissao = valorComissao;
    }

    public StatusComissao getStatusComissao() {
        return statusComissao;
    }

    public void setStatusComissao(StatusComissao statusComissao) {
        this.statusComissao = statusComissao;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Parcela getParcela() {
        return parcela;
    }

    public void setParcela(Parcela parcela) {
        this.parcela = parcela;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
