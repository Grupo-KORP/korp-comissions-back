package com.comissions.korp.service;

import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoRequest;
import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoResumoResponse;
import com.comissions.korp.DTO.PedidoDTO.PedidoRequest;
import com.comissions.korp.DTO.PedidoDTO.PedidoResponse;
import com.comissions.korp.DTO.ProdutoDTO.ProdutoResponse;
import com.comissions.korp.entity.*;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ProdutoRepository produtoRepository;
//    private final VendedorRepository vendedorRepository;
    private final ClienteRepository clienteRepository;
    private final DistribuidorRepository distribuidorRepository;


    public PedidoService(PedidoRepository pedidoRepository
            ,ItemPedidoRepository itemPedidoRepository
            ,ProdutoRepository produtoRepository
            ,ClienteRepository clienteRepository
            ,DistribuidorRepository distribuidorRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.distribuidorRepository = distribuidorRepository;
    }

    public PedidoResponse cadastrarPedido(PedidoRequest pedidoRequest) {

//        Vendedor vendedor = vendedorRepository.findById(pedidoRequest.getFkVendedor())
//                .orElseThrow(() -> new RuntimeException("Vendedor não encontrado com id: " + pedidoRequest.getFkVendedor()));

        Cliente cliente = clienteRepository.findById(pedidoRequest.getFkCliente())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com id: " + pedidoRequest.getFkCliente()));

        Distribuidor distribuidor = distribuidorRepository.findById(pedidoRequest.getFkDistribuidor())
                .orElseThrow(() -> new RuntimeException("Distribuidor não encontrado com id: " + pedidoRequest.getFkDistribuidor()));

        Pedido pedido = new Pedido();
        pedido.setDataPedido(pedidoRequest.getDataPedido());
        pedido.setNumeroNotaDistribuidor(pedidoRequest.getNumeroNotaDistribuidor());
        pedido.setValorTotalRevenda(pedidoRequest.getValorTotalRevenda());
        pedido.setValorTotalFaturamento(pedidoRequest.getValorTotalFaturamento());
        pedido.setStatusPedido(pedidoRequest.getStatusPedido());
        pedido.setFrete(pedidoRequest.getFrete());
        pedido.setTransportadora(pedidoRequest.getTransportadora());
        pedido.setObservacoes(pedidoRequest.getObservacoes());
//        pedido.setVendedor(vendedor);
        pedido.setCliente(cliente);
        pedido.setDistribuidor(distribuidor);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        List<Integer> idsProdutos = pedidoRequest.getItens()
                .stream()
                .map(ItemPedidoRequest::getFkProduto)
                .collect(Collectors.toList());

        List<Produto> produtos = produtoRepository.findByIdProdutoIn(idsProdutos);

        Map<Integer, Produto> produtoMap = produtos.stream()
                .collect(Collectors.toMap(Produto::getIdProduto, produto -> produto));

        List<ItemPedido> itensSalvos = new ArrayList<>();
        for (ItemPedidoRequest itemRequest : pedidoRequest.getItens()) {

            Produto produto = produtoMap.get(itemRequest.getFkProduto());
            if (produto == null) {
                throw new RecursoNaoEncontrado("Produto não encontrado com id: " + itemRequest.getFkProduto());
            }
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setPedido(pedidoSalvo);
            itemPedido.setProduto(produto);
            itemPedido.setQuantidade(itemRequest.getQuantidade());
            itemPedido.setValorUnitario(itemRequest.getValorUnitario());

            itensSalvos.add(itemPedidoRepository.save(itemPedido));
        }
        return convertToPedidoResponse(pedidoSalvo, itensSalvos);
    }

    public List<PedidoResponse> listarPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        List<PedidoResponse> response = new ArrayList<>();

        for (Pedido pedido : pedidos) {
            List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedido);
            response.add(convertToPedidoResponse(pedido, itens));
        }

        return response;
    }

    public PedidoResponse buscarPedidoPorId(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado("Pedido não encontrado com id: " + id));

        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedido);
        return convertToPedidoResponse(pedido, itens);
    }

    public PedidoResponse atualizarPedido(Integer id, PedidoRequest pedidoRequest) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com id: " + id));

