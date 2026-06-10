package com.comissions.korp.service;

import com.comissions.korp.config.utils.SecurityUtils;
import com.comissions.korp.entity.Comissao;
import com.comissions.korp.entity.Usuario;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ComissaoService {
    private final UsuarioService usuarioService;
    private final SecurityUtils securityUtils;

    public ComissaoService(UsuarioService usuarioService, SecurityUtils securityUtils) {
        this.usuarioService = usuarioService;
        this.securityUtils = securityUtils;
    }

    public BigDecimal pegarComissaoPorUsuario() {
        Integer idUsuario = securityUtils.getUsuarioIdAutenticado();

        Usuario usuario = usuarioService.buscarUsuarioPorId(idUsuario);
        return usuario.getPercentualComissao();
    }
}
