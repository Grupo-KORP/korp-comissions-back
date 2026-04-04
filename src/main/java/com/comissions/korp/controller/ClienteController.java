package com.comissions.korp.controller;

import com.comissions.korp.DTO.ClienteDTO.ClienteRequestDTO;
import com.comissions.korp.DTO.ClienteDTO.ClienteResponseDTO;
import com.comissions.korp.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

//    @GetMapping
//    public ResponseEntity<List<Cliente>> listarTodos(){
//        List<Cliente> listarTodos = clienteService.listarTodosClientes();
//        if(listarTodos.isEmpty()){
//            return ResponseEntity.status(204).build();
//        }
//        return ResponseEntity.status(200).body(listarTodos);
//    }
//
//    @GetMapping("/{cnpj}")
//    public ResponseEntity<Object> buscarPorCnpj(@PathVariable String cnpj) {
//        try {
//            Optional<Cliente> cliente = clienteService.buscarClientePorCnpj(cnpj);
//
//            if (cliente.isPresent()) {
//                return ResponseEntity.status(200).body(cliente.get());
//            }
//
//            return ResponseEntity.status(400)
//                    .body(Map.of("erro", "Cliente não encontrado para o CNPJ: " + cnpj));
//
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(400).body(Map.of("erro", e.getMessage()));
//        }
//    }
//
//    @PostMapping
//    public ResponseEntity<Object> cadastrarCliente(@RequestBody Cliente cliente) {
//        try {
//            Cliente cadastroRealizado = clienteService.cadastrarCliente(cliente);
//
//            return ResponseEntity.status(201).body(cadastroRealizado);
//        } catch (IllegalArgumentException e) {
//            Map<String, String> erro = new HashMap<>();
//
//            erro.put("mensagem", e.getMessage());
//            return ResponseEntity.status(400).body(erro);
//        }
//    }
//
//    @PutMapping("/cnpj/{cnpj}")
//    public ResponseEntity<Object> atualizarPorCnpj(@PathVariable String cnpj, @RequestBody Cliente dados) {
//
//        Optional<Cliente> atualizado = clienteService.atualizarCliente(cnpj, dados);
//
//        if (atualizado.isPresent()) {
//            return ResponseEntity.status(200).body(atualizado.get());
//        }
//
//        return ResponseEntity.status(400)
//                .body(Map.of("erro", "Cliente com CNPJ " + cnpj + " não encontrado."));
//    }
//
//    @DeleteMapping("/cnpj/{cnpj}")
//    public ResponseEntity<Object> deletarCliente(@PathVariable String cnpj) {
//        boolean removido = clienteService.deletarPorCnpj(cnpj);
//
//        if (removido) {
//            return ResponseEntity.status(204).build();
//        }
//
//        Map<String, String> erro = new HashMap<>();
//        erro.put("erro", "Cliente com CNPJ " + cnpj + " não encontrado para remoção.");
//
//        return ResponseEntity.status(400).body(erro);
//    }

    /**
     * Cria um novo Cliente
     * POST /api/clientes
     */
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criar(@RequestBody ClienteRequestDTO requestDTO) {
        ClienteResponseDTO responseDTO = clienteService.criar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    /**
     * Lista todos os Clientes
     * GET /api/clientes
     */
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        List<ClienteResponseDTO> clientes = clienteService.listarTodos();
        return ResponseEntity.ok(clientes);
    }

    /**
     * Busca um Cliente por ID
     * GET /api/clientes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Integer id) {
        ClienteResponseDTO responseDTO = clienteService.buscarPorId(id);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Busca um Cliente por CNPJ
     * GET /api/clientes/cnpj/{cnpj}
     */
    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<ClienteResponseDTO> buscarPorCnpj(@PathVariable String cnpj) {
        ClienteResponseDTO responseDTO = clienteService.buscarPorCnpj(cnpj);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Atualiza um Cliente existente
     * PUT /api/clientes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestBody ClienteRequestDTO requestDTO) {
        ClienteResponseDTO responseDTO = clienteService.atualizar(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Deleta um Cliente
     * DELETE /api/clientes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
