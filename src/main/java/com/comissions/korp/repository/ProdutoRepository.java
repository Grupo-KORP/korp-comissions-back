package com.comissions.korp.repository;

import com.comissions.korp.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    List<Produto> findByNomeContains(String nome);

    List<Produto> findByIdProdutoIn(List<Integer> idsProdutos);
}
