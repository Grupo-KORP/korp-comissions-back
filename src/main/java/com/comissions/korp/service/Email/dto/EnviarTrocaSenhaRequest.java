package com.comissions.korp.service.Email.dto;

public record EnviarTrocaSenhaRequest(
        String nome,
        String email,
        String urlRedefinicao
) {
}
