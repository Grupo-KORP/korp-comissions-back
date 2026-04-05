package com.comissions.korp.controller;

import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.service.ItemPedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/item_pedido")
public class ItemPedidoController {

    private ItemPedidoService itemPedidoService;

    public ItemPedidoController(ItemPedidoService itemPedidoService) {
        this.itemPedidoService = itemPedidoService;
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.status(200).body(itemPedidoService.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<Object> buscarPorPedido(@RequestParam Integer idPedido) {
        try {
            return ResponseEntity.status(200).body(itemPedidoService.buscarPorPedido(idPedido));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Object> deletar(@PathVariable Integer id) {
        try {
            itemPedidoService.deletar(id);
            return ResponseEntity.status(200).body("Item deletado com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

}
