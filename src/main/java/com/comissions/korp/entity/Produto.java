package com.comissions.korp.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "produto")
public class    Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Integer idProduto;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "ativo", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_vendedor_cadastro")
    private Usuario vendedorCadastro;

    public Produto() {
    }

    public Integer getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Integer idProduto) {
        this.idProduto = idProduto;
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

    public Usuario getVendedorCadastro() {
        return vendedorCadastro;
    }

    public void setVendedorCadastro(Usuario vendedorCadastro) {
        this.vendedorCadastro = vendedorCadastro;
    }
}
