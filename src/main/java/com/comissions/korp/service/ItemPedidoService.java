package com.comissions.korp.service;

import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoResponse;
import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoResumoResponse;
import com.comissions.korp.DTO.PedidoDTO.PedidoResumidoResponse;
import com.comissions.korp.DTO.ProdutoDTO.ProdutoResponse;
import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.exception.OperacaoNaoPermitida;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.RequisicaoInvalida;
import com.comissions.korp.repository.ItemPedidoRepository;
import com.comissions.korp.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;


@Service
@Transactional(readOnly = true)
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

    public ItemPedidoResponse buscarDtoPorId(Integer id, Integer idVendedor) {
        ItemPedido itemPedido = buscarItemDoVendedor(id, idVendedor);
        return convertToItemPedidoResponse(itemPedido);
    }

    /** Item de pedido de outro vendedor ou de pedido excluído responde igual a item inexistente (404). */
    private ItemPedido buscarItemDoVendedor(Integer id, Integer idVendedor) {
        ItemPedido item = buscarItemPedidoPorId(id);
        if (!pertenceAoVendedor(item.getPedido(), idVendedor)) {
            throw new RecursoNaoEncontrado("Item não encontrado com id: " + id);
        }
        return item;
    }

    private boolean pertenceAoVendedor(Pedido pedido, Integer idVendedor) {
        return pedido != null && Boolean.TRUE.equals(pedido.getAtivo())
                && pedido.getUsuario() != null
                && pedido.getUsuario().getIdUsuario().equals(idVendedor);
    }

    public ItemPedido buscarItemPedidoPorId(Integer id) {
        return itemPedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado("Item não encontrado com id: " + id));
    }

    public List<ItemPedidoResponse> buscarPorPedido(Integer idPedido, Integer idVendedor) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .filter(encontrado -> pertenceAoVendedor(encontrado, idVendedor))
                .orElseThrow(() -> new RecursoNaoEncontrado("Pedido não encontrado com id: " + idPedido));

        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedido);

        if (itens.isEmpty()) {
            throw new RecursoNaoEncontrado("Nenhum item encontrado para o pedido: " + idPedido);
        }

        return itens.stream()
                .map(this::convertToItemPedidoResponse)
                .collect(Collectors.toList());
    }

    /**
     * Exclui um item de pedido em andamento e recalcula os totais do pedido.
     * Pedido aprovado já gerou parcelas e comissões, então não pode mudar; e o pedido precisa
     * manter pelo menos um item.
     */
    @Transactional
    public void deletar(Integer id, Integer idVendedor) {
        ItemPedido item = buscarItemDoVendedor(id, idVendedor);
        Pedido pedido = item.getPedido();

        if (!"EM_ANDAMENTO".equals(pedido.getStatusPedido())) {
            throw new OperacaoNaoPermitida("Pedido aprovado não pode ser alterado.");
        }

        List<ItemPedido> restantes = itemPedidoRepository.findByPedido(pedido).stream()
                .filter(outro -> !id.equals(outro.getIdItemPedido()))
                .toList();
        if (restantes.isEmpty()) {
            throw new RequisicaoInvalida("O pedido precisa ter pelo menos um item. Adicione outro item antes de excluir este.");
        }

        itemPedidoRepository.deleteById(id);

        pedido.setValorTotalDistr(somar(restantes, ItemPedido::getVlrTotalDistr));
        pedido.setValorTotalCliente(somar(restantes, ItemPedido::getVlrTotalCliente));
        pedidoRepository.save(pedido);
    }

    private BigDecimal somar(List<ItemPedido> itens, java.util.function.Function<ItemPedido, BigDecimal> valor) {
        return itens.stream()
                .map(valor)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public ItemPedidoResumoResponse convertToItemPedidoResumoResponse(ItemPedido itemPedido) {
        ItemPedidoResumoResponse itemResponse = new ItemPedidoResumoResponse();
        itemResponse.setIdItemPedido(itemPedido.getIdItemPedido());
        itemResponse.setQuantidade(itemPedido.getQuantidade() != null ? itemPedido.getQuantidade() : 0);
        itemResponse.setVlrUnitDistr(itemPedido.getVlrUnitDistr() != null ? itemPedido.getVlrUnitDistr() : BigDecimal.ZERO);
        itemResponse.setVlrTotalDistr(itemPedido.getVlrTotalDistr() != null ? itemPedido.getVlrTotalDistr() : BigDecimal.ZERO);
        itemResponse.setVlrUnitCliente(itemPedido.getVlrUnitCliente() != null ? itemPedido.getVlrUnitCliente() : BigDecimal.ZERO);
        itemResponse.setVlrTotalCliente(itemPedido.getVlrTotalCliente() != null ? itemPedido.getVlrTotalCliente() : BigDecimal.ZERO);

        ProdutoResponse produtoResponse = new ProdutoResponse();
        produtoResponse.setIdProduto(itemPedido.getProduto().getIdProduto());
        produtoResponse.setNome(itemPedido.getProduto().getNome() != null ? itemPedido.getProduto().getNome() : "Não informado");
        produtoResponse.setPn(itemPedido.getProduto().getPartNumber());

        itemResponse.setProduto(produtoResponse);
        return itemResponse;
    }

    public ItemPedidoResponse populateResponseFromItemPedido(ItemPedido itemPedido){
        ItemPedidoResponse itemPedidoResponse = new ItemPedidoResponse();
        itemPedidoResponse.setIdItemPedido(itemPedido.getIdItemPedido());
        itemPedidoResponse.setQuantidade(itemPedido.getQuantidade() != null ? itemPedido.getQuantidade() : 0);
        itemPedidoResponse.setVlrUnitDistr(itemPedido.getVlrUnitDistr() != null ? itemPedido.getVlrUnitDistr() : BigDecimal.ZERO);
        itemPedidoResponse.setVlrTotalDistr(itemPedido.getVlrTotalDistr() != null ? itemPedido.getVlrTotalDistr() : BigDecimal.ZERO);
        itemPedidoResponse.setVlrUnitCliente(itemPedido.getVlrUnitCliente() != null ? itemPedido.getVlrUnitCliente() : BigDecimal.ZERO);
        itemPedidoResponse.setVlrTotalCliente(itemPedido.getVlrTotalCliente() != null ? itemPedido.getVlrTotalCliente() : BigDecimal.ZERO);

        return itemPedidoResponse;
    }

    public PedidoResumidoResponse populatePedidoResumidoResponseFromItemPedido(ItemPedido itemPedido) {
        PedidoResumidoResponse pedidoResumo = new PedidoResumidoResponse();
        pedidoResumo.setIdPedido(itemPedido.getPedido().getIdPedido());
        pedidoResumo.setDataPedido(itemPedido.getPedido().getDataPedido());
        pedidoResumo.setNumeroNotaDistribuidor(itemPedido.getPedido().getNumeroNotaDistribuidor() != null ? itemPedido.getPedido().getNumeroNotaDistribuidor() : null);
        pedidoResumo.setValorTotalDistr(itemPedido.getPedido().getValorTotalRevenda() != null ? itemPedido.getPedido().getValorTotalRevenda() : BigDecimal.ZERO);
        pedidoResumo.setValorTotalCliente(itemPedido.getPedido().getValorTotalFaturamento() != null ? itemPedido.getPedido().getValorTotalFaturamento() : BigDecimal.ZERO);
        pedidoResumo.setStatusPedido(itemPedido.getPedido().getStatusPedido() != null ? itemPedido.getPedido().getStatusPedido() : "Não informado");

        return pedidoResumo;
    }
}
