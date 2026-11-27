package com.comissions.korp.service.Email;

import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.EmailNotSendException;
import com.comissions.korp.service.Email.dto.EnviarPedidoDistribuidorRequest;
import com.comissions.korp.service.Email.dto.EnviarSenhaProvisoriaRequest;
import com.comissions.korp.service.Email.dto.EnviarTrocaSenhaRequest;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class EmailService {

    private static final String CAMINHO_REDEFINIR_SENHA = "/vendedores/redefinir-senha";

    private final EmailServiceClient emailServiceClient;
    private final String frontendBaseUrl;

    public EmailService(
            EmailServiceClient emailServiceClient,
            @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl) {
        this.emailServiceClient = emailServiceClient;
        this.frontendBaseUrl = frontendBaseUrl.replaceAll("/+$", "");
    }

    @Async
    public void enviarEmailSenhaProvisoria(Usuario usuario, String senhaAleatoria) {
        try {
            emailServiceClient.enviarSenhaProvisoria(
                    new EnviarSenhaProvisoriaRequest(usuario.getNome(), usuario.getEmail(), senhaAleatoria));
        } catch (FeignException e) {
            throw new EmailNotSendException("Erro ao solicitar envio de senha provisória: " + e.getMessage());
        }
    }

    @Async
    public void enviarEmailTrocaSenha(Usuario usuario) {
        try {
            String url = frontendBaseUrl + CAMINHO_REDEFINIR_SENHA + "?token=" + usuario.getToken();
            emailServiceClient.enviarTrocaSenha(
                    new EnviarTrocaSenhaRequest(usuario.getNome(), usuario.getEmail(), url));
        } catch (FeignException e) {
            throw new EmailNotSendException("Erro ao solicitar troca de senha: " + e.getMessage());
        }
    }

    @Async
    public void enviarPedidoDistribuidor(String emailDistribuidor, String nomeDistribuidor,
                                         String codigoPedido, String nomeCliente,
                                         byte[] pdfBytes) {
        try {
            String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

            emailServiceClient.enviarPedidoDistribuidor(new EnviarPedidoDistribuidorRequest(
                    emailDistribuidor, nomeDistribuidor, codigoPedido, nomeCliente, pdfBase64));
        } catch (FeignException e) {
            throw new EmailNotSendException("Erro ao solicitar envio de e-mail ao distribuidor: " + e.getMessage());
        }
    }
}

