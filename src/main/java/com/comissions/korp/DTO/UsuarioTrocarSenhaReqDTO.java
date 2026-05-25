package com.comissions.korp.DTO;

public class UsuarioTrocarSenhaReqDTO {

    private String senhaVelha;
    private String senhaNova;


    public String getSenhaVelha() {
        return senhaVelha;
    }

    public void setSenhaVelha(String senhaVelha) {
        this.senhaVelha = senhaVelha;
    }

    public String getSenhaNova() {
        return senhaNova;
    }

    public void setSenhaNova(String senhaNova) {
        this.senhaNova = senhaNova;
    }
}
