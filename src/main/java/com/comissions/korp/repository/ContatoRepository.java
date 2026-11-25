package com.comissions.korp.repository;

import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Contato;
import com.comissions.korp.entity.Distribuidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ContatoRepository extends JpaRepository<Contato, Integer> {
    List<Contato> findByCliente(Cliente cliente);

    List<Contato> findByDistribuidor(Distribuidor distribuidor);

    List<Contato> findByClienteAndAtivoTrue(Cliente cliente);

    List<Contato> findByDistribuidorAndAtivoTrue(Distribuidor distribuidor);

    long countByClienteAndAtivoTrue(Cliente cliente);

    long countByDistribuidorAndAtivoTrue(Distribuidor distribuidor);

    Contato findByNomeAndEmail(String nome, String email);

    /** Contatos ativos de vários clientes numa consulta só (evita uma consulta por cliente nas listagens). */
    @Query("SELECT ct FROM Contato ct JOIN FETCH ct.cliente c WHERE c.idCliente IN :ids AND ct.ativo = true ORDER BY ct.idContato")
    List<Contato> buscarAtivosDosClientes(@Param("ids") Collection<Integer> ids);

    /** Contatos ativos de vários distribuidores numa consulta só (evita uma consulta por distribuidor nas listagens). */
    @Query("SELECT ct FROM Contato ct JOIN FETCH ct.distribuidor d WHERE d.idDistribuidor IN :ids AND ct.ativo = true ORDER BY ct.idContato")
    List<Contato> buscarAtivosDosDistribuidores(@Param("ids") Collection<Integer> ids);
}
