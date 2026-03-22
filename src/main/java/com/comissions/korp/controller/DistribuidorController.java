package com.comissions.korp.controller;

import com.comissions.korp.DTO.DistribuidorRequestDTO;
import com.comissions.korp.DTO.DistribuidorResponseDTO;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.service.DistribuidorService;
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
    public ResponseEntity<DistribuidorResponseDTO> criar(@RequestBody DistribuidorRequestDTO requestDTO) {
        DistribuidorResponseDTO responseDTO = distribuidorService.criar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<DistribuidorResponseDTO>> listarTodos() {
        List<DistribuidorResponseDTO> distribuidores = distribuidorService.listarTodos();
        return ResponseEntity.ok(distribuidores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DistribuidorResponseDTO> buscarPorId(@PathVariable Integer id) {
        DistribuidorResponseDTO responseDTO = distribuidorService.buscarPorId(id);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<DistribuidorResponseDTO> buscarPorCnpj(@PathVariable String cnpj) {
        DistribuidorResponseDTO responseDTO = distribuidorService.buscarPorCnpj(cnpj);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DistribuidorResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestBody DistribuidorRequestDTO requestDTO) {
        DistribuidorResponseDTO responseDTO = distribuidorService.atualizar(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        distribuidorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
