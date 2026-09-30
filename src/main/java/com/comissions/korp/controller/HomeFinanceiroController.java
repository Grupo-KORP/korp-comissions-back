package com.comissions.korp.controller;

import com.comissions.korp.DTO.HomeFinanceiroDTO.EvolucaoVendasMesDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.PedidoRecenteDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.RankingVendedorDTO;
import com.comissions.korp.DTO.HomeFinanceiroDTO.ResumoFinanceiroDTO;
import com.comissions.korp.service.financeiro.EvolucaoVendasService;
import com.comissions.korp.service.financeiro.PedidosFinanceiroService;
import com.comissions.korp.service.financeiro.PeriodoFinanceiro;
import com.comissions.korp.service.financeiro.RankingVendedoresService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Painel financeiro. Cada bloco da tela tem seu endpoint, e todos recebem o mesmo filtro:
 * ano/mes opcionais (sem nada: mês atual; mes sem ano: ano atual).
 */
@RestController
@RequestMapping("/financeiro/home")
public class HomeFinanceiroController {

    private final ResumoFinanceiroService resumoService;
    private final EvolucaoVendasService evolucaoService;
    private final RankingVendedoresService rankingService;
    private final PedidosFinanceiroService pedidosService;

    public HomeFinanceiroController(
            ResumoFinanceiroService resumoService,
            EvolucaoVendasService evolucaoService,
            RankingVendedoresService rankingService,
            PedidosFinanceiroService pedidosService
    ) {
        this.resumoService = resumoService;
        this.evolucaoService = evolucaoService;
        this.rankingService = rankingService;
        this.pedidosService = pedidosService;
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
}
