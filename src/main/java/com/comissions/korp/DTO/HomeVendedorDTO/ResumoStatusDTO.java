package com.comissions.korp.DTO.HomeVendedorDTO;

import com.comissions.korp.entity.ENUM.StatusComissao;

import java.math.BigDecimal;

/**
 * Linha do agregado "quantidade e soma de comissões por status" (usado só no resumo do painel).
 * Instanciado direto pela query JPQL (constructor expression) em ComissaoRepository.
 */
public record ResumoStatusDTO(StatusComissao status, Long quantidade, BigDecimal total) {
}