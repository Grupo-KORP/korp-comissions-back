package com.comissions.korp.service;

import com.comissions.korp.DTO.ProdutoDTO.ListarProdutosResponseDTO;
import com.comissions.korp.DTO.ProdutoDTO.ProdutoRequest;
import com.comissions.korp.DTO.ProdutoDTO.ProdutoResponse;
import com.comissions.korp.entity.Produto;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.ProdutoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }



    @Transactional
    public ProdutoResponse cadastrarProduto(ProdutoRequest produtoRequest) {
        Produto produto = convertToEntity(produtoRequest);

        Produto produtoSalvo = produtoRepository.save(produto);

        produtoSalvo.setCodigoProduto("#P" + String.format("%04d", produtoSalvo.getIdProduto()));

        Produto produtoFinal = produtoRepository.save(produtoSalvo);

        return convertToResponseDTO(produtoFinal);
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

        if (produtoRequest.getNome() != null && !produtoRequest.getNome().isBlank()) {
            produto.setNome(produtoRequest.getNome());
        }
        if (produtoRequest.getAtivo() != null) {
            produto.setAtivo(produtoRequest.getAtivo());
        }

        Produto produtoAtt = produtoRepository.save(produto);
        return convertToResponseDTO(produtoAtt);
    }


    @Transactional
    public void deletarProduto(Integer id){
        Produto produto = buscarProdutoPorId(id);

        produto.setAtivo(false);

        produtoRepository.save(produto);
    }

    public Produto convertToEntity(ProdutoRequest dto) {
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        return produto;
    }

    public ProdutoResponse convertToResponseDTO(Produto produto) {
        ProdutoResponse dto = new ProdutoResponse();
        dto.setIdProduto(produto.getIdProduto());
        dto.setNome(produto.getNome());
        dto.setCodigoProduto(produto.getCodigoProduto());
        return dto;
    }


    /**
     * Lista todos os Vendedores com paginação
     */
    public Page<ListarProdutosResponseDTO> listarTodosProdutos(String busca, Pageable pageable) {

        String filtro = (busca != null && !busca.isBlank()) ? busca : null;

        Page<Produto> paginaProdutos = produtoRepository.findProdutosComFiltro(filtro, pageable);

        return paginaProdutos.map(produto -> new ListarProdutosResponseDTO(
                produto.getIdProduto(),
                produto.getNome(),
                produto.getCodigoProduto()
        ));
    }
}
