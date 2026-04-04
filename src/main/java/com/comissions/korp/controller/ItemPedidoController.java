package com.comissions.korp.controller;

import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.service.ItemPedidoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/item_pedido")
public class ItemPedidoController {

    private ItemPedidoService itemPedidoService;

    public ItemPedidoController(ItemPedidoService itemPedidoService) {
        this.itemPedidoService = itemPedidoService;
    }

//    public List<ItemPedido> findByPedido(Pedido p){
//        return itemPedidoService.findByPedido()
//    }
}
