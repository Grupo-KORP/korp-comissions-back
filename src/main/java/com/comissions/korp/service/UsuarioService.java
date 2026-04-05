package com.comissions.korp.service;

import com.comissions.korp.DTO.UsuarioRequestDTO;
import com.comissions.korp.DTO.UsuarioResponseDTO;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.UsuarioJaExistente;
import com.comissions.korp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Cria um novo Usuario
     */
    public UsuarioResponseDTO criar(UsuarioRequestDTO requestDTO) {
        // Verifica se já existe usuário com mesmo email
        if (usuarioRepository.existsByEmail(requestDTO.email())) {
            throw new UsuarioJaExistente("Já existe um usuário com este email: " + requestDTO.email());
        }

        // Verifica se já existe usuário com mesmo nome
        if (usuarioRepository.existsByNome(requestDTO.nome())) {
            throw new UsuarioJaExistente("Já existe um usuário com este nome: " + requestDTO.nome());
        }

        Usuario usuario = convertToEntity(requestDTO);
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
        Usuario usuarioExistente = buscarUsuarioPorId(id);

        usuarioExistente.setNome(requestDTO.nome());
        usuarioExistente.setEmail(requestDTO.email());
        usuarioExistente.setSenha(requestDTO.senha());
        usuarioExistente.setTelefone(requestDTO.telefone());

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
    private Usuario convertToEntity(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(dto.idUsuario());
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());
        usuario.setTelefone(dto.telefone());
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