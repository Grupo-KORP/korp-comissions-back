package com.comissions.korp.controller;


import com.comissions.korp.DTO.PedidoDTO.PedidoRequest;
import com.comissions.korp.DTO.PedidoDTO.PedidoResponse;
import com.comissions.korp.config.utils.SecurityUtils;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.service.PedidoService;
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
    public ResponseEntity<PedidoResponse> cadastrarPedido(@RequestBody PedidoRequest pedidoRequest) {
        Integer usuarioId = securityUtils.getUsuarioIdAutenticado();
            return ResponseEntity.status(201).body(pedidoService.cadastrarPedido(pedidoRequest, usuarioId));
    }

    @GetMapping("/listar")
    public ResponseEntity<List<PedidoResponse>> listarPedidos() {
            return ResponseEntity.status(200).body(pedidoService.listarPedidos());
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<PedidoResponse> buscarPedidoPorId(@PathVariable Integer id){
            return ResponseEntity.status(200).body(pedidoService.buscarPedidoPorId(id));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<PedidoResponse> atualizarPedido(
            @PathVariable Integer id,
            @RequestBody PedidoRequest pedidoRequest) {
            Integer vendedorId = securityUtils.getUsuarioIdAutenticado();
            return ResponseEntity.status(200).body(pedidoService.atualizarPedido(id, pedidoRequest, vendedorId));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<?> deletarPedido(@PathVariable Integer id) {
            pedidoService.deletarPedido(id);
            return ResponseEntity.status(204).body("Pedido deletado com sucesso!");
        }
    }
