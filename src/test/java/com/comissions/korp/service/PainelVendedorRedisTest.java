package com.comissions.korp.service;

import com.comissions.korp.DTO.HomeVendedorDTO.FiltroStatusVenda;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Cache do painel num Redis de verdade (o do docker-compose.yml, em localhost:6379).
 * É pulado automaticamente quando o Redis não está rodando, para não quebrar a suíte.
 * Para rodar: docker compose up -d
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:korp-cache-redis-it;MODE=MySQL",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.cache.type=redis",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "korp.cache.painel-vendedor.ttl=10m",
        // Prefixo só dos testes: os dados do banco de teste nunca aparecem no painel da aplicação
        "korp.cache.prefixo=" + PainelVendedorRedisTest.PREFIXO
})
@ActiveProfiles("test")
class PainelVendedorRedisTest {

    static final String PREFIXO = "korp-teste:";

    @Autowired private HomeVendedorService homeVendedorService;
    @Autowired private PedidoService pedidoService;
    @Autowired private StringRedisTemplate redis;
    @Autowired private EntityManagerFactory entityManagerFactory;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private DistribuidorRepository distribuidorRepository;
    @Autowired private PedidoRepository pedidoRepository;

    private Statistics estatisticas;
    private Usuario vendedor;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        assumeTrue(redisDisponivel(), "Redis não está rodando em localhost:6379 (docker compose up -d)");
        estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        criarVendedorComPedido();
    }

    @AfterEach
    void apagarChavesDoTeste() {
        if (redisDisponivel()) {
            Set<String> chaves = redis.keys(PREFIXO + "*");
            if (chaves != null && !chaves.isEmpty()) {
                redis.delete(chaves);
            }
        }
    }

    @Test
    void guardaOPainelNoRedisComTtlEJsonEApagaQuandoOPedidoMuda() {
        String chave = chaveDoMesAtual();

        // 1ª chamada (miss): monta pelo banco e grava no Redis
        HomeVendedorPainelDTO primeira = buscarPainel();
        assertThat(primeira.linhas().content()).hasSize(1);
        assertThat(redis.hasKey(chave)).isTrue();
        Long ttl = redis.getExpire(chave);
        assertThat(ttl).isBetween(1L, 600L);
        assertThat(redis.opsForValue().get(chave)).contains("\"content\"", pedido.getCodigo());

        // 2ª chamada (hit): vem do Redis, sem nenhuma consulta SQL
        estatisticas.clear();
        HomeVendedorPainelDTO segunda = buscarPainel();
        assertThat(estatisticas.getPrepareStatementCount()).isZero();
        assertThat(segunda.linhas().content().getFirst().venda().getId()).isEqualTo(pedido.getCodigo());

        // Escrita (exclusão do pedido): apaga o cache e a próxima leitura vai ao banco
        pedidoService.deletarPedido(pedido.getIdPedido(), vendedor.getIdUsuario());
        assertThat(redis.hasKey(chave)).isFalse();

        estatisticas.clear();
        HomeVendedorPainelDTO depois = buscarPainel();
        assertThat(estatisticas.getPrepareStatementCount()).isPositive();
        assertThat(depois.linhas().content()).isEmpty();
    }

    private HomeVendedorPainelDTO buscarPainel() {
        return homeVendedorService.buscarPainel(
                vendedor.getIdUsuario(), null, null, null, FiltroStatusVenda.TODAS, PageRequest.of(0, 5));
    }

    private String chaveDoMesAtual() {
        LocalDate hoje = LocalDate.now();
        return PREFIXO + "painelVendedor::" + vendedor.getIdUsuario() + ":" + hoje.getYear() + "-" + hoje.getMonthValue()
                + ":mes:TODAS:p0:s5";
    }

    private boolean redisDisponivel() {
        try (var conexao = redis.getRequiredConnectionFactory().getConnection()) {
            return "PONG".equals(conexao.ping());
        } catch (RuntimeException indisponivel) {
            return false;
        }
    }

    private void criarVendedorComPedido() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Role role = new Role();
        role.setRole(Roles.VEND);
        role = roleRepository.saveAndFlush(role);

        vendedor = new Usuario();
        vendedor.setNome("vend-redis-" + sufixo);
        vendedor.setEmail("vend-redis-" + sufixo + "@korp.com");
        vendedor.setSenha("{noop}senha");
        vendedor.setPrimeiroAcesso(false);
        vendedor.setAtivo(true);
        vendedor.setRoles(role);
        vendedor = usuarioRepository.saveAndFlush(vendedor);

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

        pedido = new Pedido();
        pedido.setDataPedido(LocalDate.now());
        pedido.setNumeroDia(pedidoRepository.maiorNumeroDoDia(LocalDate.now()) + 1);
        pedido.setStatusPedido("EM_ANDAMENTO");
        pedido.setValorTotalDistr(new BigDecimal("50.00"));
        pedido.setValorTotalCliente(new BigDecimal("70.00"));
        pedido.setUsuario(vendedor);
        pedido.setCliente(cliente);
        pedido.setDistribuidor(distribuidor);
        pedido = pedidoRepository.saveAndFlush(pedido);
    }
}
