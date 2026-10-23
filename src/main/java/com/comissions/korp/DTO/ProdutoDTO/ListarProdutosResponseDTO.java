package com.comissions.korp.DTO.ProdutoDTO;

public class ListarProdutosResponseDTO {
    private Integer idProduto;
    private String nome;
    private String codigoProduto;
    private String pn;

    public ListarProdutosResponseDTO(Integer idProduto, String nome, String codigoProduto, String pn) {
        this.idProduto = idProduto;
        this.nome = nome;
        this.codigoProduto = codigoProduto;
        this.pn = pn;
    }

    public String getPn() {
        return pn;
    }

    public void setPn(String pn) {
        this.pn = pn;
    }

    public void setIdProduto(Integer idProduto) {
        this.idProduto = idProduto;
    }

    public Integer getIdProduto() {
        return idProduto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodigoProduto() {
        return codigoProduto;
    }

    public void setCodigoProduto(String codigoProduto) {
        this.codigoProduto = codigoProduto;
    }
}