//        Vendedor vendedor = vendedorRepository.findById(pedidoRequest.getFkVendedor())
//                .orElseThrow(() -> new RuntimeException("Vendedor não encontrado com id: " + pedidoRequest.getFkVendedor()));

        Cliente cliente = clienteRepository.findById(pedidoRequest.getFkCliente())
                .orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com id: " + pedidoRequest.getFkCliente()));

        Distribuidor distribuidor = distribuidorRepository.findById(pedidoRequest.getFkDistribuidor())
                .orElseThrow(() -> new RecursoNaoEncontrado("Distribuidor não encontrado com id: " + pedidoRequest.getFkDistribuidor()));

        pedido.setDataPedido(pedidoRequest.getDataPedido());
        pedido.setNumeroNotaDistribuidor(pedidoRequest.getNumeroNotaDistribuidor());
        pedido.setValorTotalRevenda(pedidoRequest.getValorTotalRevenda());
        pedido.setValorTotalFaturamento(pedidoRequest.getValorTotalFaturamento());
        pedido.setStatusPedido(pedidoRequest.getStatusPedido());
        pedido.setFrete(pedidoRequest.getFrete());
        pedido.setTransportadora(pedidoRequest.getTransportadora());
        pedido.setObservacoes(pedidoRequest.getObservacoes());
//        pedido.setVendedor(vendedor);
        pedido.setCliente(cliente);
        pedido.setDistribuidor(distribuidor);

        Pedido pedidoAtualizado = pedidoRepository.save(pedido);
        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedidoAtualizado);
        return convertToPedidoResponse(pedidoAtualizado, itens);
    }

    public void deletarPedido(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com id: " + id));

        // Para deletar o pedido, precisa del tambem suas amarrações na tbl: Item_pedido
        itemPedidoRepository.deleteByPedido(pedido);
        pedidoRepository.deleteById(id);
    }

    //Convertento entidades em DTO
    private static ItemPedidoResumoResponse convertToItemResumoResponse(ItemPedido itemPedido) {
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

    private static PedidoResponse convertToPedidoResponse(Pedido pedido, List<ItemPedido> itens) {
        PedidoResponse pedidoResponse = new PedidoResponse();
        pedidoResponse.setIdPedido(pedido.getIdPedido());
        pedidoResponse.setDataPedido(pedido.getDataPedido());
        pedidoResponse.setNumeroNotaDistribuidor(pedido.getNumeroNotaDistribuidor() != null ? pedido.getNumeroNotaDistribuidor() : 0);
        pedidoResponse.setValorTotalRevenda(pedido.getValorTotalRevenda() != null ? pedido.getValorTotalRevenda() : 0.0);
        pedidoResponse.setValorTotalFaturamento(pedido.getValorTotalFaturamento() != null ? pedido.getValorTotalFaturamento() : 0.0);
        pedidoResponse.setStatusPedido(pedido.getStatusPedido() != null ? pedido.getStatusPedido() : "Não informado");
        pedidoResponse.setFrete(pedido.getFrete() != null ? pedido.getFrete() : false);
        pedidoResponse.setTransportadora(pedido.getTransportadora() != null ? pedido.getTransportadora() : "Não informado");
        pedidoResponse.setObservacoes(pedido.getObservacoes() != null ? pedido.getObservacoes() : "Não informado");

        List<ItemPedidoResumoResponse> itensResponse = itens
                .stream()
                .map(PedidoService::convertToItemResumoResponse)
                .collect(Collectors.toList());

        pedidoResponse.setItens(itensResponse);
        return pedidoResponse;
    }
}
