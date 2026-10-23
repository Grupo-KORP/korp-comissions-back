package com.comissions.korp.DTO;

import com.comissions.korp.exception.RequisicaoInvalida;

public class DadosEmpresaRequestDTO {

    private String nomeFantasia;
    private String telefone;

    public DadosEmpresaRequestDTO() {
    }

    public DadosEmpresaRequestDTO(String nomeFantasia, String telefone) {
        this.nomeFantasia = nomeFantasia;
        this.telefone = telefone;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    /** Valida os campos e devolve nome fantasia sem espaços extras; o telefone vai só com dígitos. */
    public DadosEmpresaRequestDTO validarENormalizar() {
        if (nomeFantasia == null || nomeFantasia.isBlank()) {
            throw new RequisicaoInvalida("Nome fantasia obrigatório.");
        }
        String digitos = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (digitos.length() < 10 || digitos.length() > 11) {
            throw new RequisicaoInvalida("Telefone inválido: informe DDD e 10 ou 11 dígitos.");
        }
        return new DadosEmpresaRequestDTO(nomeFantasia.trim(), digitos);
    }
}
