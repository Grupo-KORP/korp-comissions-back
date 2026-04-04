package com.comissions.korp.service;

import com.comissions.korp.repository.ItemPedidoRepository;
import org.springframework.stereotype.Service;


@Service
public class ItemPedidoService {

    private ItemPedidoRepository itemPedidoRepository;

    public ItemPedidoService(ItemPedidoRepository itemPedidoRepository) {
        this.itemPedidoRepository = itemPedidoRepository;
    }
}
