package com.comissions.korp.controller;

import com.comissions.korp.DTO.ContatoRequestDTO;
import com.comissions.korp.DTO.ContatoResponseDTO;
import com.comissions.korp.service.ContatoService;
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
    public ResponseEntity<ContatoResponseDTO> criar(@RequestBody ContatoRequestDTO requestDTO) {
        ContatoResponseDTO responseDTO = contatoService.criar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<ContatoResponseDTO>> listarTodos() {
        List<ContatoResponseDTO> contatos = contatoService.listarTodos();
        return ResponseEntity.ok(contatos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContatoResponseDTO> buscarPorId(@PathVariable Integer id) {
        ContatoResponseDTO responseDTO = contatoService.buscarPorId(id);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContatoResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestBody ContatoRequestDTO requestDTO) {
        ContatoResponseDTO responseDTO = contatoService.atualizar(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }
}
