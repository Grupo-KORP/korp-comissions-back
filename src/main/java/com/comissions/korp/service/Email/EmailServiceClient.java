package com.comissions.korp.service.Email;


import com.comissions.korp.service.Email.dto.EmailAceitoResponse;
import com.comissions.korp.service.Email.dto.EnviarPedidoDistribuidorRequest;
import com.comissions.korp.service.Email.dto.EnviarSenhaProvisoriaRequest;
import com.comissions.korp.service.Email.dto.EnviarTrocaSenhaRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "korp-email-service", url = "${korp.email-service.url}")
public interface EmailServiceClient {

    @PostMapping("/emails/senha-provisoria")
    EmailAceitoResponse enviarSenhaProvisoria(@RequestBody EnviarSenhaProvisoriaRequest request);

    @PostMapping("/emails/troca-senha")
    EmailAceitoResponse enviarTrocaSenha(@RequestBody EnviarTrocaSenhaRequest request);

    @PostMapping("/emails/pedido-distribuidor")
    EmailAceitoResponse enviarPedidoDistribuidor(@RequestBody EnviarPedidoDistribuidorRequest request);
}
