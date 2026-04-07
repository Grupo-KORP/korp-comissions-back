package com.comissions.korp.controller;

import com.comissions.korp.DTO.UsuarioRequestDTO;
import com.comissions.korp.DTO.UsuarioResponseDTO;
import com.comissions.korp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Cria um novo Usuário
     * POST /usuarios
     */
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@RequestBody UsuarioRequestDTO requestDTO) {
        UsuarioResponseDTO responseDTO = usuarioService.criar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    /**
     * Lista todos os Usuários
     * GET /usuario
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Busca um Usuário por ID
     * GET /usuario/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Integer id) {
        UsuarioResponseDTO responseDTO = usuarioService.buscarDtoPorId(id);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Atualiza um Usuário existente
     * PUT /usuario/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestBody UsuarioRequestDTO requestDTO) {
        UsuarioResponseDTO responseDTO = usuarioService.atualizar(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Deleta um Usuário
     * DELETE /usuario/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}