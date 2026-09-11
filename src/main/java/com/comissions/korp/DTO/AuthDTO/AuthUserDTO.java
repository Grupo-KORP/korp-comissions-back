package com.comissions.korp.DTO.AuthDTO;

import com.comissions.korp.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class AuthUserDTO {
    private Integer id;
    private String nome;
    private String email;
    private List<String> roles;
    private Boolean primeiroAcesso;

    public AuthUserDTO() {
    }

    public AuthUserDTO(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        this.id = usuario.getIdUsuario();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        this.primeiroAcesso = usuario.getPrimeiroAcesso();
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Boolean getPrimeiroAcesso() {
        return primeiroAcesso;
    }
}
