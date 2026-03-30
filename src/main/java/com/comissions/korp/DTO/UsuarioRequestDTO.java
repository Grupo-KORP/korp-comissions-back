package com.comissions.korp.DTO;

public record UsuarioRequestDTO(
        Integer idUsuario,
        String nome,
        String email,
        String senha,
        String telefone
) {
}
