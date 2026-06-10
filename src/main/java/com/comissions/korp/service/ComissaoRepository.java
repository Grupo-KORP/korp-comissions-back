package com.comissions.korp.service;

import com.comissions.korp.entity.Comissao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComissaoRepository extends JpaRepository<Comissao, Integer> {
}