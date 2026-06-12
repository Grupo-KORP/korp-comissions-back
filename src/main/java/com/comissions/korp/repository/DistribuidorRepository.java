package com.comissions.korp.repository;

import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistribuidorRepository extends JpaRepository<Distribuidor, Integer> {
    Optional<Distribuidor> findByCnpj(String cnpj);

    Boolean existsByCnpj(String cnpj);

    boolean existsByTelefone(String telefone);

    boolean existsByEmail(String email);

    @Query("""
    SELECT d
    FROM Distribuidor d
    WHERE d.ativo = true
    AND (
        :busca IS NULL OR
        LOWER(d.razaoSocial) LIKE LOWER(CONCAT('%', :busca, '%')) OR
        LOWER(d.nomeFantasia) LIKE LOWER(CONCAT('%', :busca, '%')) OR
        LOWER(d.email) LIKE LOWER(CONCAT('%', :busca, '%'))
    )
""")
    Page<Distribuidor> findDistribuidoresComFiltro(
            @Param("busca") String busca,
            Pageable pageable
    );
}
