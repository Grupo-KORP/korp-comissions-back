package com.comissions.korp.service;

import com.comissions.korp.DTO.ListarVendedoresResponseDTO;
import com.comissions.korp.DTO.UsuarioRequestDTO;
import com.comissions.korp.DTO.UsuarioResponseDTO;
import com.comissions.korp.entity.Role;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.UsuarioJaExistente;
import com.comissions.korp.repository.PedidoRepository;
import com.comissions.korp.repository.RoleRepository;
import com.comissions.korp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

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

        if (usuarioRepository.existsByTelefone((requestDTO.getTelefone()))) {
            throw new UsuarioJaExistente("Já existe um usuário com este telefone: " + requestDTO.getTelefone());
        }

        Optional<Role> role = roleRepository.findById(requestDTO.getRole());
        Role roleUsuario = role.get();

        Usuario geradorDeSenha = new Usuario();
        String senhaAleatoria = geradorDeSenha.gerarSenha();

        String senhaEncriptada = passwordEncoder.encode(senhaAleatoria);

        Usuario usuario = convertToEntity(requestDTO,roleUsuario, senhaEncriptada);
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        emailService.enviarEmailSenhaProvisoria(usuario, senhaAleatoria);
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
     * Lista todos os Vendedores com paginação
     */
    public Page<ListarVendedoresResponseDTO> listarTodosVendedores(String busca, Pageable pageable) {

        String filtro = (busca != null && !busca.isBlank()) ? busca : null;

        Page<Usuario> paginaVendedores = usuarioRepository.findVendedoresComFiltro(filtro, pageable);


        // COUNT retorna Long — cast correto
        List<Object[]> pedidosPorVendedor = pedidoRepository.contarPedidosPorVendedor();

        Map<Integer, Integer> mapaPedidos = pedidosPorVendedor.stream()
                .collect(Collectors.toMap(
                        obj -> (Integer) obj[0],
                        obj -> ((Long) obj[1]).intValue() // <-- era Integer, explode em runtime
                ));

        return paginaVendedores.map(vendedor -> new ListarVendedoresResponseDTO(
                vendedor.getNome(),
                vendedor.getEmail(),
                vendedor.getTelefone(),
                vendedor.getPercentualComissao(),
                mapaPedidos.getOrDefault(vendedor.getIdUsuario(), 0),
                0, // vendasEfetivadas — implementar quando tiver status de pedido
                0  // comissoesPendentes — implementar quando tiver lógica de comissão
        ));
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
                usuario.getEmail(),
                usuario.getSenha(),
                usuario.getTelefone(),
                usuario.getPercentualComissao()
        );
    }

}