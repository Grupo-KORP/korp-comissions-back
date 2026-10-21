package com.comissions.korp.DTO.RascunhoDTO;

import tools.jackson.databind.JsonNode;

public class RascunhoResponse extends RascunhoResumoResponse {

    private JsonNode dados;
    private Integer versao;
    private Integer versaoSchema;

    public JsonNode getDados() {
        return dados;
    }

    public void setDados(JsonNode dados) {
        this.dados = dados;
    }

    public Integer getVersao() {
        return versao;
    }

    public void setVersao(Integer versao) {
        this.versao = versao;
    }

    public Integer getVersaoSchema() {
        return versaoSchema;
    }

    public void setVersaoSchema(Integer versaoSchema) {
        this.versaoSchema = versaoSchema;
    }
}
