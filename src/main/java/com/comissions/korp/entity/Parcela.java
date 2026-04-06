package com.comissions.korp.entity;


import com.comissions.korp.entity.ENUM.StatusParcela;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "parcela")
public class Parcela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idParcela")
    private Integer id;

    @Column(nullable = false)
    private Integer numeroParcela;

    @Column(name = "valorParcela", nullable = false, precision = 10, scale = 2)
    private Double valorParcela;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "statusParcela", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PENDENTE'")
    private StatusParcela statusParcela;

    @ManyToOne
    @JoinColumn(name = "fkPagamento", nullable = false)
    private Pagamento pagamento;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumeroParcela() {
        return numeroParcela;
    }

    public void setNumeroParcela(Integer numeroParcela) {
        this.numeroParcela = numeroParcela;
    }

    public Double getValorParcela() {
        return valorParcela;
    }

    public void setValorParcela(Double valorParcela) {
        this.valorParcela = valorParcela;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public StatusParcela getStatusParcela() {
        return statusParcela;
    }

    public void setStatusParcela(StatusParcela statusParcela) {
        this.statusParcela = statusParcela;
    }
}
