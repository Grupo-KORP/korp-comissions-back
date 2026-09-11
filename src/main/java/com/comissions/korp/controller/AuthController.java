package com.comissions.korp.controller;

import com.comissions.korp.DTO.AuthDTO.LoginRequestDTO;
import com.comissions.korp.DTO.AuthDTO.LoginResponseDTO;
import com.comissions.korp.DTO.AuthDTO.AuthUserDTO;
import com.comissions.korp.config.security.AuthCookieService;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.UsuarioInativoException;
import com.comissions.korp.repository.UsuarioRepository;
import com.comissions.korp.config.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthCookieService authCookieService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida credenciais e cria uma sessão JWT protegida por cookie HttpOnly.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de login inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request,
                                                  HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new UsuarioInativoException("Usuário inativo. Entre em contato com o administrador.");
        }
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.gerarToken(userDetails);
        authCookieService.writeToken(response, token);

        return ResponseEntity.ok(new LoginResponseDTO(new AuthUserDTO(usuario, userDetails.getAuthorities())));
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session")
    public ResponseEntity<LoginResponseDTO> session(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new LoginResponseDTO(
                new AuthUserDTO(usuario, authentication.getAuthorities())
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        authCookieService.clearToken(response);
        authCookieService.clearCsrfToken(response);
        return ResponseEntity.noContent().build();
    }
}
