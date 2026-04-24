package com.comissions.korp.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String mensagem = "Operação não permitida: este registro possui vínculos com outros dados.";

        String causa = e.getRootCause() != null ? e.getRootCause().getMessage() : "";

        // DELETE com vínculos (ON DELETE RESTRICT)
        if (causa.contains("fk_distribuidor") || causa.contains("pedido_ibfk_3")) {
            mensagem = "Distribuidor não pode ser excluído pois possui pedidos vinculados.";
        } else if (causa.contains("fk_produto")) {
            mensagem = "Produto não pode ser excluído pois está vinculado a pedidos.";
        } else if (causa.contains("fk_cliente") || causa.contains("pedido_ibfk_2")) {
            mensagem = "Cliente não pode ser excluído pois possui pedidos vinculados.";
        } else if (causa.contains("fk_vendedor") || causa.contains("pedido_ibfk_1")) {
            mensagem = "Vendedor não pode ser excluído pois possui pedidos vinculados.";
        }

        // INSERT/UPDATE com duplicidade (UNIQUE constraints)
        else if (causa.contains("uc_distribuidor_cnpj")) {
            mensagem = "Já existe um distribuidor cadastrado com este CNPJ.";
        } else if (causa.contains("uc_cliente_cnpj")) {
            mensagem = "Já existe um cliente cadastrado com este CNPJ.";
        } else if (causa.contains("uc_usuario_email")) {
            mensagem = "Já existe um usuário cadastrado com este e-mail.";
        }

        Map<String, String> response = new HashMap<>();
        response.put("erro", mensagem);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(RecursoNaoEncontrado.class)
    public ResponseEntity<Map<String, String>> handleRecursoNaoEncontrado(RecursoNaoEncontrado e) {
        Map<String, String> response = new HashMap<>();
        response.put("erro", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}