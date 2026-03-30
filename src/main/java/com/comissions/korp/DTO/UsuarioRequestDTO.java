package com.comissions.korp.DTO;

import com.comissions.korp.entity.Role;

public record UsuarioRequestDTO(
        Integer idUsuario,
        String nome,
        String email,
        String senha,
        String telefone,
        Integer role
) {
}
