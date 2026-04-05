package com.comissions.korp.service;

import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoResponse;
import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoResumoResponse;
import com.comissions.korp.DTO.PedidoDTO.PedidoResumidoResponse;
import com.comissions.korp.DTO.ProdutoDTO.ProdutoResponse;
import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.ItemPedidoRepository;
import com.comissions.korp.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ItemPedidoService {

    private final ItemPedidoRepository itemPedidoRepository;
    private final ProdutoService produtoService;
    private final PedidoRepository pedidoRepository;


    public ItemPedidoService(ItemPedidoRepository itemPedidoRepository, ProdutoService produtoService, PedidoRepository pedidoRepository) {
        this.itemPedidoRepository = itemPedidoRepository;
        this.produtoService = produtoService;
        this.pedidoRepository = pedidoRepository;
    }

    public ItemPedidoResponse convertToItemPedidoResponse(ItemPedido itemPedido) {

        ItemPedidoResponse itemPedidoResponse = populateResponseFromItemPedido(itemPedido);

        PedidoResumidoResponse pedidoResumo = populatePedidoResumidoResponseFromItemPedido(itemPedido);

        ProdutoResponse produtoResponse = produtoService.convertToResponseDTO(itemPedido.getProduto());

        itemPedidoResponse.setPedido(pedidoResumo);
        itemPedidoResponse.setProduto(produtoResponse);
        return itemPedidoResponse;
    }

    public ItemPedidoResponse buscarDtoPorId(Integer id) {
        ItemPedido itemPedido = buscarItemPedidoPorId(id);
        return convertToItemPedidoResponse(itemPedido);
    }

    public ItemPedido buscarItemPedidoPorId(Integer id) {
        return itemPedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado("Item não encontrado com id: " + id));
    }

    public List<ItemPedidoResponse> buscarPorPedido(Integer idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RecursoNaoEncontrado("Pedido não encontrado com id: " + idPedido));

        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedido);

        if (itens.isEmpty()) {
            throw new RecursoNaoEncontrado("Nenhum item encontrado para o pedido: " + idPedido);
        }

        return itens.stream()
                .map(this::convertToItemPedidoResponse)
                .collect(Collectors.toList());
    }

    public void deletar(Integer id) {
        buscarItemPedidoPorId(id);
        itemPedidoRepository.deleteById(id);
    }

    public ItemPedidoResumoResponse convertToItemPedidoResumoResponse(ItemPedido itemPedido) {
        ItemPedidoResumoResponse itemResponse = new ItemPedidoResumoResponse();
        itemResponse.setIdItemPedido(itemPedido.getIdItemPedido());
        itemResponse.setQuantidade(itemPedido.getQuantidade() != null ? itemPedido.getQuantidade() : 0);
        itemResponse.setValorUnitario(itemPedido.getValorUnitario() != null ? itemPedido.getValorUnitario() : 0.0);

        ProdutoResponse produtoResponse = new ProdutoResponse();
        produtoResponse.setIdProduto(itemPedido.getProduto().getIdProduto());
        produtoResponse.setNome(itemPedido.getProduto().getNome() != null ? itemPedido.getProduto().getNome() : "Não informado");
        produtoResponse.setDescricao(itemPedido.getProduto().getDescricao() != null ? itemPedido.getProduto().getDescricao() : "Não informado");

        itemResponse.setProduto(produtoResponse);
        return itemResponse;
    }

    public ItemPedidoResponse populateResponseFromItemPedido(ItemPedido itemPedido){
        ItemPedidoResponse itemPedidoResponse = new ItemPedidoResponse();
        itemPedidoResponse.setIdItemPedido(itemPedido.getIdItemPedido());
        itemPedidoResponse.setQuantidade(itemPedido.getQuantidade() != null ? itemPedido.getQuantidade() : 0);
        itemPedidoResponse.setValorUnitario(itemPedido.getValorUnitario() != null ? itemPedido.getValorUnitario() : 0.0);

        return itemPedidoResponse;
    }

    public PedidoResumidoResponse populatePedidoResumidoResponseFromItemPedido(ItemPedido itemPedido) {
        PedidoResumidoResponse pedidoResumo = new PedidoResumidoResponse();
        pedidoResumo.setIdPedido(itemPedido.getPedido().getIdPedido());
        pedidoResumo.setDataPedido(itemPedido.getPedido().getDataPedido());
        pedidoResumo.setNumeroNotaDistribuidor(itemPedido.getPedido().getNumeroNotaDistribuidor() != null ? itemPedido.getPedido().getNumeroNotaDistribuidor() : 0);
        pedidoResumo.setValorTotalRevenda(itemPedido.getPedido().getValorTotalRevenda() != null ? itemPedido.getPedido().getValorTotalRevenda() : 0.0);
        pedidoResumo.setValorTotalFaturamento(itemPedido.getPedido().getValorTotalFaturamento() != null ? itemPedido.getPedido().getValorTotalFaturamento() : 0.0);
        pedidoResumo.setStatusPedido(itemPedido.getPedido().getStatusPedido() != null ? itemPedido.getPedido().getStatusPedido() : "Não informado");

        return pedidoResumo;
    }
}
