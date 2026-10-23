package com.comissions.korp.service;

import com.comissions.korp.DTO.ClienteDTO.ClienteContatosRequestDTO;
import com.comissions.korp.DTO.ContatoDTO.ContatoRequestDTO;
import com.comissions.korp.DTO.ContatoDTO.ContatoResponseDTO;
import com.comissions.korp.config.utils.SecurityUtils;
import com.comissions.korp.entity.Cliente;
import com.comissions.korp.entity.Contato;
import com.comissions.korp.entity.Distribuidor;
import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.OperacaoNaoPermitida;
import com.comissions.korp.exception.RecursoNaoEncontrado;
import com.comissions.korp.exception.RequisicaoInvalida;
import com.comissions.korp.repository.ContatoRepository;
import com.comissions.korp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContatoServiceTest {

    @Mock
    private ContatoRepository contatoRepository;
    @Mock
    private ClienteService clienteService;
    @Mock
    private DistribuidorService distribuidorService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private SecurityUtils securityUtils;

    private ContatoService contatoService;

    @BeforeEach
    void setUp() {
        contatoService = new ContatoService(contatoRepository, clienteService, distribuidorService, usuarioRepository);
        ReflectionTestUtils.setField(contatoService, "securityUtils", securityUtils);
    }

    @Test
    void criar_deveSalvarContatoDoCliente_quandoDadosSaoValidos() {
        Cliente cliente = cliente(10);
        when(clienteService.buscarClientePorId(10)).thenReturn(cliente);
        when(securityUtils.getUsuarioIdAutenticado()).thenReturn(99);
        when(usuarioRepository.findById(99)).thenReturn(Optional.of(new Usuario()));
        when(contatoRepository.save(any(Contato.class))).thenAnswer(invocation -> {
            Contato contato = invocation.getArgument(0);
            contato.setIdContato(1);
            return contato;
        });

        ContatoRequestDTO request = request("Maria", "maria@empresa.com", "(11) 99999-8888");
        request.setIdCliente(10);

        ContatoResponseDTO response = contatoService.criar(request);

        assertThat(response.getIdContato()).isEqualTo(1);
        assertThat(response.getIdCliente()).isEqualTo(10);
        assertThat(response.getIdDistribuidor()).isNull();
    }

    @Test
    void criar_deveFalhar_quandoNaoInformaDonoOuInformaOsDois() {
        ContatoRequestDTO semDono = request("Maria", "maria@empresa.com", "11999998888");
        ContatoRequestDTO comDoisDonos = request("Maria", "maria@empresa.com", "11999998888");
        comDoisDonos.setIdCliente(10);
        comDoisDonos.setIdDistribuidor(20);

        assertThatThrownBy(() -> contatoService.criar(semDono)).isInstanceOf(RequisicaoInvalida.class);
        assertThatThrownBy(() -> contatoService.criar(comDoisDonos)).isInstanceOf(RequisicaoInvalida.class);
        verify(contatoRepository, never()).save(any());
    }

    @Test
    void criar_deveFalhar_quandoNomeEmailOuTelefoneSaoInvalidos() {
        ContatoRequestDTO semNome = request(" ", "maria@empresa.com", "11999998888");
        semNome.setIdCliente(10);
        ContatoRequestDTO emailInvalido = request("Maria", "maria.empresa.com", "11999998888");
        emailInvalido.setIdCliente(10);
        ContatoRequestDTO telefoneInvalido = request("Maria", "maria@empresa.com", "1234");
        telefoneInvalido.setIdCliente(10);

        assertThatThrownBy(() -> contatoService.criar(semNome)).isInstanceOf(RequisicaoInvalida.class);
        assertThatThrownBy(() -> contatoService.criar(emailInvalido)).isInstanceOf(RequisicaoInvalida.class);
        assertThatThrownBy(() -> contatoService.criar(telefoneInvalido)).isInstanceOf(RequisicaoInvalida.class);
        verify(contatoRepository, never()).save(any());
    }

    @Test
    void atualizar_devePermitirEditarDadosMantendoOMesmoDono() {
        Contato contato = contatoDoCliente(1, 10);
        when(contatoRepository.findById(1)).thenReturn(Optional.of(contato));
        when(contatoRepository.save(any(Contato.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContatoRequestDTO request = request("Novo Nome", "novo@empresa.com", "11977776666");
        request.setIdCliente(10);

        ContatoResponseDTO response = contatoService.atualizar(1, request);

        assertThat(response.getNome()).isEqualTo("Novo Nome");
        assertThat(response.getIdCliente()).isEqualTo(10);
    }

    @Test
    void atualizar_deveFalhar_quandoTentaMoverContatoParaOutroDono() {
        Contato contato = contatoDoCliente(1, 10);
        when(contatoRepository.findById(1)).thenReturn(Optional.of(contato));

        ContatoRequestDTO outroCliente = request("Maria", "maria@empresa.com", "11999998888");
        outroCliente.setIdCliente(11);
        ContatoRequestDTO distribuidor = request("Maria", "maria@empresa.com", "11999998888");
        distribuidor.setIdDistribuidor(20);

        assertThatThrownBy(() -> contatoService.atualizar(1, outroCliente)).isInstanceOf(RequisicaoInvalida.class);
        assertThatThrownBy(() -> contatoService.atualizar(1, distribuidor)).isInstanceOf(RequisicaoInvalida.class);
        verify(contatoRepository, never()).save(any());
    }

    @Test
    void deletar_deveRecusar_quandoEOUltimoContatoAtivoDoCliente() {
        Contato contato = contatoDoCliente(1, 10);
        when(contatoRepository.findById(1)).thenReturn(Optional.of(contato));
        when(contatoRepository.countByClienteAndAtivoTrue(contato.getCliente())).thenReturn(1L);

        assertThatThrownBy(() -> contatoService.deletar(1))
                .isInstanceOf(OperacaoNaoPermitida.class)
                .hasMessageContaining("pelo menos um contato");
        assertThat(contato.getAtivo()).isTrue();
        verify(contatoRepository, never()).save(any());
    }

    @Test
    void deletar_deveRecusar_quandoEOUltimoContatoAtivoDoDistribuidor() {
        Contato contato = contatoDoDistribuidor(2, 20);
        when(contatoRepository.findById(2)).thenReturn(Optional.of(contato));
        when(contatoRepository.countByDistribuidorAndAtivoTrue(contato.getDistribuidor())).thenReturn(1L);

        assertThatThrownBy(() -> contatoService.deletar(2)).isInstanceOf(OperacaoNaoPermitida.class);
        assertThat(contato.getAtivo()).isTrue();
    }

    @Test
    void deletar_deveInativarContato_quandoExistemOutrosContatosAtivos() {
        Contato contato = contatoDoCliente(1, 10);
        when(contatoRepository.findById(1)).thenReturn(Optional.of(contato));
        when(contatoRepository.countByClienteAndAtivoTrue(contato.getCliente())).thenReturn(2L);

        contatoService.deletar(1);

        assertThat(contato.getAtivo()).isFalse();
        verify(contatoRepository).save(contato);
    }

    @Test
    void deletar_deveFalhaComNaoEncontrado_quandoContatoNaoExiste() {
        when(contatoRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contatoService.deletar(404)).isInstanceOf(RecursoNaoEncontrado.class);
    }

    @Test
    void atualizarContatosCliente_deveFalharComNaoEncontrado_quandoContatoNaoExiste() {
        when(contatoRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contatoService.atualizarContatosCliente(requestEmLote(404), 10))
                .isInstanceOf(RecursoNaoEncontrado.class);
    }

    @Test
    void atualizarContatosCliente_deveRecusar_quandoContatoPertenceAOutroCliente() {
        when(contatoRepository.findById(1)).thenReturn(Optional.of(contatoDoCliente(1, 11)));

        assertThatThrownBy(() -> contatoService.atualizarContatosCliente(requestEmLote(1), 10))
                .isInstanceOf(RequisicaoInvalida.class);
        verify(contatoRepository, never()).save(any());
    }

    private ClienteContatosRequestDTO requestEmLote(Integer idContato) {
        ClienteContatosRequestDTO.ContatoItemDTO item = new ClienteContatosRequestDTO.ContatoItemDTO();
        item.setIdContato(idContato);
        item.setNome("Maria");
        item.setEmail("maria@empresa.com");
        item.setTelefone("11999998888");

        ClienteContatosRequestDTO request = new ClienteContatosRequestDTO();
        request.setContatos(List.of(item));
        return request;
    }

    private ContatoRequestDTO request(String nome, String email, String telefone) {
        ContatoRequestDTO request = new ContatoRequestDTO();
        request.setNome(nome);
        request.setEmail(email);
        request.setTelefone(telefone);
        return request;
    }

    private Cliente cliente(Integer id) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(id);
        return cliente;
    }

    private Contato contatoDoCliente(Integer idContato, Integer idCliente) {
        Contato contato = new Contato();
        contato.setIdContato(idContato);
        contato.setNome("Maria");
        contato.setEmail("maria@empresa.com");
        contato.setTelefone("11999998888");
        contato.setAtivo(true);
        contato.setCliente(cliente(idCliente));
        return contato;
    }

    private Contato contatoDoDistribuidor(Integer idContato, Integer idDistribuidor) {
        Distribuidor distribuidor = new Distribuidor();
        distribuidor.setIdDistribuidor(idDistribuidor);

        Contato contato = new Contato();
        contato.setIdContato(idContato);
        contato.setNome("Joao");
        contato.setEmail("joao@dist.com");
        contato.setTelefone("11988887777");
        contato.setAtivo(true);
        contato.setDistribuidor(distribuidor);
        return contato;
    }
}
