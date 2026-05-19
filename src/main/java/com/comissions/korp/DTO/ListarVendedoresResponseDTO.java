package com.comissions.korp.DTO;

public class ListarVendedoresResponseDTO {

    private String  nome;
    private String  email;
    private String  telefone;
    private Double  percentualComissao;
    private Integer numeroDePedidos;
    private Integer vendasEfetivadas;   // fixo 0 por ora
    private Integer comissoesPendentes; // fixo 0 por ora

    public ListarVendedoresResponseDTO() {}

    public ListarVendedoresResponseDTO(
            String nome,
            String email,
            String telefone,
            Double percentualComissao,
            Integer numeroDePedidos,
            Integer vendasEfetivadas,
            Integer comissoesPendentes) {
        this.nome               = nome;
        this.email              = email;
        this.telefone           = telefone;
        this.percentualComissao = percentualComissao;
        this.numeroDePedidos    = numeroDePedidos;
        this.vendasEfetivadas   = vendasEfetivadas;
        this.comissoesPendentes = comissoesPendentes;
    }

    public String  getNome()               { return nome; }
    public String  getEmail()              { return email; }
    public String  getTelefone()           { return telefone; }
    public Double  getPercentualComissao() { return percentualComissao; }
    public Integer getNumeroDePedidos()    { return numeroDePedidos; }
    public Integer getVendasEfetivadas()   { return vendasEfetivadas; }
    public Integer getComissoesPendentes() { return comissoesPendentes; }
}