package com.comissions.korp.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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


        Map<String, String> response = new HashMap<>();
        response.put("erro", mensagem);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        String mensagem = e.getMessage() != null ? e.getMessage() : "Requisição inválida.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleMensagemIlegivel(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Requisição inválida: verifique os valores enviados."));
    }

    @ExceptionHandler(RecursoNaoEncontrado.class)
    public ResponseEntity<Map<String, String>> handleRecursoNaoEncontrado(RecursoNaoEncontrado e) {
        Map<String, String> response = new HashMap<>();
        response.put("erro", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UsuarioJaExistente.class)
    public ResponseEntity<Map<String, String>> handleUsuarioJaExistente(UsuarioJaExistente ex) {
        System.out.println(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UsuarioInativoException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioInativo(UsuarioInativoException ex) {
        System.out.println(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(RequisicaoInvalida.class)
    public ResponseEntity<Map<String, String>> handleRequisicaoInvalida(RequisicaoInvalida ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(OperacaoNaoPermitida.class)
    public ResponseEntity<Map<String, String>> handleOperacaoNaoPermitida(OperacaoNaoPermitida ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(LimiteRascunhosAtingido.class)
    public ResponseEntity<Map<String, String>> handleLimiteRascunhos(LimiteRascunhosAtingido ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage(), "codigo", "LIMITE_RASCUNHOS"));
    }

    @ExceptionHandler({RascunhoDesatualizado.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<Map<String, String>> handleRascunhoDesatualizado(Exception ex) {
        String mensagem = ex instanceof RascunhoDesatualizado
                ? ex.getMessage()
                : "Este registro foi alterado em outra operação. Recarregue e tente novamente.";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", mensagem, "codigo", "VERSAO_DESATUALIZADA"));
    }
}