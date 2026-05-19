package com.comissions.korp.repository;

import com.comissions.korp.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query("""
        SELECT p.usuario.idUsuario, COUNT(p)
        FROM Pedido p
        GROUP BY p.usuario.idUsuario
    """)
    List<Object[]> contarPedidosPorVendedor(); // obj[0]=Integer, obj[1]=Long
}



