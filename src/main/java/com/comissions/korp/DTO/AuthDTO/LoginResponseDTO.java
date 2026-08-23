package com.comissions.korp.DTO.AuthDTO;

public class LoginResponseDTO {
    private AuthUserDTO usuario;

    public LoginResponseDTO(AuthUserDTO usuario) {
        this.usuario = usuario;
    }

    public AuthUserDTO getUsuario() {
        return usuario;
    }
}
