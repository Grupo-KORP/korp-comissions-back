package com.comissions.korp.controller;

import com.comissions.korp.DTO.ProdutoDTO.ProdutoRequest;
import com.comissions.korp.DTO.ProdutoDTO.ProdutoResponse;
import com.comissions.korp.service.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/produto")
public class ProdutoController {

    private ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<ProdutoResponse> cadastrarProduto(@RequestBody ProdutoRequest produtoRequest){
        return ResponseEntity.status(201).body(produtoService.cadastrarProduto(produtoRequest));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listarProdutos(){
        List<ProdutoResponse> produtos = produtoService.listarProdutos();
        return ResponseEntity.status(200).body(produtos);
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<ProdutoResponse> busccarPorId(@PathVariable Integer id){
        return ResponseEntity.status(200).body(produtoService.buscarDtoPorId(id));
    }

    @GetMapping("/buscar/nome/{nome}")
    public ResponseEntity<List<ProdutoResponse>> busccarPorNome(@PathVariable String nome){
        return ResponseEntity.status(200).body(produtoService.buscarPorNome(nome));
    }

    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<ProdutoResponse> atualizarProduto(@PathVariable Integer id, @RequestBody ProdutoRequest produtoRequest){
        return ResponseEntity.status(200).body(produtoService.atualizarProduto(id, produtoRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable Integer id){
        produtoService.deletarProduto(id);
        return ResponseEntity.status(204).build();
    }
}
