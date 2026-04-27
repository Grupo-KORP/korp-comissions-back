package com.comissions.korp.controller;

import com.comissions.korp.DTO.ContatoDTO.ContatoRequestDTO;
import com.comissions.korp.DTO.ContatoDTO.ContatoResponseDTO;
import com.comissions.korp.service.ContatoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contatos")
public class ContatoController {

    private final ContatoService contatoService;

    public ContatoController(ContatoService contatoService) {
        this.contatoService = contatoService;
    }

    @PostMapping
    @Operation(summary = "Criar contato", description = "Cria um novo contato com os dados informados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contato criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ContatoResponseDTO> criar(@RequestBody ContatoRequestDTO requestDTO) {
        ContatoResponseDTO responseDTO = contatoService.criar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    @Operation(summary = "Listar contatos", description = "Retorna a lista de todos os contatos cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contatos listados com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<ContatoResponseDTO>> listarTodos() {
        List<ContatoResponseDTO> contatos = contatoService.listarTodos();
        return ResponseEntity.ok(contatos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar contato por ID", description = "Retorna um contato específico a partir do ID informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contato encontrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Contato não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ContatoResponseDTO> buscarPorId(@PathVariable Integer id) {
        ContatoResponseDTO responseDTO = contatoService.buscarDtoPorId(id);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar contato", description = "Atualiza os dados de um contato existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contato atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Contato não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ContatoResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestBody ContatoRequestDTO requestDTO) {
        ContatoResponseDTO responseDTO = contatoService.atualizar(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }
}
