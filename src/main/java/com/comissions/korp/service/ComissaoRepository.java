package com.comissions.korp.service;

import com.comissions.korp.entity.Comissao;
import com.comissions.korp.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
