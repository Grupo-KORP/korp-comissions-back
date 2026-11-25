package com.comissions.korp.controller;

import com.comissions.korp.config.security.AuthCookieService;
import com.comissions.korp.config.security.JwtService;
import com.comissions.korp.config.security.UsuarioDetailsService;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.ENUM.Roles;
import com.comissions.korp.entity.ItemPedido;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.entity.Produto;
import com.comissions.korp.entity.Role;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.repository.ClienteRepository;
import com.comissions.korp.repository.DistribuidorRepository;
import com.comissions.korp.repository.ItemPedidoRepository;
import com.comissions.korp.repository.PedidoRepository;
import com.comissions.korp.repository.ProdutoRepository;
import com.comissions.korp.repository.RoleRepository;
import com.comissions.korp.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Exclusão de item: status HTTP de cada regra e recálculo dos totais do pedido. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:korp-item-pedido-it;MODE=MySQL")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ItemPedidoControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private DistribuidorRepository distribuidorRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ItemPedidoRepository itemPedidoRepository;
    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;

    private Usuario vendedor;
    private Cookie cookieVendedor;
    private Cookie cookieOutroVendedor;
    private int proximoNumeroDia = 1;

    @BeforeEach
    void setUp() {
        vendedor = criarUsuario("vend-item@korp.com");
        cookieVendedor = cookieDe(vendedor);
        cookieOutroVendedor = cookieDe(criarUsuario("vend-item-outro@korp.com"));
    }

    @Test
    void deveExcluirItemERecalcularTotais_quandoPedidoEmAndamentoTemOutroItem() throws Exception {
        Pedido pedido = criarPedido("EM_ANDAMENTO");
        ItemPedido removido = criarItem(pedido, "20.00", "30.00");
        criarItem(pedido, "30.00", "40.00");

        mvc.perform(excluir(removido, cookieVendedor)).andExpect(status().isOk());

        Pedido atualizado = pedidoRepository.findById(pedido.getIdPedido()).orElseThrow();
        assertThat(atualizado.getValorTotalDistr()).isEqualByComparingTo("30.00");
        assertThat(atualizado.getValorTotalCliente()).isEqualByComparingTo("40.00");
        assertThat(itemPedidoRepository.findById(removido.getIdItemPedido())).isEmpty();
    }

    @Test
    void deveResponder409_quandoPedidoJaFoiAprovado() throws Exception {
        Pedido pedido = criarPedido("APROVADO");
        ItemPedido item = criarItem(pedido, "20.00", "30.00");
        criarItem(pedido, "30.00", "40.00");

        mvc.perform(excluir(item, cookieVendedor))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Pedido aprovado não pode ser alterado."));
    }

    @Test
    void deveResponder400_quandoEOUltimoItemDoPedido() throws Exception {
        ItemPedido item = criarItem(criarPedido("EM_ANDAMENTO"), "20.00", "30.00");

        mvc.perform(excluir(item, cookieVendedor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O pedido precisa ter pelo menos um item. Adicione outro item antes de excluir este."));
    }

    @Test
    void deveResponder404_quandoItemEDeOutroVendedor() throws Exception {
        Pedido pedido = criarPedido("EM_ANDAMENTO");
        ItemPedido item = criarItem(pedido, "20.00", "30.00");
        criarItem(pedido, "30.00", "40.00");

        mvc.perform(excluir(item, cookieOutroVendedor)).andExpect(status().isNotFound());
        assertThat(itemPedidoRepository.findById(item.getIdItemPedido())).isPresent();
    }

    private MockHttpServletRequestBuilder excluir(ItemPedido item, Cookie auth) {
        return delete("/api/item_pedido/deletar/" + item.getIdItemPedido())
                .cookie(auth, new Cookie("XSRF-TOKEN", "teste"))
                .header("X-XSRF-TOKEN", "teste");
    }

    private Pedido criarPedido(String status) {
        Cliente cliente = new Cliente();
        cliente.setRazaoSocial("Cliente LTDA");
        cliente.setCnpj("1111111100019" + proximoNumeroDia);
        cliente.setInscricaoEstadual("ISENTO");
        cliente = clienteRepository.saveAndFlush(cliente);

        Distribuidor distribuidor = new Distribuidor();
        distribuidor.setRazaoSocial("Distribuidor LTDA");
        distribuidor.setCnpj("2222222200019" + proximoNumeroDia);
        distribuidor.setInscricaoEstadual("ISENTO");
        distribuidor = distribuidorRepository.saveAndFlush(distribuidor);

        Pedido pedido = new Pedido();
        pedido.setDataPedido(LocalDate.now());
        pedido.setNumeroDia(proximoNumeroDia++);
        pedido.setStatusPedido(status);
        pedido.setValorTotalDistr(new BigDecimal("50.00"));
        pedido.setValorTotalCliente(new BigDecimal("70.00"));
        pedido.setUsuario(vendedor);
        pedido.setCliente(cliente);
        pedido.setDistribuidor(distribuidor);
        return pedidoRepository.saveAndFlush(pedido);
    }

    private ItemPedido criarItem(Pedido pedido, String totalDistr, String totalCliente) {
        Produto produto = new Produto();
        produto.setNome("Produto");
        produto = produtoRepository.saveAndFlush(produto);

        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(1);
        item.setVlrUnitDistr(new BigDecimal(totalDistr));
        item.setVlrTotalDistr(new BigDecimal(totalDistr));
        item.setVlrUnitCliente(new BigDecimal(totalCliente));
        item.setVlrTotalCliente(new BigDecimal(totalCliente));
        return itemPedidoRepository.saveAndFlush(item);
    }

    private Usuario criarUsuario(String email) {
        Role role = new Role();
        role.setRole(Roles.VEND);
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
