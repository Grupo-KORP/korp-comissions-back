package com.comissions.korp.service.Email.dto;

public record EnviarSenhaProvisoriaRequest(
        String nome,
        String email,
        String senhaProvisoria
) {
}
