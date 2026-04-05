package com.comissions.korp.controller;


import com.comissions.korp.DTO.PedidoDTO.PedidoRequest;
import com.comissions.korp.DTO.PedidoDTO.PedidoResponse;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<PedidoResponse> cadastrarPedido(@RequestBody PedidoRequest pedidoRequest) {
            return ResponseEntity.status(201).body(pedidoService.cadastrarPedido(pedidoRequest));
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
            return ResponseEntity.status(200).body(pedidoService.atualizarPedido(id, pedidoRequest));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<?> deletarPedido(@PathVariable Integer id) {
            pedidoService.deletarPedido(id);
            return ResponseEntity.status(204).body("Pedido deletado com sucesso!");
        }
    }
