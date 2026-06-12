package com.comissions.korp.DTO.HomeVendedorDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class HomeVendedorResponseDTO {

    private Integer ano;
    private Integer mes;
    private String nomeMes;
    private Integer totalVendas;
    private Integer comissoesLiberadas;
    private Integer pagamentosPendentes;
    private BigDecimal projecao;
    private Integer parcelas;
    private String tendencia;
    private List<VendaResumoDTO> vendas;
    private Map<String, DetalheVendaDTO> detalhesVenda;

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public String getNomeMes() {
        return nomeMes;
    }

    public void setNomeMes(String nomeMes) {
        this.nomeMes = nomeMes;
    }

    public Integer getTotalVendas() {
        return totalVendas;
    }

    public void setTotalVendas(Integer totalVendas) {
        this.totalVendas = totalVendas;
    }

    public Integer getComissoesLiberadas() {
        return comissoesLiberadas;
    }

    public void setComissoesLiberadas(Integer comissoesLiberadas) {
        this.comissoesLiberadas = comissoesLiberadas;
    }

    public Integer getPagamentosPendentes() {
        return pagamentosPendentes;
    }

    public void setPagamentosPendentes(Integer pagamentosPendentes) {
        this.pagamentosPendentes = pagamentosPendentes;
    }

    public BigDecimal getProjecao() {
        return projecao;
    }

    public void setProjecao(BigDecimal projecao) {
        this.projecao = projecao;
    }

    public Integer getParcelas() {
        return parcelas;
    }

    public void setParcelas(Integer parcelas) {
        this.parcelas = parcelas;
    }

    public String getTendencia() {
        return tendencia;
    }

    public void setTendencia(String tendencia) {
        this.tendencia = tendencia;
    }

    public List<VendaResumoDTO> getVendas() {
        return vendas;
    }

    public void setVendas(List<VendaResumoDTO> vendas) {
        this.vendas = vendas;
    }

    public Map<String, DetalheVendaDTO> getDetalhesVenda() {
        return detalhesVenda;
    }

    public void setDetalhesVenda(Map<String, DetalheVendaDTO> detalhesVenda) {
        this.detalhesVenda = detalhesVenda;
    }

    public static class VendaResumoDTO {
        private String id;
        private Integer idPedido;
        private String nome;
        private String cliente;
        private String comissao;
        private BigDecimal valorComissao;
        private String status;
        private String tipo;
        private List<ParcelaDTO> parcelas;
        private List<ParcelaDTO> parcelasDaVenda;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getIdPedido() {
            return idPedido;
        }

        public void setIdPedido(Integer idPedido) {
            this.idPedido = idPedido;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getCliente() {
            return cliente;
        }

        public void setCliente(String cliente) {
            this.cliente = cliente;
        }

        public String getComissao() {
            return comissao;
        }

        public void setComissao(String comissao) {
            this.comissao = comissao;
        }

        public BigDecimal getValorComissao() {
            return valorComissao;
        }

        public void setValorComissao(BigDecimal valorComissao) {
            this.valorComissao = valorComissao;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public List<ParcelaDTO> getParcelas() {
            return parcelas;
        }

        public void setParcelas(List<ParcelaDTO> parcelas) {
            this.parcelas = parcelas;
        }

        public List<ParcelaDTO> getParcelasDaVenda() {
            return parcelasDaVenda;
        }

        public void setParcelasDaVenda(List<ParcelaDTO> parcelasDaVenda) {
            this.parcelasDaVenda = parcelasDaVenda;
        }
    }

    public static class ParcelaDTO {
        private Integer idParcela;
        private String label;
        private BigDecimal valor;
        private Integer numeroParcela;
        private Integer totalParcelas;
        private LocalDate dataVencimento;
        private String status;

        public LocalDate getDataVencimento() {
            return dataVencimento;
        }

        public void setDataVencimento(LocalDate dataVencimento) {
            this.dataVencimento = dataVencimento;
        }

        public Integer getIdParcela() {
            return idParcela;
        }

        public void setIdParcela(Integer idParcela) {
            this.idParcela = idParcela;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public BigDecimal getValor() {
            return valor;
        }

        public void setValor(BigDecimal valor) {
            this.valor = valor;
        }

        public Integer getNumeroParcela() {
            return numeroParcela;
        }

        public void setNumeroParcela(Integer numeroParcela) {
            this.numeroParcela = numeroParcela;
        }

        public Integer getTotalParcelas() {
            return totalParcelas;
        }

        public void setTotalParcelas(Integer totalParcelas) {
            this.totalParcelas = totalParcelas;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class DetalheVendaDTO {
        private PessoaDTO cliente;
        private PessoaDTO distribuidor;
        private ProdutoVendaDTO produto;
        private PedidoVendaDTO pedido;

        public PessoaDTO getCliente() {
            return cliente;
        }

        public void setCliente(PessoaDTO cliente) {
            this.cliente = cliente;
        }

        public PessoaDTO getDistribuidor() {
            return distribuidor;
        }

        public void setDistribuidor(PessoaDTO distribuidor) {
            this.distribuidor = distribuidor;
        }

        public ProdutoVendaDTO getProduto() {
            return produto;
        }

        public void setProduto(ProdutoVendaDTO produto) {
            this.produto = produto;
        }

        public PedidoVendaDTO getPedido() {
            return pedido;
        }

        public void setPedido(PedidoVendaDTO pedido) {
            this.pedido = pedido;
        }
    }

    public static class PedidoVendaDTO {
        private Integer idPedido;
        private String statusPedido;
        private String numeroNotaDistribuidor;
        private String observacoes;
        private String metodoPagamento;
        private Boolean parcelado;
        private Integer quantidadeParcelas;

        public Integer getIdPedido() {
            return idPedido;
        }

        public void setIdPedido(Integer idPedido) {
            this.idPedido = idPedido;
        }

        public String getStatusPedido() {
            return statusPedido;
        }

        public void setStatusPedido(String statusPedido) {
            this.statusPedido = statusPedido;
        }

        public String getNumeroNotaDistribuidor() {
            return numeroNotaDistribuidor;
        }

        public void setNumeroNotaDistribuidor(String numeroNotaDistribuidor) {
            this.numeroNotaDistribuidor = numeroNotaDistribuidor;
        }

        public String getObservacoes() {
            return observacoes;
        }

        public void setObservacoes(String observacoes) {
            this.observacoes = observacoes;
        }

        public String getMetodoPagamento() {
            return metodoPagamento;
        }

        public void setMetodoPagamento(String metodoPagamento) {
            this.metodoPagamento = metodoPagamento;
        }

        public Boolean getParcelado() {
            return parcelado;
        }

        public void setParcelado(Boolean parcelado) {
            this.parcelado = parcelado;
        }

        public Integer getQuantidadeParcelas() {
            return quantidadeParcelas;
        }

        public void setQuantidadeParcelas(Integer quantidadeParcelas) {
            this.quantidadeParcelas = quantidadeParcelas;
        }
    }

    public static class PessoaDTO {
        private Integer id;
        private String razaoSocial;
        private String nomeFantasia;
        private String cnpj;
        private String inscricaoEstadual;
        private String telefone;
        private String cep;
        private String endereco;
        private String cidade;
        private String uf;
        private String contato;
        private String email;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getRazaoSocial() {
            return razaoSocial;
        }

        public void setRazaoSocial(String razaoSocial) {
            this.razaoSocial = razaoSocial;
        }

        public String getNomeFantasia() {
            return nomeFantasia;
        }

        public void setNomeFantasia(String nomeFantasia) {
            this.nomeFantasia = nomeFantasia;
        }

        public String getCnpj() {
            return cnpj;
        }

        public void setCnpj(String cnpj) {
            this.cnpj = cnpj;
        }

        public String getInscricaoEstadual() {
            return inscricaoEstadual;
        }

        public void setInscricaoEstadual(String inscricaoEstadual) {
            this.inscricaoEstadual = inscricaoEstadual;
        }

        public String getTelefone() {
            return telefone;
        }

        public void setTelefone(String telefone) {
            this.telefone = telefone;
        }

        public String getCep() {
            return cep;
        }

        public void setCep(String cep) {
            this.cep = cep;
        }

        public String getEndereco() {
            return endereco;
        }

        public void setEndereco(String endereco) {
            this.endereco = endereco;
        }

        public String getCidade() {
            return cidade;
        }

        public void setCidade(String cidade) {
            this.cidade = cidade;
        }

        public String getUf() {
            return uf;
        }

        public void setUf(String uf) {
            this.uf = uf;
        }

        public String getContato() {
            return contato;
        }

        public void setContato(String contato) {
            this.contato = contato;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class ProdutoVendaDTO {
        private Integer idItemPedido;
        private Integer idProduto;
        private String descricao;
        private String pn;
        private String entrega;
        private Integer quantidade;
        private BigDecimal valorUnitario;
        private BigDecimal valorTotal;
        private BigDecimal valorUnitarioFaturado;
        private BigDecimal totalFaturado;

        public Integer getIdItemPedido() {
            return idItemPedido;
        }

        public void setIdItemPedido(Integer idItemPedido) {
            this.idItemPedido = idItemPedido;
        }

        public Integer getIdProduto() {
            return idProduto;
        }

        public void setIdProduto(Integer idProduto) {
            this.idProduto = idProduto;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }

        public String getPn() {
            return pn;
        }

        public void setPn(String pn) {
            this.pn = pn;
        }

        public String getEntrega() {
            return entrega;
        }

        public void setEntrega(String entrega) {
            this.entrega = entrega;
        }

        public Integer getQuantidade() {
            return quantidade;
        }

        public void setQuantidade(Integer quantidade) {
            this.quantidade = quantidade;
        }

        public BigDecimal getValorUnitario() {
            return valorUnitario;
        }

        public void setValorUnitario(BigDecimal valorUnitario) {
            this.valorUnitario = valorUnitario;
        }

        public BigDecimal getValorTotal() {
            return valorTotal;
        }

        public void setValorTotal(BigDecimal valorTotal) {
            this.valorTotal = valorTotal;
        }

        public BigDecimal getValorUnitarioFaturado() {
            return valorUnitarioFaturado;
        }

        public void setValorUnitarioFaturado(BigDecimal valorUnitarioFaturado) {
            this.valorUnitarioFaturado = valorUnitarioFaturado;
        }

        public BigDecimal getTotalFaturado() {
            return totalFaturado;
        }

        public void setTotalFaturado(BigDecimal totalFaturado) {
            this.totalFaturado = totalFaturado;
        }
    }
}
