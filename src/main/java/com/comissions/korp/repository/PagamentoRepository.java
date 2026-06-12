package com.comissions.korp.repository;

import com.comissions.korp.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {

    Optional<Pagamento> findByPedido_IdPedido(Integer idPedido);

    boolean existsByPedido_IdPedido(Integer idPedido);
}
