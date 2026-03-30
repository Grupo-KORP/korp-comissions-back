package com.comissions.korp.repository;

import com.comissions.korp.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByEmail(String email);
    boolean existsByNome(String nome);

    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByNome(String nome);
}
