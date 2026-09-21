package com.comissions.korp.service;

import com.comissions.korp.DTO.HomeVendedorDTO.ResumoStatusDTO;
import com.comissions.korp.entity.Comissao;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ENUM.StatusParcela;
import com.comissions.korp.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ComissaoRepository extends JpaRepository<Comissao, Integer> {

    // ─── Já existiam (mantidos) ────────────────────────────────────────────────

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

    // ─── Novos (painel paginado) ───────────────────────────────────────────────

    /** Todas as comissões do vendedor nos pedidos da página atual (uma query para as N vendas). */
    @Query("""
        SELECT c
        FROM Comissao c
        JOIN FETCH c.pedido p
        JOIN FETCH c.parcela parcela
        JOIN FETCH parcela.pagamento pagamento
        WHERE p.idPedido IN :idsPedido
          AND c.usuario.idUsuario = :idUsuario
    """)
    List<Comissao> buscarComissoesDosPedidos(
            @Param("idsPedido") List<Integer> idsPedido,
            @Param("idUsuario") Integer idUsuario
    );

    /** Quantidade e soma de comissões por status no período (cards, projeção e tendência). */
    @Query("""
        SELECT new com.comissions.korp.DTO.HomeVendedorDTO.ResumoStatusDTO(
            c.statusComissao, COUNT(c), SUM(c.valorComissao)
        )
        FROM Comissao c
        WHERE c.usuario.idUsuario = :idUsuario
          AND c.pedido.ativo = true
          AND c.parcela.dataVencimento BETWEEN :inicio AND :fim
          AND c.parcela.statusParcela <> :parcelaCancelada
        GROUP BY c.statusComissao
    """)
    List<ResumoStatusDTO> resumirPorStatus(
            @Param("idUsuario") Integer idUsuario,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("parcelaCancelada") StatusParcela parcelaCancelada
    );

    /** Comissões do período por status (lista de parcelas liberadas da coluna direita). */
    @Query("""
        SELECT c
        FROM Comissao c
        JOIN FETCH c.pedido p
        JOIN FETCH c.parcela parcela
        JOIN FETCH parcela.pagamento pagamento
        WHERE c.usuario.idUsuario = :idUsuario
          AND p.ativo = true
          AND c.statusComissao IN :statusComissoes
          AND parcela.dataVencimento BETWEEN :inicio AND :fim
          AND parcela.statusParcela <> :parcelaCancelada
        ORDER BY parcela.dataVencimento ASC, p.idPedido DESC, parcela.numeroParcela ASC
    """)
    List<Comissao> buscarComissoesPorStatusNoPeriodo(
            @Param("idUsuario") Integer idUsuario,
            @Param("statusComissoes") List<StatusComissao> statusComissoes,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("parcelaCancelada") StatusParcela parcelaCancelada
    );

    /** Usado para validar que o vendedor pode ver o detalhe de uma venda. */
    boolean existsByPedido_IdPedidoAndUsuario_IdUsuario(Integer idPedido, Integer idUsuario);
}