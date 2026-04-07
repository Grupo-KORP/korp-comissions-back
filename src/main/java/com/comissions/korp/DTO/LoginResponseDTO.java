package com.comissions.korp.DTO;

public class LoginResponseDTO {
    private String token;
    private String tipo = "Bearer";
    private String nome;
    private String email;

    public LoginResponseDTO(String token, String nome, String email) {
        this.token = token;
        this.nome = nome;
        this.email = email;
    }

    public String getToken() { return token; }
    public String getTipo() { return tipo; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
}