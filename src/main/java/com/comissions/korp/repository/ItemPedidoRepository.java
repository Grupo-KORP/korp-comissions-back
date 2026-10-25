package com.comissions.korp.repository;

import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Integer> {

    List<ItemPedido> findByPedido(Pedido pedido);

    @Query("""
        SELECT ip
        FROM ItemPedido ip
        JOIN FETCH ip.produto
        WHERE ip.pedido.idPedido IN :idsPedido
    """)
    List<ItemPedido> buscarItensDosPedidos(@Param("idsPedido") List<Integer> idsPedido);

    void deleteByPedido(Pedido pedido);
}
