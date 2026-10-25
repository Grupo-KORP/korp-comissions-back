package com.comissions.korp.DTO.HomeFinanceiroDTO;

import com.comissions.korp.entity.ENUM.StatusComissao;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusComissaoRequestDTO(
        @NotNull StatusComissao status
) {
}