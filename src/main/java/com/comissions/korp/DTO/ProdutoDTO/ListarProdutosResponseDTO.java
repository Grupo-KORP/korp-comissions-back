package com.comissions.korp.DTO.ProdutoDTO;

public class ListarProdutosResponseDTO {
    private String nome;
    private String codigoProduto;

    public ListarProdutosResponseDTO(String nome, String codigoProduto) {
        this.nome = nome;
        this.codigoProduto = codigoProduto;
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
