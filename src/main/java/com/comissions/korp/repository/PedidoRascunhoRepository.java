package com.comissions.korp.repository;

import com.comissions.korp.entity.PedidoRascunho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRascunhoRepository extends JpaRepository<PedidoRascunho, Integer> {

    List<PedidoRascunho> findByUsuario_IdUsuarioAndDtAtualizacaoAfterOrderByDtAtualizacaoDesc(
            Integer idVendedor, LocalDateTime limiteExpiracao);

    Optional<PedidoRascunho> findByIdRascunhoAndUsuario_IdUsuarioAndDtAtualizacaoAfter(
            Integer idRascunho, Integer idVendedor, LocalDateTime limiteExpiracao);

    long countByUsuario_IdUsuarioAndDtAtualizacaoAfter(Integer idVendedor, LocalDateTime limiteExpiracao);

    long deleteByIdRascunhoAndUsuario_IdUsuario(Integer idRascunho, Integer idVendedor);

    @Modifying
    @Query("DELETE FROM PedidoRascunho r WHERE r.dtAtualizacao < :limiteExpiracao")
    int apagarExpirados(@Param("limiteExpiracao") LocalDateTime limiteExpiracao);
}
