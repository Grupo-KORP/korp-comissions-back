package com.comissions.korp.repository;

import com.comissions.korp.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    boolean existsByTelefone(String telefone);

    boolean existsByEmail(String email);
}
