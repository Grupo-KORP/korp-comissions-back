package com.comissions.korp.controller;


import com.comissions.korp.DTO.PedidoDTO.PedidoRequest;
import com.comissions.korp.DTO.PedidoDTO.PedidoResponse;
import com.comissions.korp.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarPedido(@RequestBody PedidoRequest pedidoRequest) {
        try {
            return ResponseEntity.status(201).body(pedidoService.cadastrarPedido(pedidoRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listarPedidos() {
        try {
            return ResponseEntity.status(200).body(pedidoService.listarPedidos());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<Object> buscarPedidoPorId(@PathVariable Integer id){
        try {
            return ResponseEntity.status(200).body(pedidoService.buscarPedidoPorId(id));
        } catch (RuntimeException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<Object> atualizarPedido(
            @PathVariable Integer id,
            @RequestBody PedidoRequest pedidoRequest) {
        try {
            return ResponseEntity.status(200).body(pedidoService.atualizarPedido(id, pedidoRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Object> deletarPedido(@PathVariable Integer id) {
        try {
            pedidoService.deletarPedido(id);
            return ResponseEntity.status(200).body("Pedido deletado com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
