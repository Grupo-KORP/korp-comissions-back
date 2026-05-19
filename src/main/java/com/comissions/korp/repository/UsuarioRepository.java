package com.comissions.korp.repository;

import com.comissions.korp.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByEmail(String email);
    boolean existsByNome(String nome);

    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByNome(String nome);

    boolean existsByTelefone(String telefone);

    List<Usuario> findAllByRoles_Id(Integer roleId);

    @Query("""
    SELECT u FROM Usuario u JOIN u.roles r
    WHERE r.id = 3
    AND (:busca IS NULL OR
         LOWER(u.nome)  LIKE LOWER(CONCAT('%', :busca, '%')) OR
         LOWER(u.email) LIKE LOWER(CONCAT('%', :busca, '%')))
""")
    Page<Usuario> findVendedoresComFiltro(
            @Param("busca") String busca,
            Pageable pageable
    );
}
