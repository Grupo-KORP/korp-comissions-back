package com.comissions.korp.config.utils;

import com.comissions.korp.DTO.UsuarioRequestDTO;
import com.comissions.korp.entity.Role;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.repository.RoleRepository;
import com.comissions.korp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DataInicializer implements ApplicationRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;


    @Override
    public void run(ApplicationArguments arg){
        boolean admCadastrado = false;
        boolean vendedorCadastrado = false;

        // Verifica se já existe usuário com mesmo email
        if (!usuarioRepository.existsByEmail("janderson@tndbrasil.com.br")) {
            UsuarioRequestDTO requestDTO = new UsuarioRequestDTO(
                    null,"Janderson Mira","janderson@tndbrasil.com.br","","119950012322",1,0.0
            );

            Optional<Role> role = roleRepository.findById(requestDTO.getRole());
            Role roleUsuario = role.get();

            String senhaAleatoria = "123456";

            String senhaEncriptada = passwordEncoder.encode(senhaAleatoria);

            Usuario usuario = convertToEntity(requestDTO, roleUsuario, senhaEncriptada);
            usuarioRepository.save(usuario);
            System.out.println("ADMIN CRIADO COM SUCESSO!");
            admCadastrado = true;
        }

        if (!usuarioRepository.existsByEmail("vendedor@tndbrasil.com.br")) {
            UsuarioRequestDTO requestDTO = new UsuarioRequestDTO(
                    null,"Vendedor Padrão","vendedor@tndbrasil.com.br","","119950012321",3,30.0
            );

            Optional<Role> role = roleRepository.findById(requestDTO.getRole());
            Role roleUsuario = role.get();

            String senhaAleatoria = "123456";

            String senhaEncriptada = passwordEncoder.encode(senhaAleatoria);

            Usuario usuario = convertToEntity(requestDTO, roleUsuario, senhaEncriptada);
            usuarioRepository.save(usuario);
            System.out.println("VENDEDOR CRIADO COM SUCESSO!");
            vendedorCadastrado = true;
        }

        if (!admCadastrado) {
            System.out.println("ADMIN JÁ CRIADO");
        }

        if (!vendedorCadastrado) {
            System.out.println("VENDEDOR JÁ CRIADO");
        }
    }

    private Usuario convertToEntity(UsuarioRequestDTO dto, Role role, String senha) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(senha);
        usuario.setTelefone(dto.getTelefone());
        usuario.setPercentualComissao(dto.getPercentualComissao());
        usuario.setDtCriacao(LocalDateTime.now());
        usuario.setRoles(role);
        return usuario;
    }
}
