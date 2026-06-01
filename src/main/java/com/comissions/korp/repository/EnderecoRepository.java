package com.comissions.korp.repository;

import com.comissions.korp.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnderecoRepository extends JpaRepository<Endereco, Integer> {

	List<Endereco> findByCliente_IdCliente(Integer idCliente);

	Optional<Endereco> findFirstByCliente_IdCliente(Integer idCliente);

	List<Endereco> findByDistribuidor_IdDistribuidor(Integer idDistribuidor);

    Optional<Endereco> findFirstByDistribuidor_IdDistribuidor(Integer idDistribuidor);
}