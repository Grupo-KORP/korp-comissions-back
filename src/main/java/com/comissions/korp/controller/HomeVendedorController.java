package com.comissions.korp.controller;

import com.comissions.korp.DTO.HomeVendedorDTO.FiltroStatusVenda;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorPainelDTO;
import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO.DetalheVendaDTO;
import com.comissions.korp.config.utils.SecurityUtils;
import com.comissions.korp.service.HomeVendedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendedor/home")
public class HomeVendedorController {

    private final HomeVendedorService homeVendedorService;
    private final SecurityUtils securityUtils;

    public HomeVendedorController(HomeVendedorService homeVendedorService, SecurityUtils securityUtils) {
        this.homeVendedorService = homeVendedorService;
        this.securityUtils = securityUtils;
    }

    /**
     * GET /vendedor/home?ano=2026&mes=9&dia=18&status=LIBERADAS&page=0&size=5
     * - ano/mes/dia opcionais (sem nada: mês atual; mes sem ano: ano atual; dia exige mes)
     * - status: TODAS (padrão) | LIBERADAS | PENDENTES
     * - page (0-based) e size seguem o padrão do Spring; a ordenação é fixa no service
     */
    @GetMapping
    @Operation(
            summary = "Buscar painel do vendedor",
            description = "Retorna o resumo do período e uma página de vendas do vendedor autenticado, "
                    + "filtradas por status (TODAS, LIBERADAS, PENDENTES) e por período (mês ou dia)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Painel retornado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<HomeVendedorPainelDTO> buscarPainel(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer dia,
            @RequestParam(defaultValue = "TODAS") FiltroStatusVenda status,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        Integer idVendedor = securityUtils.getUsuarioIdAutenticado();
        return ResponseEntity.ok(homeVendedorService.buscarPainel(idVendedor, ano, mes, dia, status, pageable));
    }

    @GetMapping("/vendas/{idPedido}")
    @Operation(
            summary = "Buscar detalhe de uma venda",
            description = "Retorna cliente, distribuidor, produto e dados do pedido de uma venda do vendedor autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhe retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Venda não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<DetalheVendaDTO> buscarDetalheVenda(@PathVariable Integer idPedido) {
        Integer idVendedor = securityUtils.getUsuarioIdAutenticado();
        return ResponseEntity.ok(homeVendedorService.buscarDetalheVenda(idVendedor, idPedido));
    }
}