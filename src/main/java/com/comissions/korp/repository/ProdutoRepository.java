package com.comissions.korp.repository;

import com.comissions.korp.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    List<Produto> findByNomeContains(String nome);

    List<Produto> findByIdProdutoIn(List<Integer> idsProdutos);


    @Query("""
    SELECT u FROM Produto u
    WHERE (:busca IS NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :busca, '%')))
""")
    Page<Produto> findProdutosComFiltro(@Param("busca") String busca,
                                        Pageable pageable);
}
