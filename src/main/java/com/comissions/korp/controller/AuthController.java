package com.comissions.korp.controller;

import com.comissions.korp.DTO.LoginRequestDTO;
import com.comissions.korp.DTO.LoginResponseDTO;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.repository.UsuarioRepository;
import com.comissions.korp.config.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.gerarToken(userDetails);
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();

        return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getNome(), usuario.getEmail()));
    }
}