package com.comissions.korp.service;

import com.comissions.korp.DTO.HomeVendedorDTO.FiltroStatusVenda;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO.LinhaPainelDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO.ResumoDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.ParcelaDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.VendaResumoDTO;
import com.comissions.korp.DTO.PaginaDTO;
import com.comissions.korp.config.CacheConfig;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.ENUM.Roles;
import com.comissions.korp.entity.Pedido;
import com.comissions.korp.entity.Role;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.repository.ClienteRepository;
import com.comissions.korp.repository.DistribuidorRepository;
import com.comissions.korp.repository.PedidoRepository;
import com.comissions.korp.repository.RoleRepository;
import com.comissions.korp.repository.UsuarioRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cache do painel do vendedor: a 2ª chamada não vai ao banco, escritas apagam o cache
 * e o painel guardado volta do JSON igual. Sem @Transactional de propósito: cada chamada
 * do service faz commit, como em produção.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:korp-cache-painel-it;MODE=MySQL",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@ActiveProfiles("test")
class PainelVendedorCacheTest {

    @Autowired private HomeVendedorService homeVendedorService;
    @Autowired private PedidoService pedidoService;
    @Autowired private CacheManager cacheManager;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private EntityManagerFactory entityManagerFactory;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private DistribuidorRepository distribuidorRepository;
    @Autowired private PedidoRepository pedidoRepository;

    private Statistics estatisticas;
    private Usuario vendedor;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(CacheConfig.PAINEL_VENDEDOR).clear();
        estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        vendedor = criarVendedor();
    }

    @Test
    void segundaChamadaIgualNaoConsultaOBanco() {
        HomeVendedorPainelDTO primeira = buscarPainelDoMes(null, null);
        assertThat(primeira.linhas().content()).hasSize(1);

        estatisticas.clear();
        HomeVendedorPainelDTO segunda = buscarPainelDoMes(null, null);

        assertThat(estatisticas.getPrepareStatementCount()).isZero();
        assertThat(segunda.linhas().totalElements()).isEqualTo(1);
    }

    @Test
    void semMesNaUrlUsaAMesmaChaveDoMesAtual() {
        LocalDate hoje = LocalDate.now();
        buscarPainelDoMes(null, null);

        estatisticas.clear();
        buscarPainelDoMes(hoje.getYear(), hoje.getMonthValue());

        assertThat(estatisticas.getPrepareStatementCount()).isZero();
    }

    @Test
    void excluirPedidoApagaOCacheEAProximaLeituraConsultaOBanco() {
        Pedido pedido = pedidoRepository.findByUsuario_IdUsuarioAndAtivoTrue(vendedor.getIdUsuario()).getFirst();
        buscarPainelDoMes(null, null);

        pedidoService.deletarPedido(pedido.getIdPedido(), vendedor.getIdUsuario());

        estatisticas.clear();
        HomeVendedorPainelDTO depois = buscarPainelDoMes(null, null);
        assertThat(estatisticas.getPrepareStatementCount()).isPositive();
        assertThat(depois.linhas().content()).isEmpty();
    }

    @Test
    void painelGuardadoVoltaDoJsonSemPerderNada() {
        // Mesmo conversor configurado para o Redis no CacheConfig
        JacksonJsonRedisSerializer<HomeVendedorPainelDTO> conversor =
                new JacksonJsonRedisSerializer<>(jsonMapper, HomeVendedorPainelDTO.class);
        HomeVendedorPainelDTO original = painelDeExemplo();

        byte[] json = conversor.serialize(original);
        HomeVendedorPainelDTO devolvido = conversor.deserialize(json);

        assertThat(new String(json)).contains("\"content\"", "\"totalPages\"", "251126-P03");
        assertThat(devolvido).usingRecursiveComparison().isEqualTo(original);
    }

    private HomeVendedorPainelDTO buscarPainelDoMes(Integer ano, Integer mes) {
        return homeVendedorService.buscarPainel(
                vendedor.getIdUsuario(), ano, mes, null, FiltroStatusVenda.TODAS, PageRequest.of(0, 5));
    }

    /** Vendedor com um pedido em andamento feito hoje (gera uma linha VENDA no painel do mês). */
    private Usuario criarVendedor() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Role role = new Role();
        role.setRole(Roles.VEND);
        role = roleRepository.saveAndFlush(role);

        Usuario usuario = new Usuario();
        usuario.setNome("vend-cache-" + sufixo);
        usuario.setEmail("vend-cache-" + sufixo + "@korp.com");
        usuario.setSenha("{noop}senha");
        usuario.setPrimeiroAcesso(false);
        usuario.setAtivo(true);
        usuario.setRoles(role);
        usuario = usuarioRepository.saveAndFlush(usuario);

        Cliente cliente = new Cliente();
        cliente.setRazaoSocial("Cliente " + sufixo);
        cliente.setCnpj("C" + sufixo);
        cliente.setInscricaoEstadual("ISENTO");
        cliente = clienteRepository.saveAndFlush(cliente);

        Distribuidor distribuidor = new Distribuidor();
        distribuidor.setRazaoSocial("Distribuidor " + sufixo);
        distribuidor.setCnpj("D" + sufixo);
        distribuidor.setInscricaoEstadual("ISENTO");
        distribuidor = distribuidorRepository.saveAndFlush(distribuidor);

        Pedido pedido = new Pedido();
        pedido.setDataPedido(LocalDate.now());
        pedido.setNumeroDia(pedidoRepository.maiorNumeroDoDia(LocalDate.now()) + 1);
        pedido.setStatusPedido("EM_ANDAMENTO");
        pedido.setValorTotalDistr(new BigDecimal("50.00"));
        pedido.setValorTotalCliente(new BigDecimal("70.00"));
        pedido.setUsuario(usuario);
        pedido.setCliente(cliente);
        pedido.setDistribuidor(distribuidor);
        pedidoRepository.saveAndFlush(pedido);
        return usuario;
    }

    private HomeVendedorPainelDTO painelDeExemplo() {
        ParcelaDTO parcela = new ParcelaDTO();
        parcela.setIdParcela(7);
        parcela.setPedidoId(30);
        parcela.setCodigoPedido("251126-P03");
        parcela.setCliente("Acme");
        parcela.setLabel("Parcela 1/2");
        parcela.setValor(new BigDecimal("150.00"));
        parcela.setNumeroParcela(1);
        parcela.setTotalParcelas(2);
        parcela.setDataVencimento(LocalDate.of(2026, 12, 25));
        parcela.setStatus("PENDENTE");

        VendaResumoDTO venda = new VendaResumoDTO();
        venda.setId("251126-P03");
        venda.setIdPedido(30);
        venda.setNome("251126-P03");
        venda.setCliente("Contato - Acme");
        venda.setComissao("R$ 300,00");
        venda.setValorComissao(new BigDecimal("300.00"));
        venda.setStatus("Aprovada");
        venda.setTipo("aprovada");
        venda.setDataVenda(LocalDate.of(2026, 11, 25));
        venda.setParcelas(List.of(parcela));
        venda.setParcelasDaVenda(List.of(parcela));

        ResumoDTO resumo = new ResumoDTO(1, 0, 1, new BigDecimal("300.00"), 0, 2, "Novo", "+100%", "0%");
        PaginaDTO<LinhaPainelDTO> linhas = new PaginaDTO<>(
                List.of(LinhaPainelDTO.venda(venda), LinhaPainelDTO.pagamento(venda, parcela)), 0, 5, 2, 1);

        return new HomeVendedorPainelDTO(2026, 11, "Novembro", resumo, List.of(parcela), linhas);
    }
}
