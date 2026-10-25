package com.comissions.korp.controller;

import com.comissions.korp.DTO.HomeFinanceiroDTO.ComissoesPainelDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.AtualizarStatusComissaoRequestDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.EvolucaoVendasMesDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.PedidoRecenteDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.RankingVendedorDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.ResumoFinanceiroDTO;
import com.comissions.korp.service.financeiro.ComissoesFinanceiroService;
import com.comissions.korp.service.financeiro.EvolucaoVendasService;
import com.comissions.korp.service.financeiro.PedidosFinanceiroService;
import com.comissions.korp.service.financeiro.PeriodoFinanceiro;
import com.comissions.korp.service.financeiro.RankingVendedoresService;
import com.comissions.korp.entity.ENUM.StatusComissao;
import com.comissions.korp.service.financeiro.ResumoFinanceiroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Painel financeiro. Cada bloco da tela tem seu endpoint, e todos recebem o mesmo filtro:
 * ano/mes opcionais (sem nada: mês atual; mes sem ano: ano atual).
 */
@RestController
@RequestMapping("/api/financeiro/home")
public class HomeFinanceiroController {

    private final ResumoFinanceiroService resumoService;
    private final EvolucaoVendasService evolucaoService;
    private final RankingVendedoresService rankingService;
    private final PedidosFinanceiroService pedidosService;
        private final ComissoesFinanceiroService comissoesService;

    public HomeFinanceiroController(
            ResumoFinanceiroService resumoService,
            EvolucaoVendasService evolucaoService,
            RankingVendedoresService rankingService,
            PedidosFinanceiroService pedidosService,
            ComissoesFinanceiroService comissoesService
    ) {
        this.resumoService = resumoService;
        this.evolucaoService = evolucaoService;
        this.rankingService = rankingService;
        this.pedidosService = pedidosService;
        this.comissoesService = comissoesService;
    }

    @GetMapping("/resumo")
    @Operation(
            summary = "Buscar cards do painel financeiro",
            description = "Faturamento, vendas, pagamentos pendentes e comissões do mês, com tendência sobre o mês anterior."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<ResumoFinanceiroDTO> buscarResumo(
            @RequestParam(required = false) @Min(2000) @Max(2100) Integer ano,
            @RequestParam(required = false) @Min(1) @Max(12) Integer mes
    ) {
        return ResponseEntity.ok(resumoService.buscarResumo(PeriodoFinanceiro.de(ano, mes)));
    }

    @GetMapping("/evolucao-vendas")
    @Operation(
            summary = "Buscar evolução de vendas",
            description = "Faturamento e quantidade de vendas dos 6 meses que terminam no mês filtrado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evolução retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<List<EvolucaoVendasMesDTO>> buscarEvolucaoVendas(
            @RequestParam(required = false) @Min(2000) @Max(2100) Integer ano,
            @RequestParam(required = false) @Min(1) @Max(12) Integer mes
    ) {
        return ResponseEntity.ok(evolucaoService.buscarEvolucao(PeriodoFinanceiro.de(ano, mes)));
    }

    @GetMapping("/ranking-vendedores")
    @Operation(
            summary = "Buscar ranking de comissão por vendedor",
            description = "Vendedores com maior comissão no mês filtrado (limite padrão 5)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ranking retornado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<List<RankingVendedorDTO>> buscarRankingVendedores(
            @RequestParam(required = false) @Min(2000) @Max(2100) Integer ano,
            @RequestParam(required = false) @Min(1) @Max(12) Integer mes,
            @RequestParam(required = false) @Min(1) @Max(50) Integer limite
    ) {
        return ResponseEntity.ok(rankingService.buscarRanking(PeriodoFinanceiro.de(ano, mes), limite));
    }

    @GetMapping("/pedidos")
    @Operation(
            summary = "Buscar pedidos do período",
            description = "Página de pedidos feitos no mês filtrado, do mais recente para o mais antigo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos retornados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<Page<PedidoRecenteDTO>> buscarPedidos(
            @RequestParam(required = false) @Min(2000) @Max(2100) Integer ano,
            @RequestParam(required = false) @Min(1) @Max(12) Integer mes,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return ResponseEntity.ok(pedidosService.buscarPedidos(PeriodoFinanceiro.de(ano, mes), pageable));
    }

    @GetMapping("/comissoes")
    @Operation(
            summary = "Buscar parcelas de comissão do período",
            description = "Retorna contadores das KPIs, parcelas paginadas e filtradas por busca/status e as cinco próximas liberações."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comissões retornadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<ComissoesPainelDTO> buscarComissoes(
            @RequestParam(required = false) @Min(2000) @Max(2100) Integer ano,
            @RequestParam(required = false) @Min(1) @Max(12) Integer mes,
            @RequestParam(required = false) String busca,
                        @RequestParam(required = false) StatusComissao status,
                        @RequestParam(defaultValue = "0") @Min(0) Integer pagina,
                        @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer tamanho
    ) {
                return ResponseEntity.ok(comissoesService.buscarPainel(
                                PeriodoFinanceiro.de(ano, mes), busca, status, pagina, tamanho));
    }

        @PatchMapping("/comissoes/{idComissao}/status")
        @Operation(summary = "Atualizar status da comissão", description = "Atualiza o status de uma comissão pelo ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Status inválido"),
                        @ApiResponse(responseCode = "404", description = "Comissão não encontrada")
        })
        public ResponseEntity<StatusComissao> atualizarStatusComissao(
                        @PathVariable Integer idComissao,
                        @Valid @RequestBody AtualizarStatusComissaoRequestDTO request
        ) {
                return ResponseEntity.ok(comissoesService.atualizarStatus(idComissao, request.status()));
        }
}
