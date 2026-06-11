package com.comissions.korp.DTO;

public class UsuarioEsqueciSenhaRequestDTO {

    private String novaSenha;
    private String confirmaSenha;

    public String getNovaSenha() {
        return novaSenha;
    }

    public String getConfirmaSenha() {
        return confirmaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }

    public void setConfirmaSenha(String confirmaSenha) {
        this.confirmaSenha = confirmaSenha;
    }
}
