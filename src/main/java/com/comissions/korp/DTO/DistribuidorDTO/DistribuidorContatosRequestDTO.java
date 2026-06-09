package com.comissions.korp.DTO.DistribuidorDTO;

import java.util.List;

public class DistribuidorContatosRequestDTO {

    private List<ContatoItemDTO> contatos;

    public static class ContatoItemDTO {
        private Integer idContato;
        private String nome;
        private String email;
        private String telefone;

        public Integer getIdContato() {
            return idContato;
        }
        public String getNome()     { return nome; }
        public String getEmail()    { return email; }
        public String getTelefone() { return telefone; }

        public void setIdContato(Integer idContato) {
            this.idContato = idContato;
        }
        public void setNome(String nome)         { this.nome = nome; }
        public void setEmail(String email)       { this.email = email; }
        public void setTelefone(String telefone) { this.telefone = telefone; }
    }

    public List<ContatoItemDTO> getContatos() { return contatos; }
    public void setContatos(List<ContatoItemDTO> contatos) { this.contatos = contatos; }
}
