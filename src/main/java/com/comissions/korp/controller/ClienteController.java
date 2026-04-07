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
        ClienteResponseDTO responseDTO = clienteService.buscarDtoPorId(id);
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
