package com.comissions.korp.service;


import com.comissions.korp.DTO.ProdutoDTO.ProdutoRequest;
import com.comissions.korp.DTO.ProdutoDTO.ProdutoResponse;
import com.comissions.korp.entity.Produto;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class ProdutoService {

    private ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ProdutoResponse cadastrarProduto(ProdutoRequest produtoRequest){
        Produto produto = convertToEntity(produtoRequest);
        Produto produtoSalvo = produtoRepository.save(produto);
        return convertToResponseDTO(produtoSalvo);
    }

    public List<ProdutoResponse> listarProdutos(){
        return produtoRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public ProdutoResponse buscarDtoPorId(Integer id){
        Produto produto = buscarProdutoPorId(id);
        return convertToResponseDTO(produto);
    }

    public Produto buscarProdutoPorId(Integer id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado("Produto não encontrado"));
    }

    public List<ProdutoResponse> buscarPorNome(String nome){
        List<ProdutoResponse> produtos = produtoRepository.findByNomeContains(nome)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        if (produtos.isEmpty()) {
            throw new RuntimeException("Nenhum produto encontrado com esse nome");
        }
        return produtos;
    }

    @Transactional
    public ProdutoResponse atualizarProduto(Integer id, ProdutoRequest produtoRequest){
        Produto produto = buscarProdutoPorId(id);

        produto.setNome(produtoRequest.getNome());
        produto.setDescricao(produtoRequest.getDescricao());
        Produto produtoAtt = produtoRepository.save(produto);
        return convertToResponseDTO(produtoAtt);
    }

    @Transactional
    public void deletarProduto(Integer id){
        buscarProdutoPorId(id);

        produtoRepository.deleteById(id);
    }

    public Produto convertToEntity(ProdutoRequest dto) {
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        return produto;
    }

    public ProdutoResponse convertToResponseDTO(Produto produto) {
        ProdutoResponse dto = new ProdutoResponse();
        dto.setIdProduto(produto.getIdProduto());
        dto.setNome(produto.getNome() != null ? produto.getNome() : "Não informado");
        dto.setDescricao(produto.getDescricao() != null ? produto.getDescricao() : "Não informado");
        return dto;
    }
}
