package com.comissions.korp.service;

import com.comissions.korp.entity.Comissao;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ComissaoRepository extends JpaRepository<Comissao, Integer> {

    @Query("""
        SELECT c
        FROM Comissao c
        JOIN FETCH c.pedido p
        JOIN FETCH c.parcela parcela
        JOIN FETCH parcela.pagamento pagamento
        WHERE c.usuario.idUsuario = :idUsuario
          AND p.ativo = true
          AND parcela.dataVencimento BETWEEN :inicio AND :fim
        ORDER BY parcela.dataVencimento ASC, p.idPedido DESC, parcela.numeroParcela ASC
    """)
    List<Comissao> buscarComissoesDoPainelPorVencimento(
            @Param("idUsuario") Integer idUsuario,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    List<Comissao> findByPedidoIn(List<Pedido> pedidos);

    @Query("""
    SELECT COALESCE(SUM(c.valorComissao), 0)
    FROM Comissao c
    WHERE c.usuario.idUsuario = :idVendedor
      AND c.parcela.dataVencimento BETWEEN :inicio AND :fim
      AND c.statusComissao = :status
""")
    BigDecimal somarComissoesPorStatus(@Param("idVendedor") Integer idVendedor,
                                       @Param("inicio") LocalDate inicio,
                                       @Param("fim") LocalDate fim,
                                       @Param("status") StatusComissao status);

    @Query("""
    SELECT COUNT(c)
    FROM Comissao c
    WHERE c.usuario.idUsuario = :idVendedor
      AND c.parcela.dataVencimento BETWEEN :inicio AND :fim
      AND c.statusComissao IN :status
""")
    long contarComissoesPorStatus(@Param("idVendedor") Integer idVendedor,
                                  @Param("inicio") LocalDate inicio,
                                  @Param("fim") LocalDate fim,
                                  @Param("status") List<StatusComissao> status);

    List<Comissao> findByPedidoInAndParcela_DataVencimentoBetween(List<Pedido> pedidos, LocalDate inicio, LocalDate fim);
}
