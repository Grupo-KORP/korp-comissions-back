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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:korp-rascunho-it;MODE=MySQL")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PedidoRascunhoControllerTest {

    private static final String URL = "/api/pedidos/rascunhos";
    private static final String CORPO = """
            {"dados":{"cliente":{"nomeFantasia":"Acme"},"distribuidor":{"razaoSocial":"Dist"},
                      "produtos":[{"descricao":"A"}],"formaPagamento":"AVISTA"}}
            """;

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

    private Cookie vendedor1;
    private Cookie vendedor2;
    private Cookie financeiro;

    @BeforeEach
    void criarUsuarios() {
        vendedor1 = cookieDe(criarUsuario("vend1@korp.com", Roles.VEND));
        vendedor2 = cookieDe(criarUsuario("vend2@korp.com", Roles.VEND));
        financeiro = cookieDe(criarUsuario("fin@korp.com", Roles.FINAN));
    }

    @Test
    void deveExigirAutenticacaoERoleDeVendedor() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mvc.perform(autenticado(get(URL), financeiro)).andExpect(status().isForbidden());
        mvc.perform(autenticado(post(URL), financeiro).contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isForbidden());
        mvc.perform(autenticado(get(URL), vendedor1)).andExpect(status().isOk());
    }

    @Test
    void deveCriarListarBuscarAtualizarEExcluirApenasOsProprios() throws Exception {
        MvcResult criado = mvc.perform(escrita(post(URL), vendedor1).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versao").value(0))
                .andExpect(jsonPath("$.nomeCliente").value("Acme"))
                .andExpect(jsonPath("$.metodoPagamento").value("AVISTA"))
                .andExpect(jsonPath("$.dtExpiracao").exists())
                .andReturn();
        String id = jsonId(criado);

        mvc.perform(autenticado(get(URL), vendedor1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].qtdProdutos").value(1));
        mvc.perform(autenticado(get(URL), vendedor2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mvc.perform(autenticado(get(URL + "/" + id), vendedor1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.cliente.nomeFantasia").value("Acme"));
        mvc.perform(autenticado(get(URL + "/" + id), vendedor2)).andExpect(status().isNotFound());
        mvc.perform(escrita(delete(URL + "/" + id), vendedor2)).andExpect(status().isNotFound());

        String atualizar = CORPO.replace("{\"dados\"", "{\"versao\":0,\"dados\"");
        mvc.perform(escrita(put(URL + "/" + id), vendedor1).content(atualizar))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(1));
        mvc.perform(escrita(put(URL + "/" + id), vendedor1).content(atualizar))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VERSAO_DESATUALIZADA"));

        mvc.perform(escrita(delete(URL + "/" + id), vendedor1)).andExpect(status().isNoContent());
        mvc.perform(autenticado(get(URL + "/" + id), vendedor1)).andExpect(status().isNotFound());
    }

    @Test
    void deveBloquearODecimoPrimeiroRascunhoComMensagemELiberarAposExcluir() throws Exception {
        String primeiro = null;
        for (int i = 0; i < 10; i++) {
            MvcResult resultado = mvc.perform(escrita(post(URL), vendedor1).content(CORPO))
                    .andExpect(status().isCreated()).andReturn();
            if (primeiro == null) primeiro = jsonId(resultado);
        }

        mvc.perform(escrita(post(URL), vendedor1).content(CORPO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("LIMITE_RASCUNHOS"))
                .andExpect(jsonPath("$.message").value("Limite de 10 rascunhos atingido. Exclua um para salvar outro."));

        // o limite é por vendedor
        mvc.perform(escrita(post(URL), vendedor2).content(CORPO)).andExpect(status().isCreated());

        mvc.perform(escrita(delete(URL + "/" + primeiro), vendedor1)).andExpect(status().isNoContent());
        mvc.perform(escrita(post(URL), vendedor1).content(CORPO)).andExpect(status().isCreated());
    }

    @Test
    void deveRejeitarFormaDePagamentoInvalida() throws Exception {
        String corpo = CORPO.replace("AVISTA", "PIX");

        mvc.perform(escrita(post(URL), vendedor1).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Forma de pagamento inválida: use AVISTA ou BOLETO."));
    }

    private MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder builder, Cookie auth) {
        return builder.cookie(auth);
    }

    private MockHttpServletRequestBuilder escrita(MockHttpServletRequestBuilder builder, Cookie auth) {
        return builder.cookie(auth, new Cookie("XSRF-TOKEN", "teste"))
                .header("X-XSRF-TOKEN", "teste")
                .contentType(MediaType.APPLICATION_JSON);
    }

    private String jsonId(MvcResult resultado) throws Exception {
        String corpo = resultado.getResponse().getContentAsString();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"id\":(\\d+)").matcher(corpo);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
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
