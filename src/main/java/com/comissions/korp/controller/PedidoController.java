package com.comissions.korp.controller;


import com.comissions.korp.DTO.PedidoDTO.PedidoRequest;
import com.comissions.korp.DTO.PedidoDTO.PedidoResponse;
import com.comissions.korp.config.utils.SecurityUtils;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private PedidoService pedidoService;
    private SecurityUtils securityUtils;

    public PedidoController(PedidoService pedidoService, SecurityUtils securityUtils) {
        this.pedidoService = pedidoService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/cadastrar")
    @Operation(summary = "Criar pedido", description = "Cadastra um novo pedido para o usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PedidoResponse> cadastrarPedido(@RequestBody PedidoRequest pedidoRequest) {
        Integer usuarioId = securityUtils.getUsuarioIdAutenticado();
            return ResponseEntity.status(201).body(pedidoService.cadastrarPedido(pedidoRequest, usuarioId));
    }

    @GetMapping("/listar")
    @Operation(summary = "Listar pedidos", description = "Retorna todos os pedidos cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos listados com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<PedidoResponse>> listarPedidos() {
            return ResponseEntity.status(200).body(pedidoService.listarPedidos());
    }

    @GetMapping("/buscar/{id}")
    @Operation(summary = "Buscar pedido por ID", description = "Retorna os dados de um pedido a partir do ID informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PedidoResponse> buscarPedidoPorId(@PathVariable Integer id){
            return ResponseEntity.status(200).body(pedidoService.buscarPedidoPorId(id));
    }

    @PutMapping("/atualizar/{id}")
    @Operation(summary = "Atualizar pedido", description = "Atualiza um pedido existente pelo ID para o usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PedidoResponse> atualizarPedido(
            @PathVariable Integer id,
            @RequestBody PedidoRequest pedidoRequest) {
            Integer vendedorId = securityUtils.getUsuarioIdAutenticado();
            return ResponseEntity.status(200).body(pedidoService.atualizarPedido(id, pedidoRequest, vendedorId));
    }

    @DeleteMapping("/deletar/{id}")
    @Operation(summary = "Remover pedido", description = "Remove um pedido existente pelo ID informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<?> deletarPedido(@PathVariable Integer id) {
            pedidoService.deletarPedido(id);
            return ResponseEntity.status(204).body("Pedido deletado com sucesso!");
        }
    }
