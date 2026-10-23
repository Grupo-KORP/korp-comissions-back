package com.comissions.korp.repository;

import com.comissions.korp.DTO.HomeFinanceiroDTO.AgregadoMensalDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.ComissaoPedidoDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.RankingVendedorDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.TotalQuantidadeDTO;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ENUM.StatusParcela;
import com.comissions.korp.entity.Parcela;
import com.comissions.korp.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Consultas somente leitura do painel financeiro. Ficam separadas dos repositórios de entidade
 * porque cruzam Pedido, Parcela e Comissão sem filtrar por vendedor.
 */
public interface FinanceiroRepository extends Repository<Pedido, Integer> {

    String PEDIDOS_DO_PERIODO = "p.ativo = true AND p.dataPedido BETWEEN :inicio AND :fim";

    /** Faturamento e quantidade de vendas por mês (cards, tendência e gráfico de evolução). */
    @Query("""
        SELECT new com.comissions.korp.DTO.HomeFinanceiroDTO.AgregadoMensalDTO(
            YEAR(p.dataPedido), MONTH(p.dataPedido), COUNT(p), SUM(p.valorTotalCliente)
        )
        FROM Pedido p
        WHERE p.ativo = true
          AND p.dataPedido BETWEEN :inicio AND :fim
        GROUP BY YEAR(p.dataPedido), MONTH(p.dataPedido)
    """)
    List<AgregadoMensalDTO> totalizarVendasPorMes(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    /** Parcelas com vencimento no período nos status informados (pagamentos pendentes). */
    @Query("""
        SELECT new com.comissions.korp.DTO.HomeFinanceiroDTO.TotalQuantidadeDTO(
            COUNT(pa), SUM(pa.valorParcela)
        )
        FROM Parcela pa
        WHERE pa.statusParcela IN :statusParcelas
          AND pa.dataVencimento BETWEEN :inicio AND :fim
          AND pa.pagamento.pedido.ativo = true
    """)
    TotalQuantidadeDTO totalizarParcelas(
            @Param("statusParcelas") List<StatusParcela> statusParcelas,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    /** Comissões cuja parcela vence no período: quantidade de vendas distintas e soma. */
    @Query("""
        SELECT new com.comissions.korp.DTO.HomeFinanceiroDTO.TotalQuantidadeDTO(
            COUNT(DISTINCT c.pedido.idPedido), SUM(c.valorComissao)
        )
        FROM Comissao c
        WHERE c.statusComissao IN :statusComissoes
          AND c.pedido.ativo = true
          AND c.parcela.dataVencimento BETWEEN :inicio AND :fim
          AND c.parcela.statusParcela <> :parcelaCancelada
    """)
    TotalQuantidadeDTO totalizarComissoes(
            @Param("statusComissoes") List<StatusComissao> statusComissoes,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("parcelaCancelada") StatusParcela parcelaCancelada
    );

    /** Ranking de comissão por vendedor no período; o limite vem do Pageable. */
    @Query("""
        SELECT new com.comissions.korp.DTO.HomeFinanceiroDTO.RankingVendedorDTO(
            c.usuario.idUsuario, c.usuario.nome, SUM(c.valorComissao), COUNT(DISTINCT c.pedido.idPedido)
        )
        FROM Comissao c
        WHERE c.statusComissao IN :statusComissoes
          AND c.pedido.ativo = true
          AND c.parcela.dataVencimento BETWEEN :inicio AND :fim
          AND c.parcela.statusParcela <> :parcelaCancelada
        GROUP BY c.usuario.idUsuario, c.usuario.nome
        ORDER BY SUM(c.valorComissao) DESC, c.usuario.nome ASC
    """)
    List<RankingVendedorDTO> rankearVendedoresPorComissao(
            @Param("statusComissoes") List<StatusComissao> statusComissoes,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("parcelaCancelada") StatusParcela parcelaCancelada,
            Pageable pageable
    );

    /** Página de pedidos feitos no período (tabela de últimos pedidos). */
    @Query(
            value = "SELECT p FROM Pedido p JOIN FETCH p.usuario JOIN FETCH p.cliente WHERE " + PEDIDOS_DO_PERIODO,
            countQuery = "SELECT COUNT(p) FROM Pedido p WHERE " + PEDIDOS_DO_PERIODO
    )
    Page<Pedido> buscarPedidosDoPeriodo(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            Pageable pageable
    );

    /** Parcelas (com o pagamento) dos pedidos da página, em uma única query. */
    @Query("""
        SELECT pa
        FROM Parcela pa
        JOIN FETCH pa.pagamento pg
        WHERE pg.pedido.idPedido IN :idsPedido
        ORDER BY pa.numeroParcela ASC
    """)
    List<Parcela> buscarParcelasDosPedidos(@Param("idsPedido") List<Integer> idsPedido);

    /** Comissão total por pedido, ignorando as canceladas. */
    @Query("""
        SELECT new com.comissions.korp.DTO.HomeFinanceiroDTO.ComissaoPedidoDTO(
            c.pedido.idPedido, SUM(c.valorComissao)
        )
        FROM Comissao c
        WHERE c.pedido.idPedido IN :idsPedido
          AND c.statusComissao <> :comissaoCancelada
        GROUP BY c.pedido.idPedido
    """)
    List<ComissaoPedidoDTO> somarComissoesPorPedido(
            @Param("idsPedido") List<Integer> idsPedido,
            @Param("comissaoCancelada") StatusComissao comissaoCancelada
    );
}
