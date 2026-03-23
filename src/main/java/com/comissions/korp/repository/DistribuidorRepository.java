package com.comissions.korp.repository;

import com.comissions.korp.entity.Distribuidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistribuidorRepository extends JpaRepository<Distribuidor, Integer> {
    Optional<Distribuidor> findByCnpj(String cnpj);
}
