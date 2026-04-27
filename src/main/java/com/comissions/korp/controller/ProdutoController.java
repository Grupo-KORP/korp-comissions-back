package com.comissions.korp.controller;

import com.comissions.korp.DTO.ProdutoDTO.ProdutoRequest;
import com.comissions.korp.DTO.ProdutoDTO.ProdutoResponse;
import com.comissions.korp.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Criar produto", description = "Cadastra um novo produto com os dados informados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ProdutoResponse> cadastrarProduto(@RequestBody ProdutoRequest produtoRequest){
        return ResponseEntity.status(201).body(produtoService.cadastrarProduto(produtoRequest));
    }

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Retorna a lista de todos os produtos cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos listados com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<ProdutoResponse>> listarProdutos(){
        List<ProdutoResponse> produtos = produtoService.listarProdutos();
        return ResponseEntity.status(200).body(produtos);
    }

    @GetMapping("/buscar/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Retorna um produto com base no ID informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ProdutoResponse> busccarPorId(@PathVariable Integer id){
        return ResponseEntity.status(200).body(produtoService.buscarDtoPorId(id));
    }

    @GetMapping("/buscar/nome/{nome}")
    @Operation(summary = "Buscar produtos por nome", description = "Retorna produtos que correspondem ao nome informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos encontrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nome inválido"),
            @ApiResponse(responseCode = "404", description = "Produtos não encontrados"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<ProdutoResponse>> busccarPorNome(@PathVariable String nome){
        return ResponseEntity.status(200).body(produtoService.buscarPorNome(nome));
    }

    @PatchMapping("/atualizar/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto existente pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ProdutoResponse> atualizarProduto(@PathVariable Integer id, @RequestBody ProdutoRequest produtoRequest){
        return ResponseEntity.status(200).body(produtoService.atualizarProduto(id, produtoRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover produto", description = "Remove um produto cadastrado pelo ID informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> deletarProduto(@PathVariable Integer id){
        produtoService.deletarProduto(id);
        return ResponseEntity.status(204).build();
    }
}
