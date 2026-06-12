package com.comissions.korp.repository;

import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Distribuidor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    boolean existsByTelefone(String telefone);

    boolean existsByEmail(String email);

    @Query("""
    SELECT c
    FROM Cliente c
    WHERE c.ativo = true
    AND (
        :busca IS NULL OR
        LOWER(c.razaoSocial) LIKE LOWER(CONCAT('%', :busca, '%')) OR
        LOWER(c.nomeFantasia) LIKE LOWER(CONCAT('%', :busca, '%')) OR
        LOWER(c.email) LIKE LOWER(CONCAT('%', :busca, '%'))
    )
""")
    Page<Cliente> findClientesComFiltro(
            @Param("busca") String busca,
            Pageable pageable
    );
}
