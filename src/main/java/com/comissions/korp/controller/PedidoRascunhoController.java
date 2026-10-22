package com.comissions.korp.controller;

import com.comissions.korp.DTO.RascunhoDTO.RascunhoRequest;
import com.comissions.korp.DTO.RascunhoDTO.RascunhoResponse;
import com.comissions.korp.DTO.RascunhoDTO.RascunhoResumoResponse;
import com.comissions.korp.config.utils.SecurityUtils;
import com.comissions.korp.service.PedidoRascunhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos/rascunhos")
public class PedidoRascunhoController {

    private final PedidoRascunhoService rascunhoService;
    private final SecurityUtils securityUtils;

    public PedidoRascunhoController(PedidoRascunhoService rascunhoService, SecurityUtils securityUtils) {
        this.rascunhoService = rascunhoService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    @Operation(summary = "Listar rascunhos", description = "Lista os rascunhos de pedido não expirados do vendedor autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rascunhos listados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Apenas vendedores acessam rascunhos")
    })
    public ResponseEntity<List<RascunhoResumoResponse>> listar() {
        return ResponseEntity.ok(rascunhoService.listar(securityUtils.getUsuarioIdAutenticado()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar rascunho", description = "Retorna o rascunho completo do vendedor autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rascunho encontrado"),
            @ApiResponse(responseCode = "404", description = "Rascunho não encontrado ou expirado")
    })
    public ResponseEntity<RascunhoResponse> buscar(@PathVariable Integer id) {
        return ResponseEntity.ok(rascunhoService.buscar(securityUtils.getUsuarioIdAutenticado(), id));
    }

    @PostMapping
    @Operation(summary = "Criar rascunho", description = "Salva um novo rascunho de pedido (máximo de 10 por vendedor).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rascunho criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Limite de rascunhos atingido")
    })
    public ResponseEntity<RascunhoResponse> criar(@RequestBody RascunhoRequest request) {
        return ResponseEntity.status(201).body(rascunhoService.criar(securityUtils.getUsuarioIdAutenticado(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar rascunho", description = "Atualiza o rascunho; exige a versão recebida ao abri-lo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rascunho atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Rascunho não encontrado ou expirado"),
            @ApiResponse(responseCode = "409", description = "Rascunho alterado em outra aba")
    })
    public ResponseEntity<RascunhoResponse> atualizar(@PathVariable Integer id, @RequestBody RascunhoRequest request) {
        return ResponseEntity.ok(rascunhoService.atualizar(securityUtils.getUsuarioIdAutenticado(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir rascunho", description = "Exclui um rascunho do vendedor autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rascunho excluído"),
            @ApiResponse(responseCode = "404", description = "Rascunho não encontrado ou expirado")
    })
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        rascunhoService.excluir(securityUtils.getUsuarioIdAutenticado(), id);
        return ResponseEntity.noContent().build();
    }
}
