package com.comissions.korp.controller;

import com.comissions.korp.DTO.HomeVendedorDTO.HomeVendedorResponseDTO;
import com.comissions.korp.config.utils.SecurityUtils;
import com.comissions.korp.service.HomeVendedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vendedor/home")
public class HomeVendedorController {

    private final HomeVendedorService homeVendedorService;
    private final SecurityUtils securityUtils;

    public HomeVendedorController(HomeVendedorService homeVendedorService, SecurityUtils securityUtils) {
        this.homeVendedorService = homeVendedorService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    @Operation(
            summary = "Buscar painel do vendedor",
            description = "Retorna os dados mensais do painel do vendedor autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Painel retornado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<HomeVendedorResponseDTO> buscarPainel(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes
    ) {
        Integer idVendedor = securityUtils.getUsuarioIdAutenticado();
        return ResponseEntity.ok(homeVendedorService.buscarPainel(idVendedor, ano, mes));
    }
}
