package com.comissions.korp.DTO.ProdutoDTO;

public class ProdutoRequest {

    private String nome;
    private String descricao;
    private Boolean ativo;
    private Integer fkVendedorCadastro;

    public ProdutoRequest() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Integer getFkVendedorCadastro() {
        return fkVendedorCadastro;
    }

    public void setFkVendedorCadastro(Integer fkVendedorCadastro) {
        this.fkVendedorCadastro = fkVendedorCadastro;
    }
}
