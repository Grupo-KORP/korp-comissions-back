package com.comissions.korp.repository;

import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query("""
        SELECT p.usuario.idUsuario, COUNT(p)
        FROM Pedido p
        GROUP BY p.usuario.idUsuario
    """)
    List<Object[]> contarPedidosPorVendedor(); // obj[0]=Integer, obj[1]=Long

    Integer countByCliente(Cliente cliente);

    List<Pedido> findByUsuario_IdUsuarioAndAtivoTrueAndStatusPedidoAndDataPedidoBetweenOrderByDataPedidoDescIdPedidoDesc(
            Integer idUsuario,
            String statusPedido,
            LocalDate inicio,
            LocalDate fim
    );

    @Query(value = """
        SELECT DISTINCT p
        FROM Pedido p
        LEFT JOIN Comissao c ON c.pedido = p AND c.parcela.dataVencimento BETWEEN :inicio AND :fim
        WHERE p.usuario.idUsuario = :idVendedor
          AND p.ativo = true
          AND (
                (p.statusPedido = 'EM_ANDAMENTO' AND p.dataPedido BETWEEN :inicio AND :fim)
                OR c.id IS NOT NULL
          )
          AND (
                :statusFiltro IS NULL
                OR (:statusFiltro = 'PENDENTE' AND c.statusComissao = com.comissions.korp.entity.ENUM.StatusComissao.PENDENTE)
                OR (:statusFiltro = 'LIBERADA' AND c.statusComissao IN (
                        com.comissions.korp.entity.ENUM.StatusComissao.LIBERADA,
                        com.comissions.korp.entity.ENUM.StatusComissao.PAGA))
                OR (:statusFiltro = 'PAGA' AND c.statusComissao = com.comissions.korp.entity.ENUM.StatusComissao.PAGA)
          )
        """,
            countQuery = """
        SELECT COUNT(DISTINCT p)
        FROM Pedido p
        LEFT JOIN Comissao c ON c.pedido = p AND c.parcela.dataVencimento BETWEEN :inicio AND :fim
        WHERE p.usuario.idUsuario = :idVendedor
          AND p.ativo = true
          AND (
                (p.statusPedido = 'EM_ANDAMENTO' AND p.dataPedido BETWEEN :inicio AND :fim)
                OR c.id IS NOT NULL
          )
          AND (
                :statusFiltro IS NULL
                OR (:statusFiltro = 'PENDENTE' AND c.statusComissao = com.comissions.korp.entity.ENUM.StatusComissao.PENDENTE)
                OR (:statusFiltro = 'LIBERADA' AND c.statusComissao IN (
                        com.comissions.korp.entity.ENUM.StatusComissao.LIBERADA,
                        com.comissions.korp.entity.ENUM.StatusComissao.PAGA))
                OR (:statusFiltro = 'PAGA' AND c.statusComissao = com.comissions.korp.entity.ENUM.StatusComissao.PAGA)
          )
        """)
    Page<Pedido> buscarPedidosDoPainel(
            @Param("idVendedor") Integer idVendedor,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("statusFiltro") String statusFiltro,
            Pageable pageable
    );
}



