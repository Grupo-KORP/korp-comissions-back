package com.comissions.korp.repository;

import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Integer> {

    List<ItemPedido> findByPedido(Pedido pedido);

    void deleteByPedido(Pedido pedido);
}
