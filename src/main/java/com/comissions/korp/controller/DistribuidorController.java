package com.comissions.korp.controller;

import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorRequestDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorResponseDTO;
import com.comissions.korp.service.DistribuidorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/distribuidor")
public class DistribuidorController {

    private final DistribuidorService distribuidorService;

    public DistribuidorController(DistribuidorService distribuidorService) {
        this.distribuidorService = distribuidorService;
    }

    @PostMapping
    @Operation(summary = "Criar distribuidor", description = "Cria um novo distribuidor com os dados informados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Distribuidor criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<DistribuidorResponseDTO> criar(@RequestBody DistribuidorRequestDTO requestDTO) {
        DistribuidorResponseDTO responseDTO = distribuidorService.criar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    @Operation(summary = "Listar distribuidores", description = "Retorna todos os distribuidores cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Distribuidores listados com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<DistribuidorResponseDTO>> listarTodos() {
        List<DistribuidorResponseDTO> distribuidores = distribuidorService.listarTodos();
        return ResponseEntity.ok(distribuidores);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar distribuidor por ID", description = "Retorna um distribuidor pelo identificador informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Distribuidor encontrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Distribuidor não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<DistribuidorResponseDTO> buscarPorId(@PathVariable Integer id) {
        DistribuidorResponseDTO responseDTO = distribuidorService.buscarDtoPorId(id);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/cnpj/{cnpj}")
    @Operation(summary = "Buscar distribuidor por CNPJ", description = "Retorna um distribuidor a partir do CNPJ informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Distribuidor encontrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "CNPJ inválido"),
            @ApiResponse(responseCode = "404", description = "Distribuidor não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<DistribuidorResponseDTO> buscarPorCnpj(@PathVariable String cnpj) {
        DistribuidorResponseDTO responseDTO = distribuidorService.buscarPorCnpj(cnpj);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar distribuidor", description = "Atualiza os dados de um distribuidor existente pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Distribuidor atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Distribuidor não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<DistribuidorResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestBody DistribuidorRequestDTO requestDTO) {
        DistribuidorResponseDTO responseDTO = distribuidorService.atualizar(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover distribuidor", description = "Remove um distribuidor cadastrado pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Distribuidor removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Distribuidor não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        distribuidorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
