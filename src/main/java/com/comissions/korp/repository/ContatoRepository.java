package com.comissions.korp.repository;

import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Contato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContatoRepository extends JpaRepository<Contato, Integer> {
    List<Contato> findByCliente(Cliente cliente);
}
