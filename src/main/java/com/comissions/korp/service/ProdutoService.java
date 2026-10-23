package com.comissions.korp.service;

import com.comissions.korp.DTO.ProdutoDTO.ListarProdutosResponseDTO;
import com.comissions.korp.DTO.ProdutoDTO.ProdutoRequest;
import com.comissions.korp.DTO.ProdutoDTO.ProdutoResponse;
import com.comissions.korp.entity.Produto;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.RequisicaoInvalida;
import com.comissions.korp.exception.UsuarioJaExistente;
import com.comissions.korp.repository.ProdutoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class ProdutoService {

    private static final Pattern PART_NUMBER_VALIDO = Pattern.compile("^[A-Za-z0-9./-]{1,50}$");

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }



    @Transactional
    public ProdutoResponse cadastrarProduto(ProdutoRequest produtoRequest) {
        Produto produto = convertToEntity(produtoRequest);

        String partNumber = normalizarPartNumber(produtoRequest.getPn());
        if (partNumber != null) {
            validarPartNumberDisponivel(partNumber, null);
        }
        produto.setPartNumber(partNumber);

        Produto produtoSalvo = produtoRepository.save(produto);

        produtoSalvo.setCodigoProduto("#P" + String.format("%04d", produtoSalvo.getIdProduto()));

        Produto produtoFinal = produtoRepository.save(produtoSalvo);

        return convertToResponseDTO(produtoFinal);
    }

    public List<ProdutoResponse> listarProdutos(){
        return produtoRepository.findAll()
                .stream()
                .filter(produto -> produto.getAtivo() != null && produto.getAtivo())
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public ProdutoResponse buscarDtoPorId(Integer id){
        Produto produto = buscarProdutoPorId(id);
        return convertToResponseDTO(produto);
    }

    public Produto buscarProdutoPorId(Integer id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado("Produto não encontrado"));
    }

    public List<ProdutoResponse> buscarPorNome(String nome){
        List<ProdutoResponse> produtos = produtoRepository.findByNomeContains(nome)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        if (produtos.isEmpty()) {
            throw new RuntimeException("Nenhum produto encontrado com esse nome");
        }
        return produtos;
    }


    @Transactional
    public ProdutoResponse atualizarProduto(Integer id, ProdutoRequest produtoRequest){
        Produto produto = buscarProdutoPorId(id);

        boolean estavaAtivo = Boolean.TRUE.equals(produto.getAtivo());
        String partNumberAtual = produto.getPartNumber();
        String novoPartNumber = produtoRequest.getPn() != null
                ? normalizarPartNumber(produtoRequest.getPn())
                : partNumberAtual;
        boolean ficaraAtivo = produtoRequest.getAtivo() != null ? produtoRequest.getAtivo() : estavaAtivo;

        // Só confere duplicidade quando o produto fica ativo com um P/N novo ou volta a ficar ativo
        if (ficaraAtivo && novoPartNumber != null
                && (!estavaAtivo || !novoPartNumber.equalsIgnoreCase(partNumberAtual == null ? "" : partNumberAtual))) {
            validarPartNumberDisponivel(novoPartNumber, id);
        }
        produto.setPartNumber(novoPartNumber);

        if (produtoRequest.getNome() != null && !produtoRequest.getNome().isBlank()) {
            produto.setNome(produtoRequest.getNome());
        }
        if (produtoRequest.getAtivo() != null) {
            produto.setAtivo(produtoRequest.getAtivo());
        }

        Produto produtoAtt = produtoRepository.save(produto);
        return convertToResponseDTO(produtoAtt);
    }


    @Transactional
    public void deletarProduto(Integer id){
        Produto produto = buscarProdutoPorId(id);

        produto.setAtivo(false);

        produtoRepository.save(produto);
    }

    public Produto convertToEntity(ProdutoRequest dto) {
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        return produto;
    }

    private String normalizarPartNumber(String partNumber) {
        if (partNumber == null) return null;
        String limpo = partNumber.trim();
        if (limpo.isEmpty()) return null;
        if (!PART_NUMBER_VALIDO.matcher(limpo).matches()) {
            throw new RequisicaoInvalida("P/N inválido. Use até 50 caracteres entre letras, números, hífen, ponto e barra.");
        }
        return limpo;
    }

    private void validarPartNumberDisponivel(String partNumber, Integer idProdutoAtual) {
        boolean emUso = idProdutoAtual == null
                ? produtoRepository.existsByPartNumberIgnoreCaseAndAtivoTrue(partNumber)
                : produtoRepository.existsByPartNumberIgnoreCaseAndAtivoTrueAndIdProdutoNot(partNumber, idProdutoAtual);
        if (emUso) {
            throw new UsuarioJaExistente("Já existe um produto ativo com o P/N: " + partNumber);
        }
    }

    public ProdutoResponse convertToResponseDTO(Produto produto) {
        ProdutoResponse dto = new ProdutoResponse();
        dto.setIdProduto(produto.getIdProduto());
        dto.setNome(produto.getNome());
        dto.setCodigoProduto(produto.getCodigoProduto());
        dto.setPn(produto.getPartNumber());
        return dto;
    }


    /**
     * Lista todos os Vendedores com paginação
     */
    public Page<ListarProdutosResponseDTO> listarTodosProdutos(String busca, Pageable pageable) {

        String filtro = (busca != null && !busca.isBlank()) ? busca : null;

        Page<Produto> paginaProdutos = produtoRepository.findProdutosComFiltro(filtro, pageable);

        return paginaProdutos.map(produto -> new ListarProdutosResponseDTO(
                produto.getIdProduto(),
                produto.getNome(),
                produto.getCodigoProduto(),
                produto.getPartNumber()
        ));
    }
}
