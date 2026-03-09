package com.comissions.korp.service;

import com.comissions.korp.entity.Distribuidor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DistribuidorService {

    private List<Distribuidor> distribuidores = new ArrayList<>(List.of(
            new Distribuidor(
                    12345678, "12.345.678/0001-00", "Sala 10", "Centro",
                    "contato@alpha.com", "Rua das Flores", "São Paulo",
                    "Empresa Alpha Ltda", "Alpha Tech", 100,
                    "1133334444", "11988887777", "SP"
            ),
            new Distribuidor(
                    87654321, "98.765.432/0001-99", null, "Industrial",
                    "vendas@beta.com", "Avenida Central", "Curitiba",
                    "Beta Suprimentos S.A.", "Beta Distribuição", 500,
                    "4133330000", "41999991111", "PR"
            )
    ));

    public List<Distribuidor> listarTodosDistribuidores(){
        return (distribuidores != null) ? distribuidores : Collections.emptyList();
    }

    public Optional<Distribuidor> buscarDistribuidorPorCnpj(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException("CNPJ não pode ser vazio!");
        }

        String cnpjBuscaLimpo = limparCnpj(cnpj);

        // Busca na lista comparando os CNPJs limpos
        return distribuidores.stream()
                .filter(d -> limparCnpj(d.getCnpj()).equals(cnpjBuscaLimpo))
                .findFirst();
    }

    public Distribuidor cadastrarDistribuidor(Distribuidor distribuidor) {
        validarCamposObrigatorios(distribuidor);
        distribuidores.add(distribuidor);
        return distribuidor;
    }

    private void validarCamposObrigatorios(Distribuidor distribuidor) {
        validarString(distribuidor.getCnpj(), "CNPJ");
        validarString(distribuidor.getComplemento(), "Complemento");
        validarString(distribuidor.getDistrito(), "Distrito");
        validarString(distribuidor.getEmail(), "E-mail");
        validarString(distribuidor.getLogradouro(), "Logradouro");
        validarString(distribuidor.getMunicipio(), "Município");
        validarString(distribuidor.getNomeEmpresa(), "Nome da Empresa");
        validarString(distribuidor.getNomeFantasia(), "Nome Fantasia");
        validarString(distribuidor.getTelComercial(), "Telefone Comercial");
        validarString(distribuidor.getTelRepresentante(), "Telefone do Representante");
        validarString(distribuidor.getUf(), "UF");

        validarNumero(distribuidor.getCep(), "CEP");
        validarNumero(distribuidor.getNumero(), "Número");
    }

    private void validarString(String valor, String nomeCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo '" + nomeCampo + "' é obrigatório e não pode estar vazio.");
        }
    }

    private void validarNumero(Integer valor, String nomeCampo) {
        if (valor == null) {
            throw new IllegalArgumentException("O campo '" + nomeCampo + "' é obrigatório.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O campo '" + nomeCampo + "' deve ser um número positivo.");
        }
    }

    private String limparCnpj(String cnpj) {
        if (cnpj == null) return "";
        return cnpj.replaceAll("\\D", "");
    }

    public Optional<Distribuidor> atualizarDistribuidor(String cnpj, Distribuidor dadosNovos) {
        String cnpjBuscaLimpo = limparCnpj(cnpj);

        Optional<Distribuidor> distribuidorExistente = distribuidores.stream()
                .filter(d -> limparCnpj(d.getCnpj()).equals(cnpjBuscaLimpo))
                .findFirst();

        if (distribuidorExistente.isPresent()) {
            Distribuidor d = distribuidorExistente.get();

            if (dadosNovos.getCep() != null) d.setCep(dadosNovos.getCep());

            if (dadosNovos.getCnpj() != null) d.setCnpj(dadosNovos.getCnpj());

            if (dadosNovos.getComplemento() != null) d.setComplemento(dadosNovos.getComplemento());
            if (dadosNovos.getDistrito() != null) d.setDistrito(dadosNovos.getDistrito());
            if (dadosNovos.getEmail() != null) d.setEmail(dadosNovos.getEmail());
            if (dadosNovos.getLogradouro() != null) d.setLogradouro(dadosNovos.getLogradouro());
            if (dadosNovos.getMunicipio() != null) d.setMunicipio(dadosNovos.getMunicipio());
            if (dadosNovos.getNomeEmpresa() != null) d.setNomeEmpresa(dadosNovos.getNomeEmpresa());
            if (dadosNovos.getNomeFantasia() != null) d.setNomeFantasia(dadosNovos.getNomeFantasia());
            if (dadosNovos.getNumero() != null) d.setNumero(dadosNovos.getNumero());
            if (dadosNovos.getTelComercial() != null) d.setTelComercial(dadosNovos.getTelComercial());
            if (dadosNovos.getTelRepresentante() != null) d.setTelRepresentante(dadosNovos.getTelRepresentante());
            if (dadosNovos.getUf() != null) d.setUf(dadosNovos.getUf());

            return Optional.of(d);
        }

        return Optional.empty();
    }

    public boolean deletarPorCnpj(String cnpj) {
        if (cnpj == null || distribuidores == null) {
            return false;
        }

        String cnpjLimpo = limparCnpj(cnpj);
        return distribuidores.removeIf(d ->
                d != null && d.getCnpj() != null && limparCnpj(d.getCnpj()).equals(cnpjLimpo));
    }

}
