package com.comissions.korp.DTO.RascunhoDTO;

import java.time.LocalDateTime;

public class RascunhoResumoResponse {

    private Integer id;
    private String nomeCliente;
    private String nomeDistribuidor;
    private Integer qtdProdutos;
    private String metodoPagamento;
    private LocalDateTime dtAtualizacao;
    private LocalDateTime dtExpiracao;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getNomeDistribuidor() {
        return nomeDistribuidor;
    }

    public void setNomeDistribuidor(String nomeDistribuidor) {
        this.nomeDistribuidor = nomeDistribuidor;
    }

    public Integer getQtdProdutos() {
        return qtdProdutos;
    }

    public void setQtdProdutos(Integer qtdProdutos) {
        this.qtdProdutos = qtdProdutos;
    }

    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public LocalDateTime getDtAtualizacao() {
        return dtAtualizacao;
    }

    public void setDtAtualizacao(LocalDateTime dtAtualizacao) {
        this.dtAtualizacao = dtAtualizacao;
    }

    public LocalDateTime getDtExpiracao() {
        return dtExpiracao;
    }

    public void setDtExpiracao(LocalDateTime dtExpiracao) {
        this.dtExpiracao = dtExpiracao;
    }
}
