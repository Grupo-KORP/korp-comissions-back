package com.comissions.korp.service;

import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoRequest;
import com.comissions.korp.DTO.ItemPedidoDTO.ItemPedidoResumoResponse;
import com.comissions.korp.DTO.PedidoDTO.PedidoRequest;
import com.comissions.korp.DTO.PedidoDTO.PedidoResponse;
import com.comissions.korp.entity.*;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ClienteService clienteService;
    private final DistribuidorService distribuidorService;
    private final ItemPedidoService itemPedidoService;


    public PedidoService(PedidoRepository pedidoRepository
            ,ItemPedidoRepository itemPedidoRepository
            ,ProdutoRepository produtoRepository
            ,ClienteService clienteService
            ,DistribuidorService distribuidorService,
                         ItemPedidoService itemPedidoService) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteService = clienteService;
        this.distribuidorService = distribuidorService;
        this.itemPedidoService = itemPedidoService;
    }

    public PedidoResponse cadastrarPedido(PedidoRequest pedidoRequest) {

//        Vendedor vendedor = vendedorRepository.findById(pedidoRequest.getFkVendedor())
//                .orElseThrow(() -> new RuntimeException("Vendedor não encontrado com id: " + pedidoRequest.getFkVendedor()));

        Cliente cliente = clienteService.buscarClientePorId(pedidoRequest.getFkCliente());
        Distribuidor distribuidor = distribuidorService.buscarDistribuidorPorId(pedidoRequest.getFkDistribuidor());

        Pedido pedidoSalvo = pedidoRepository.save(criarPedidoFromRequest(pedidoRequest, cliente, distribuidor));
        Map<Integer, Produto> produtoMap = mapearProdutosPorId(pedidoRequest.getItens());
        List<ItemPedido> itensSalvos = salvarItensDoPedido(pedidoSalvo, pedidoRequest.getItens(), produtoMap);

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
                .orElseThrow(() -> new RecursoNaoEncontrado("Pedido não encontrado com id: " + id));

//        Vendedor vendedor = vendedorRepository.findById(pedidoRequest.getFkVendedor())
//                .orElseThrow(() -> new RuntimeException("Vendedor não encontrado com id: " + pedidoRequest.getFkVendedor()));

        Cliente cliente = clienteService.buscarClientePorId(pedidoRequest.getFkCliente());
        Distribuidor distribuidor = distribuidorService.buscarDistribuidorPorId(pedidoRequest.getFkDistribuidor());

        atualizarDadosPedido(pedido, pedidoRequest, cliente, distribuidor);

        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

        if (pedidoRequest.getItens() != null) {
            atualizarItensDoPedido(pedidoAtualizado, pedidoRequest.getItens());
        }

        List<ItemPedido> itens = itemPedidoRepository.findByPedido(pedidoAtualizado);

        return convertToPedidoResponse(pedidoAtualizado, itens);
    }

    @Transactional
    public void deletarPedido(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado("Pedido não encontrado com id: " + id));

        // Para deletar o pedido, precisa del tambem suas amarrações na tbl: Item_pedido
        itemPedidoRepository.deleteByPedido(pedido);
        pedidoRepository.deleteById(id);
    }

    //Convertendo entidades em DTO

    private PedidoResponse convertToPedidoResponse(Pedido pedido, List<ItemPedido> itens) {
        PedidoResponse pedidoResponse = new PedidoResponse();
        pedidoResponse.setIdPedido(pedido.getIdPedido());
        pedidoResponse.setDataPedido(pedido.getDataPedido());
        pedidoResponse.setNumeroNotaDistribuidor(pedido.getNumeroNotaDistribuidor());
        pedidoResponse.setValorTotalRevenda(pedido.getValorTotalRevenda());
        pedidoResponse.setValorTotalFaturamento(pedido.getValorTotalFaturamento());
        pedidoResponse.setStatusPedido(pedido.getStatusPedido());
        pedidoResponse.setFrete(pedido.getFrete());
        pedidoResponse.setTransportadora(pedido.getTransportadora());
        pedidoResponse.setObservacoes(pedido.getObservacoes());
        pedidoResponse.setFkCliente(pedido.getCliente() != null ? pedido.getCliente().getIdCliente() : null);
        pedidoResponse.setFkDistribuidor(pedido.getDistribuidor() != null ? pedido.getDistribuidor().getIdDistribuidor() : null);

        List<ItemPedidoResumoResponse> itensResponse = itens
                .stream()
                .map(itemPedidoService::convertToItemPedidoResumoResponse)
                .collect(Collectors.toList());

        pedidoResponse.setItens(itensResponse);
        return pedidoResponse;
    }

    private void atualizarDadosPedido(Pedido pedido, PedidoRequest request, Cliente cliente, Distribuidor distribuidor) {
        pedido.setDataPedido(request.getDataPedido());
        pedido.setNumeroNotaDistribuidor(request.getNumeroNotaDistribuidor());
        pedido.setValorTotalRevenda(request.getValorTotalRevenda());
        pedido.setValorTotalFaturamento(request.getValorTotalFaturamento());
        pedido.setStatusPedido(request.getStatusPedido());
        pedido.setFrete(request.getFrete());
        pedido.setTransportadora(request.getTransportadora());
        pedido.setObservacoes(request.getObservacoes());
        pedido.setCliente(cliente);
        pedido.setDistribuidor(distribuidor);
    }


    private Pedido criarPedidoFromRequest(PedidoRequest pedidoRequest, Cliente cliente, Distribuidor distribuidor) {
        Pedido pedido = new Pedido();
        atualizarDadosPedido(pedido, pedidoRequest, cliente, distribuidor);
        return pedido;
    }

    private Map<Integer, Produto> mapearProdutosPorId(List<ItemPedidoRequest> itensRequest) {
        List<Integer> idsProdutos = itensRequest.stream()
                .map(ItemPedidoRequest::getFkProduto)
                .collect(Collectors.toList());

        List<Produto> produtos = produtoRepository.findByIdProdutoIn(idsProdutos);

        return produtos.stream()
                .collect(Collectors.toMap(Produto::getIdProduto, produto -> produto));
    }

    private List<ItemPedido> salvarItensDoPedido(Pedido pedido, List<ItemPedidoRequest> itensRequest,
                                                  Map<Integer, Produto> produtoMap) {
        List<ItemPedido> itensSalvos = new ArrayList<>();

        for (ItemPedidoRequest itemRequest : itensRequest) {
            Produto produto = buscarProdutoPorId(itemRequest.getFkProduto(), produtoMap);
            ItemPedido itemPedido = criarItemPedido(pedido, produto, itemRequest);
            itensSalvos.add(itemPedidoRepository.save(itemPedido));
        }

        return itensSalvos;
    }

    private void atualizarItensDoPedido(Pedido pedido, List<ItemPedidoRequest> itensRequest) {
        itemPedidoRepository.deleteByPedido(pedido);

        if (itensRequest.isEmpty()) {
            return;
        }

        Map<Integer, Produto> produtoMap = mapearProdutosPorId(itensRequest);
        salvarItensDoPedido(pedido, itensRequest, produtoMap);
    }

    private Produto buscarProdutoPorId(Integer idProduto, Map<Integer, Produto> produtoMap) {
        Produto produto = produtoMap.get(idProduto);
        if (produto == null) {
            throw new RecursoNaoEncontrado("Produto não encontrado com id: " + idProduto);
        }
        return produto;
    }

    private ItemPedido criarItemPedido(Pedido pedido, Produto produto, ItemPedidoRequest itemRequest) {
        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setPedido(pedido);
        itemPedido.setProduto(produto);
        itemPedido.setQuantidade(itemRequest.getQuantidade());
        itemPedido.setValorUnitario(itemRequest.getValorUnitario());
        return itemPedido;
    }
}
