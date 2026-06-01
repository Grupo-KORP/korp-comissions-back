package com.comissions.korp.DTO.DistribuidorDTO;

import com.comissions.korp.DTO.ContatoDTO.ContatoClienteResponseDTO;

import java.util.List;

public class DistribuidorPedidoResponseDTO {

    private Integer id;
    private String nomeFantasia;
    private String razaoSocial;
    private String cnpj;
    private String inscEst;
    private String fone;
    private String email;
    private String cep;
    private String endereco;
    private String cidade;
    private String uf;
    private List<ContatoClienteResponseDTO> contatos;

    public DistribuidorPedidoResponseDTO() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getInscEst() { return inscEst; }
    public void setInscEst(String inscEst) { this.inscEst = inscEst; }

    public String getFone() { return fone; }
    public void setFone(String fone) { this.fone = fone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public List<ContatoClienteResponseDTO> getContatos() { return contatos; }
    public void setContatos(List<ContatoClienteResponseDTO> contatos) { this.contatos = contatos; }
}

