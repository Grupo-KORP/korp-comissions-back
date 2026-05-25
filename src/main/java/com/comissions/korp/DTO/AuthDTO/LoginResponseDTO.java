package com.comissions.korp.DTO.AuthDTO;

public class LoginResponseDTO {
    private String token;
    private String tipo = "Bearer";
    private String nome;
    private String email;
    private Boolean primeiroAcesso;

    public LoginResponseDTO(String token, String nome, String email, Boolean primeiroAcesso) {
        this.token = token;
        this.nome = nome;
        this.email = email;
        this.primeiroAcesso = primeiroAcesso;
    }

    public String getToken() { return token; }
    public String getTipo() { return tipo; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }

    public Boolean getPrimeiroAcesso() {
        return primeiroAcesso;
    }

    public void setPrimeiroAcesso(Boolean primeiroAcesso) {
        this.primeiroAcesso = primeiroAcesso;
    }
}