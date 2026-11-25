package com.comissions.korp.controller;

import com.comissions.korp.config.security.AuthCookieService;
import com.comissions.korp.config.security.JwtService;
import com.comissions.korp.config.security.UsuarioDetailsService;
import com.comissions.korp.entity.ENUM.Roles;
import com.comissions.korp.entity.Role;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.repository.RoleRepository;
import com.comissions.korp.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Regras de acesso dos endpoints da área do vendedor (SecurityConfig). */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:korp-seguranca-vendedor-it;MODE=MySQL")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VendedorSecurityTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    private Usuario usuarioVendedor;
    private Cookie vendedor;
    private Cookie financeiro;

    @BeforeEach
    void criarUsuarios() {
        usuarioVendedor = criarUsuario("vend-seg@korp.com", Roles.VEND);
        vendedor = cookieDe(usuarioVendedor);
        financeiro = cookieDe(criarUsuario("fin-seg@korp.com", Roles.FINAN));
    }

    @Test
    void anonimoNaoAcessaEndpointsDoVendedor() throws Exception {
        mvc.perform(get("/api/vendedor/home")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/cliente/listarFiltro")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/distribuidor/pedido-dto")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/produto")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/pedidos/listar")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/pedidos/vendedor/" + usuarioVendedor.getIdUsuario())).andExpect(status().isUnauthorized());
        mvc.perform(escrita(post("/api/pedidos/cadastrar"), null).content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(escrita(post("/api/cliente"), null).content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(escrita(delete("/api/item_pedido/deletar/1"), null)).andExpect(status().isUnauthorized());
    }

    @Test
    void financeiroNaoOperaPedidosNemPainelDoVendedor() throws Exception {
        mvc.perform(get("/api/vendedor/home").cookie(financeiro)).andExpect(status().isForbidden());
        mvc.perform(escrita(post("/api/pedidos/cadastrar"), financeiro).content("{}")).andExpect(status().isForbidden());
        mvc.perform(escrita(delete("/api/pedidos/deletar/1"), financeiro)).andExpect(status().isForbidden());
        mvc.perform(escrita(delete("/api/item_pedido/deletar/1"), financeiro)).andExpect(status().isForbidden());
    }

    @Test
    void financeiroContinuaVendoHistoricoDoVendedor() throws Exception {
        mvc.perform(get("/api/pedidos/vendedor/" + usuarioVendedor.getIdUsuario()).cookie(financeiro))
                .andExpect(status().isOk());
    }

    @Test
    void vendedorAcessaSuaArea() throws Exception {
        mvc.perform(get("/api/vendedor/home").cookie(vendedor)).andExpect(status().isOk());
        mvc.perform(get("/api/cliente/listarFiltro").cookie(vendedor)).andExpect(status().isOk());
        mvc.perform(get("/api/produto/listarFiltro").cookie(vendedor)).andExpect(status().isOk());
        mvc.perform(get("/api/pedidos/vendedor/" + usuarioVendedor.getIdUsuario()).cookie(vendedor))
                .andExpect(status().isOk());
    }

    @Test
    void vendedorNaoVeHistoricoDeOutroVendedor() throws Exception {
        Usuario outroVendedor = criarUsuario("vend-outro@korp.com", Roles.VEND);

        mvc.perform(get("/api/pedidos/vendedor/" + outroVendedor.getIdUsuario()).cookie(vendedor))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/pedidos/vendedor/" + outroVendedor.getIdUsuario()).cookie(financeiro))
                .andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder escrita(MockHttpServletRequestBuilder builder, Cookie auth) {
        Cookie csrf = new Cookie("XSRF-TOKEN", "teste");
        MockHttpServletRequestBuilder comCsrf = auth == null ? builder.cookie(csrf) : builder.cookie(auth, csrf);
        return comCsrf.header("X-XSRF-TOKEN", "teste").contentType(MediaType.APPLICATION_JSON);
    }

    private Usuario criarUsuario(String email, Roles papel) {
        Role role = new Role();
        role.setRole(papel);
        role = roleRepository.saveAndFlush(role);

        Usuario usuario = new Usuario();
        usuario.setNome(email);
        usuario.setEmail(email);
        usuario.setSenha("{noop}senha");
        usuario.setPrimeiroAcesso(false);
        usuario.setAtivo(true);
        usuario.setRoles(role);
        return usuarioRepository.saveAndFlush(usuario);
    }

    private Cookie cookieDe(Usuario usuario) {
        String token = jwtService.gerarToken(usuarioDetailsService.loadUserByUsername(usuario.getEmail()));
        return new Cookie(AuthCookieService.AUTH_COOKIE_NAME, token);
    }
}
