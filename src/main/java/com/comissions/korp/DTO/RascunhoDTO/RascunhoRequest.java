package com.comissions.korp.DTO.RascunhoDTO;

import tools.jackson.databind.JsonNode;

public class RascunhoRequest {

    private JsonNode dados;
    private Integer versao;

    public JsonNode getDados() {
        return dados;
    }

    public void setDados(JsonNode dados) {
        this.dados = dados;
    }

    /** Versão que o front recebeu ao abrir o rascunho; obrigatória na atualização. */
    public Integer getVersao() {
        return versao;
    }

    public void setVersao(Integer versao) {
        this.versao = versao;
    }
}
