package com.comissions.korp.controller;

import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoResponse;
import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.service.ItemPedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Buscar item por ID", description = "Retorna um item de pedido com base no ID informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item encontrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ItemPedidoResponse> buscarPorId(@PathVariable Integer id) {
            return ResponseEntity.status(200).body(itemPedidoService.buscarDtoPorId(id));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar itens por pedido", description = "Retorna os itens vinculados ao pedido informado por parâmetro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Itens encontrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
            @ApiResponse(responseCode = "404", description = "Pedido ou itens não encontrados"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Object> buscarPorPedido(@RequestParam Integer idPedido) {
        try {
            return ResponseEntity.status(200).body(itemPedidoService.buscarPorPedido(idPedido));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("/deletar/{id}")
    @Operation(summary = "Remover item de pedido", description = "Remove um item de pedido pelo ID informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Object> deletar(@PathVariable Integer id) {
        try {
            itemPedidoService.deletar(id);
            return ResponseEntity.status(200).body("Item deletado com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

}
