package com.comissions.korp.service;

import com.comissions.korp.DTO.UsuarioRequestDTO;
import com.comissions.korp.DTO.UsuarioResponseDTO;
import com.comissions.korp.entity.Role;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.UsuarioJaExistente;
import com.comissions.korp.repository.RoleRepository;
import com.comissions.korp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Cria um novo Usuario
     */
    public UsuarioResponseDTO criar(UsuarioRequestDTO requestDTO) {
        // Verifica se já existe usuário com mesmo email
        if (usuarioRepository.existsByEmail(requestDTO.getEmail())) {
            throw new UsuarioJaExistente("Já existe um usuário com este email: " + requestDTO.getEmail());
        }

        // Verifica se já existe usuário com mesmo nome
        if (usuarioRepository.existsByNome(requestDTO.getNome())) {
            throw new UsuarioJaExistente("Já existe um usuário com este nome: " + requestDTO.getNome());
        }

        Optional<Role> role = roleRepository.findById(requestDTO.getRole());
        Role roleUsuario = role.get();

        String senhaEncriptada = passwordEncoder.encode(requestDTO.getSenha());

        Usuario usuario = convertToEntity(requestDTO,roleUsuario, senhaEncriptada);
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return convertToResponseDTO(usuarioSalvo);
    }

    /**
     * Lista todos os Usuarios
     */
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca Usuario por ID
     */
    public UsuarioResponseDTO buscarDtoPorId(Integer id) {
        Usuario usuario = buscarUsuarioPorId(id);
        return convertToResponseDTO(usuario);
    }

    public Usuario buscarUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado("Usuário não encontrado com ID: " + id));
    }

    /**
     * Atualiza Usuario existente
     */
    public UsuarioResponseDTO atualizar(Integer id, UsuarioRequestDTO requestDTO) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontrado("Usuário não encontrado com ID: " + id));

        usuarioExistente.setNome(requestDTO.getNome());
        usuarioExistente.setEmail(requestDTO.getEmail());
        usuarioExistente.setSenha(requestDTO.getSenha());
        usuarioExistente.setTelefone(requestDTO.getTelefone());

        Usuario usuarioAtualizado = usuarioRepository.save(usuarioExistente);
        return convertToResponseDTO(usuarioAtualizado);
    }

    /**
     * Deleta Usuario por ID
     */
    public void deletar(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNaoEncontrado("Usuário não encontrado com ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * Converte UsuarioRequestDTO para Entity
     */
    private Usuario convertToEntity(UsuarioRequestDTO dto, Role role, String senha) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(dto.getIdUsuario());
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(senha);
        usuario.setTelefone(dto.getTelefone());
        usuario.setPercentualComissao(dto.getPercentualComissao());
        usuario.setDtCriacao(LocalDateTime.now());
        usuario.setRoles(role);
        return usuario;
    }

    /**
     * Converte Entity para UsuarioResponseDTO
     */
    private UsuarioResponseDTO convertToResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getSenha(),
                usuario.getEmail(),
                usuario.getTelefone()
        );
    }

}