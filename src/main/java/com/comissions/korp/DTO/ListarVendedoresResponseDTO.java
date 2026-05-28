package com.comissions.korp.DTO;

import java.math.BigDecimal;

public class ListarVendedoresResponseDTO {

    private Integer idVendedor;
    private String  nome;
    private String  email;
    private String  telefone;
    private BigDecimal  percentualComissao;
    private Integer numeroDePedidos;
    private Integer vendasEfetivadas;   // fixo 0 por ora
    private Boolean status; // fixo 0 por ora

    public ListarVendedoresResponseDTO() {}

    public ListarVendedoresResponseDTO(
            Integer idVendedor,
            String nome,
            String email,
            String telefone,
            BigDecimal percentualComissao,
            Integer numeroDePedidos,
            Integer vendasEfetivadas,
            Boolean status) {
        this.idVendedor = idVendedor;
        this.nome               = nome;
        this.email              = email;
        this.telefone           = telefone;
        this.percentualComissao = percentualComissao;
        this.numeroDePedidos    = numeroDePedidos;
        this.vendasEfetivadas   = vendasEfetivadas;
        this.status = status ;
    }

    public String  getNome()               { return nome; }
    public String  getEmail()              { return email; }
    public String  getTelefone()           { return telefone; }
    public BigDecimal  getPercentualComissao() { return percentualComissao; }
    public Integer getNumeroDePedidos()    { return numeroDePedidos; }
    public Integer getVendasEfetivadas()   { return vendasEfetivadas; }
    public Boolean getStatus() {
        return status;
    }
    public Integer getIdVendedor() {
        return idVendedor;
    }
}