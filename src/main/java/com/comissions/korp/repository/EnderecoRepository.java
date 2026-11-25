package com.comissions.korp.repository;

import com.comissions.korp.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

import java.util.List;
import java.util.Optional;

public interface EnderecoRepository extends JpaRepository<Endereco, Integer> {

	List<Endereco> findByCliente_IdCliente(Integer idCliente);

	Optional<Endereco> findFirstByCliente_IdCliente(Integer idCliente);

	List<Endereco> findByDistribuidor_IdDistribuidor(Integer idDistribuidor);

    Optional<Endereco> findFirstByDistribuidor_IdDistribuidor(Integer idDistribuidor);

    /** Endereços de vários clientes numa consulta só (evita uma consulta por cliente nas listagens). */
    @Query("SELECT e FROM Endereco e JOIN FETCH e.cliente c WHERE c.idCliente IN :ids ORDER BY e.idEndereco")
    List<Endereco> buscarDosClientes(@Param("ids") Collection<Integer> ids);

    /** Endereços de vários distribuidores numa consulta só (evita uma consulta por distribuidor nas listagens). */
    @Query("SELECT e FROM Endereco e JOIN FETCH e.distribuidor d WHERE d.idDistribuidor IN :ids ORDER BY e.idEndereco")
    List<Endereco> buscarDosDistribuidores(@Param("ids") Collection<Integer> ids);
}