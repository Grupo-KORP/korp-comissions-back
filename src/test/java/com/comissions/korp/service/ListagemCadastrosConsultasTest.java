package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteDTO.ClientePedidoResponseDTO;
import com.comissions.korp.DTO.ClienteDTO.ListarClientesResponseDTO;
import com.comissions.korp.DTO.DistribuidorDTO.DistribuidorPedidoResponseDTO;
import com.comissions.korp.DTO.DistribuidorDTO.ListarDistribuidoresResponseDTO;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Contato;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.Endereco;
import com.comissions.korp.repository.ClienteRepository;
import com.comissions.korp.repository.ContatoRepository;
import com.comissions.korp.repository.DistribuidorRepository;
import com.comissions.korp.repository.EnderecoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * As listagens de cliente e distribuidor buscam endereços e contatos em lote:
 * o número de consultas SQL não cresce com a quantidade de itens (sem N+1).
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:korp-listagem-consultas-it;MODE=MySQL",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@ActiveProfiles("test")
@Transactional
class ListagemCadastrosConsultasTest {

    private static final int QUANTIDADE_COM_ENDERECO = 5;

    @Autowired private ClienteService clienteService;
    @Autowired private DistribuidorService distribuidorService;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private DistribuidorRepository distribuidorRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private ContatoRepository contatoRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private EntityManagerFactory entityManagerFactory;

    private Statistics estatisticas;

    @BeforeEach
    void prepararDados() {
        for (int i = 1; i <= QUANTIDADE_COM_ENDERECO; i++) {
            Cliente cliente = salvarCliente("Cliente " + i, "1000000000000" + i);
            salvarEndereco(cliente, null, "Rua Cliente " + i);
            salvarContato(cliente, null, "Contato A " + i, true);
            salvarContato(cliente, null, "Contato B " + i, true);
            salvarContato(cliente, null, "Contato inativo " + i, false);

            Distribuidor distribuidor = salvarDistribuidor("Distribuidor " + i, "2000000000000" + i);
            salvarEndereco(null, distribuidor, "Rua Distribuidor " + i);
            salvarContato(null, distribuidor, "Contato D " + i, true);
        }
        // Cadastros sem endereço e sem contato também precisam aparecer
        salvarCliente("Cliente sem endereco", "19999999999999");
        salvarDistribuidor("Distribuidor sem endereco", "29999999999999");

        entityManager.flush();
        entityManager.clear();
        estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();
    }

    @Test
    void listaDeClientesDoPedido_deveUsarPoucasConsultas() {
        List<ClientePedidoResponseDTO> lista = clienteService.listarTodosPedidoDto();

        // 1 clientes + 1 endereços + 1 contatos (antes: 1 + 2 por cliente = 13)
        assertThat(estatisticas.getPrepareStatementCount()).isLessThanOrEqualTo(3);
        assertThat(lista).hasSize(QUANTIDADE_COM_ENDERECO + 1);
        ClientePedidoResponseDTO primeiro = porNome(lista, "Cliente 1");
        assertThat(primeiro.getEndereco()).isEqualTo("Rua Cliente 1");
        assertThat(primeiro.getContatos()).extracting(contato -> contato.getNome())
                .containsExactly("Contato A 1", "Contato B 1");
        ClientePedidoResponseDTO semEndereco = porNome(lista, "Cliente sem endereco");
        assertThat(semEndereco.getEndereco()).isNull();
        assertThat(semEndereco.getContatos()).isEmpty();
    }

    @Test
    void paginaDeClientes_deveUsarPoucasConsultas() {
        List<ListarClientesResponseDTO> pagina = clienteService
                .listarTodosClientes(null, PageRequest.of(0, 10, Sort.by("razaoSocial")))
                .getContent();

        // 1 página (+ 1 contagem, se precisar) + 1 endereços + 1 contatos
        assertThat(estatisticas.getPrepareStatementCount()).isLessThanOrEqualTo(4);
        assertThat(pagina).hasSize(QUANTIDADE_COM_ENDERECO + 1);
        assertThat(pagina).filteredOn(item -> "Cliente 3".equals(item.getRazaoSocial()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getLogradouro()).isEqualTo("Rua Cliente 3");
                    assertThat(item.getContatos()).hasSize(2);
                });
    }

    @Test
    void listaEPaginaDeDistribuidores_devemUsarPoucasConsultas() {
        List<DistribuidorPedidoResponseDTO> lista = distribuidorService.listarTodosPedidoDto();
        assertThat(estatisticas.getPrepareStatementCount()).isLessThanOrEqualTo(3);
        assertThat(lista).hasSize(QUANTIDADE_COM_ENDERECO + 1);
        assertThat(lista).filteredOn(item -> "Distribuidor 2".equals(item.getRazaoSocial()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getEndereco()).isEqualTo("Rua Distribuidor 2");
                    assertThat(item.getContatos()).extracting(contato -> contato.getNome()).containsExactly("Contato D 2");
                });

        estatisticas.clear();
        List<ListarDistribuidoresResponseDTO> pagina = distribuidorService
                .listarTodosDistribuidores(null, PageRequest.of(0, 10, Sort.by("razaoSocial")))
                .getContent();
        assertThat(estatisticas.getPrepareStatementCount()).isLessThanOrEqualTo(4);
        assertThat(pagina).hasSize(QUANTIDADE_COM_ENDERECO + 1);
    }

    private ClientePedidoResponseDTO porNome(List<ClientePedidoResponseDTO> lista, String razaoSocial) {
        return lista.stream().filter(item -> razaoSocial.equals(item.getRazaoSocial())).findFirst().orElseThrow();
    }

    private Cliente salvarCliente(String razaoSocial, String cnpj) {
        Cliente cliente = new Cliente();
        cliente.setRazaoSocial(razaoSocial);
        cliente.setCnpj(cnpj);
        cliente.setInscricaoEstadual("ISENTO");
        return clienteRepository.save(cliente);
    }

    private Distribuidor salvarDistribuidor(String razaoSocial, String cnpj) {
        Distribuidor distribuidor = new Distribuidor();
        distribuidor.setRazaoSocial(razaoSocial);
        distribuidor.setCnpj(cnpj);
        distribuidor.setInscricaoEstadual("ISENTO");
        return distribuidorRepository.save(distribuidor);
    }

    private void salvarEndereco(Cliente cliente, Distribuidor distribuidor, String logradouro) {
        Endereco endereco = new Endereco();
        endereco.setLogradouro(logradouro);
        endereco.setCidade("Sao Paulo");
        endereco.setEstado("SP");
        endereco.setCep("01000000");
        endereco.setCliente(cliente);
        endereco.setDistribuidor(distribuidor);
        enderecoRepository.save(endereco);
    }

    private void salvarContato(Cliente cliente, Distribuidor distribuidor, String nome, boolean ativo) {
        Contato contato = new Contato();
        contato.setNome(nome);
        contato.setCliente(cliente);
        contato.setDistribuidor(distribuidor);
        contato.setAtivo(ativo);
        contatoRepository.save(contato);
    }
}
