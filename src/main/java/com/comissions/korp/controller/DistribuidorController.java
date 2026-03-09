package com.comissions.korp.controller;


import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.service.DistribuidorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PrivateKey;
import java.util.*;


@RestController
@RequestMapping("/distribuidor")
public class DistribuidorController {

    private final DistribuidorService distribuidorService;

    public DistribuidorController(DistribuidorService distribuidorService) {
        this.distribuidorService = distribuidorService;
    }

    @GetMapping
    public ResponseEntity<List<Distribuidor>> listarTodos(){
        List<Distribuidor> listarTodos = distribuidorService.listarTodosDistribuidores();
        if(listarTodos.isEmpty()){
            return ResponseEntity.status(204).build();
        }
        return ResponseEntity.status(200).body(listarTodos);
    }

    @GetMapping("/{cnpj}")
    public ResponseEntity<Object> buscarPorCnpj(@PathVariable String cnpj) {
        try {
            Optional<Distribuidor> distribuidor = distribuidorService.buscarDistribuidorPorCnpj(cnpj);

            if (distribuidor.isPresent()) {
                return ResponseEntity.status(200).body(distribuidor.get());
            }

            return ResponseEntity.status(400)
                    .body(Map.of("erro", "Distribuidor não encontrado para o CNPJ: " + cnpj));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<Object> cadastrarDistribuidor(@RequestBody Distribuidor distribuidor) {
        try {
            Distribuidor cadastroRealizado = distribuidorService.cadastrarDistribuidor(distribuidor);

            return ResponseEntity.status(201).body(cadastroRealizado);
        } catch (IllegalArgumentException e) {
            Map<String, String> erro = new HashMap<>();

            erro.put("mensagem", e.getMessage());
            return ResponseEntity.status(400).body(erro);
        }
    }

    @PutMapping("/cnpj/{cnpj}")
    public ResponseEntity<Object> atualizarPorCnpj(@PathVariable String cnpj, @RequestBody Distribuidor dados) {

        Optional<Distribuidor> atualizado = distribuidorService.atualizarDistribuidor(cnpj, dados);

        if (atualizado.isPresent()) {
            return ResponseEntity.status(200).body(atualizado.get());
        }

        return ResponseEntity.status(400)
                .body(Map.of("erro", "Distribuidor com CNPJ " + cnpj + " não encontrado."));
    }

    @DeleteMapping("/cnpj/{cnpj}")
    public ResponseEntity<Object> deletarDistribuidor(@PathVariable String cnpj) {
        boolean removido = distribuidorService.deletarPorCnpj(cnpj);

        if (removido) {
            return ResponseEntity.status(204).build();
        }

        Map<String, String> erro = new HashMap<>();
        erro.put("erro", "Distribuidor com CNPJ " + cnpj + " não encontrado para remoção.");

        return ResponseEntity.status(400).body(erro);
    }
}
