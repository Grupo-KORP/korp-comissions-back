package com.comissions.korp.repository;

import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.entity.ENUM.StatusParcela;
import com.comissions.korp.entity.Pedido;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query("""
        SELECT p.usuario.idUsuario, COUNT(p)
        FROM Pedido p
        GROUP BY p.usuario.idUsuario
    """)
    List<Object[]> contarPedidosPorVendedor();

    Integer countByCliente(Cliente cliente);

    List<Pedido> findByUsuario_IdUsuarioAndAtivoTrueAndStatusPedidoAndDataPedidoBetweenOrderByDataPedidoDescIdPedidoDesc(
            Integer idUsuario,
            String statusPedido,
            LocalDate inicio,
            LocalDate fim
    );

    /**
     * Um pedido entra no painel quando:
     *  - tem comissão do vendedor, com status dentro do filtro, cuja parcela vence no período
     *    (e a parcela não está cancelada); OU
     *  - (quando o filtro permite) está EM_ANDAMENTO, é do vendedor e foi feito no período.
     */

    String FILTRO_PAINEL = """
        p.ativo = true
        AND (
            EXISTS (
                SELECT 1
                FROM Comissao c
                WHERE c.pedido = p
                  AND c.usuario.idUsuario = :idVendedor
                  AND c.statusComissao IN :statusComissoes
                  AND c.parcela.dataVencimento BETWEEN :inicio AND :fim
                  AND c.parcela.statusParcela <> :parcelaCancelada
            )
            OR (
                :incluirVendaDoPeriodo = true
                AND p.usuario.idUsuario = :idVendedor
                AND p.dataPedido BETWEEN :inicio AND :fim
            )
            OR (
                :incluirEmAndamento = true
                AND p.usuario.idUsuario = :idVendedor
                AND p.statusPedido = 'EM_ANDAMENTO'
                AND p.dataPedido BETWEEN :inicio AND :fim
            )
        )
        """;

    /**
     * Todos os pedidos do filtro, sem paginar: a tabela pagina por linha (venda + parcelas),
     * e o número de linhas de cada pedido só é conhecido depois de carregar as comissões.
     */
    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente WHERE " + FILTRO_PAINEL)
    List<Pedido> listarPedidosDoPainel(
            @Param("idVendedor") Integer idVendedor,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("statusComissoes") List<StatusComissao> statusComissoes,
            @Param("parcelaCancelada") StatusParcela parcelaCancelada,
            @Param("incluirEmAndamento") boolean incluirEmAndamento,
            @Param("incluirVendaDoPeriodo") boolean incluirVendaDoPeriodo,
            Sort sort
    );

    /** Vendas do período: pedidos ativos do vendedor feitos entre as datas (qualquer status do pedido). */
    String FILTRO_VENDAS_DO_PERIODO = """
        p.ativo = true
        AND p.usuario.idUsuario = :idVendedor
        AND p.dataPedido BETWEEN :inicio AND :fim
        """;

    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente WHERE " + FILTRO_VENDAS_DO_PERIODO)
    List<Pedido> listarVendasDoPeriodo(
            @Param("idVendedor") Integer idVendedor,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            Sort sort
    );

    /** Base do card "Total de Vendas" e da tendência de vendas: só a data do pedido conta. */
    @Query("SELECT COUNT(p) FROM Pedido p WHERE " + FILTRO_VENDAS_DO_PERIODO)
    long contarVendasDoPeriodo(
            @Param("idVendedor") Integer idVendedor,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    Optional<Pedido> findByIdPedidoAndAtivoTrue(Integer idPedido);

    boolean existsByIdPedidoAndUsuario_IdUsuario(Integer idPedido, Integer idUsuario);
}



