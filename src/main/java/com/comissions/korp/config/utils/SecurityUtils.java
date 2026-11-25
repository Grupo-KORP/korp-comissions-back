package com.comissions.korp.config.utils;

import com.comissions.korp.config.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SecurityUtils {

    @Autowired
    JwtService jwtService;

    public Integer getUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuário não autenticado");
        }

        String token = (String) authentication.getCredentials();
        return jwtService.extrairId(token);
    }

    /** true quando o usuário logado tem algum dos perfis informados, sem o prefixo "ROLE_" (ex.: "FINAN", "ADMIN"). */
    public boolean usuarioPossuiAlgumPerfil(String... perfis) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        Set<String> esperadas = Arrays.stream(perfis).map(perfil -> "ROLE_" + perfil).collect(Collectors.toSet());
        return authentication.getAuthorities().stream()
                .anyMatch(autoridade -> esperadas.contains(autoridade.getAuthority()));
    }
}