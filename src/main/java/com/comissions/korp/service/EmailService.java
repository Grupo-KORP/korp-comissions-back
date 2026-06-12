package com.comissions.korp.service;

import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.EmailNotSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private final JavaMailSender enviadorEmail;
    private static final String EMAIL_ORIGEM = "sistemacomissaotnd@gmail.com";
    private static final String NOME_ENVIADOR = "Sistema de Comissões/Vendas - TND";

    public static final String URL_SITE = "http://localhost:5173/vendedores/redefinir-senha";

    public EmailService(JavaMailSender enviadorEmail) {
        this.enviadorEmail = enviadorEmail;
    }

    public void enviarEmail(String emailUsuario, String assunto, String conteudo) {
        MimeMessage message = enviadorEmail.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setFrom(EMAIL_ORIGEM, NOME_ENVIADOR);
            helper.setTo(emailUsuario);
            helper.setSubject(assunto);
            helper.setText(conteudo, true);
        } catch(MessagingException | UnsupportedEncodingException e){
            throw new EmailNotSendException("Erro ao enviar email");
        }

        enviadorEmail.send(message);
    }

    private String gerarConteudoEmail(String template, String nome, String senhaAleatoria) {
        return template.replace("[[name]]", nome).replace("[[password]]", senhaAleatoria);
    }

    private String gerarConteudoTrocaSenha(String template, String nome, String url) {
        return template.replace("[[name]]", nome).replace("[[URL]]", url);
    }

    @Async
    public void enviarEmailSenhaProvisoria(Usuario usuario, String senhaAleatoria) {

        String assunto = "Aqui está sua senha de acesso para o Sistema de Comissões - TND";
        String conteudo = gerarConteudoEmail(
                        "<div style=\"font-family: Arial, sans-serif; background-color: #f4f4f7; padding: 40px 20px; color: #1b1b1b;\">" +

                                "  <div style=\"max-width: 600px; margin: auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);\">" +

                                // HEADER
                                "    <div style=\"background: linear-gradient(90deg, #132B7A, #4D87C7); padding: 30px; text-align: center;\">" +
                                "      <h1 style=\"color: white; margin: 0; font-size: 28px;\">TND Brasil</h1>" +
                                "      <p style=\"color: #dfe9ff; margin-top: 8px; font-size: 14px; letter-spacing: 1px;\">" +
                                "        Sistema de Comissões" +
                                "      </p>" +
                                "    </div>" +

                                // CONTEÚDO
                                "    <div style=\"padding: 40px 35px;\">" +

                                "      <h2 style=\"color: #132B7A; margin-top: 0; font-size: 24px;\">" +
                                "        Olá, [[name]] 👋" +
                                "      </h2>" +

                                "      <p style=\"font-size: 16px; color: #555;\">" +
                                "        Seu acesso ao <strong>Sistema de Comissões da TND</strong> foi criado com sucesso." +
                                "      </p>" +

                                "      <p style=\"font-size: 16px; color: #555;\">" +
                                "        Para realizar seu primeiro acesso, utilize a senha provisória abaixo:" +
                                "      </p>" +

                                // SENHA
                                "      <div style=\"background-color: #eef3ff; border: 2px dashed #2F5BFF; border-radius: 12px; padding: 20px; text-align: center; margin: 30px 0;\">" +
                                "        <span style=\"font-size: 28px; font-weight: bold; color: #132B7A; letter-spacing: 3px;\">" +
                                "          [[password]]" +
                                "        </span>" +
                                "      </div>" +

                                "      <p style=\"font-size: 15px; color: #666;\">" +
                                "        Por segurança, recomendamos que você altere sua senha após acessar o sistema pela primeira vez." +
                                "      </p>" +

                                "      <div style=\"margin-top: 35px; padding: 18px; background-color: #f8f9fc; border-left: 4px solid #2F5BFF; border-radius: 8px;\">" +
                                "        <p style=\"margin: 0; color: #555; font-size: 14px;\">" +
                                "          Caso você tenha recebido este e-mail por engano, apenas ignore esta mensagem." +
                                "        </p>" +
                                "      </div>" +

                                "      <p style=\"margin-top: 40px; font-size: 15px; color: #444;\">" +
                                "        Atenciosamente,<br>" +
                                "        <strong style=\"color: #132B7A;\">Equipe TND Brasil</strong>" +
                                "      </p>" +

                                "    </div>" +

                                // FOOTER
                                "    <div style=\"background-color: #f1f4fa; text-align: center; padding: 18px; font-size: 12px; color: #888;\">" +
                                "      © 2026 TND Brasil • Sistema de Comissões" +
                                "    </div>" +

                                "  </div>" +
                                "</div>",
                usuario.getNome(), senhaAleatoria
        );

        enviarEmail(usuario.getEmail(), assunto, conteudo);
    }

    @Async
    public void enviarEmailTrocaSenha(Usuario usuario) {

        String assunto = "Aqui está seu link para Alterar a senha";
        String conteudo = gerarConteudoTrocaSenha(
                "<div style=\"font-family: Arial, sans-serif; background-color: #f4f4f7; padding: 40px 20px; color: #1b1b1b;\">" +

                        "  <div style=\"max-width: 600px; margin: auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);\">" +

                        // HEADER
                        "    <div style=\"background: linear-gradient(90deg, #132B7A, #4D87C7); padding: 30px; text-align: center;\">" +
                        "      <h1 style=\"color: white; margin: 0; font-size: 28px;\">TND Brasil</h1>" +
                        "      <p style=\"color: #dfe9ff; margin-top: 8px; font-size: 14px; letter-spacing: 1px;\">" +
                        "        Sistema de Comissões" +
                        "      </p>" +
                        "    </div>" +

                        // CONTEÚDO
                        "    <div style=\"padding: 40px 35px;\">" +

                        "      <h2 style=\"color: #132B7A; margin-top: 0; font-size: 24px;\">" +
                        "        Olá, [[name]] 👋" +
                        "      </h2>" +

                        "      <p style=\"font-size: 16px; color: #555;\">" +
                        "        Recebemos uma solicitação para redefinir a senha da sua conta no <strong>Sistema de Comissões da TND</strong>." +
                        "      </p>" +

                        "      <p style=\"font-size: 16px; color: #555;\">" +
                        "        Clique no botão abaixo para criar uma nova senha:" +
                        "      </p>" +

                        // BOTÃO
                        "      <div style=\"text-align: center; margin: 35px 0;\">" +
                        "        <a href=\"[[URL]]\"" +
                        "           style=\"background: linear-gradient(90deg, #132B7A, #4D87C7); color: white; padding: 14px 32px;" +
                        "                  text-decoration: none; font-size: 16px; font-weight: bold; border-radius: 10px;" +
                        "                  display: inline-block; letter-spacing: 0.5px;\">" +
                        "          Redefinir Senha" +
                        "        </a>" +
                        "      </div>" +

                        // AVISO
                        "      <div style=\"margin-top: 10px; padding: 18px; background-color: #f8f9fc; border-left: 4px solid #2F5BFF; border-radius: 8px;\">" +
                        "        <p style=\"margin: 0; color: #555; font-size: 14px;\">" +
                        "          Se você não solicitou a troca de senha, apenas ignore este e-mail. O link expira automaticamente após algum tempo." +
                        "        </p>" +
                        "      </div>" +

                        "      <p style=\"margin-top: 40px; font-size: 15px; color: #444;\">" +
                        "        Atenciosamente,<br>" +
                        "        <strong style=\"color: #132B7A;\">Equipe TND Brasil</strong>" +
                        "      </p>" +

                        "    </div>" +

                        // FOOTER
                        "    <div style=\"background-color: #f1f4fa; text-align: center; padding: 18px; font-size: 12px; color: #888;\">" +
                        "      © 2026 TND Brasil • Sistema de Comissões" +
                        "    </div>" +

                        "  </div>" +
                        "</div>",
                usuario.getNome(),
                URL_SITE + "?token=" + usuario.getToken()
        );

        enviarEmail(usuario.getEmail(), assunto, conteudo);
    }

    @Async
    public void enviarPedidoDistribuidor(String emailDistribuidor, String nomeDistribuidor,
                                         String codigoPedido, String nomeCliente,
                                         byte[] pdfBytes) {

        String assunto = "Novo Pedido Registrado - Código: " + codigoPedido;

        String conteudo =
                "<div style=\"font-family: Arial, sans-serif; background-color: #f4f4f7; padding: 40px 20px; color: #1b1b1b;\">" +
                        "  <div style=\"max-width: 600px; margin: auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);\">" +

                        // HEADER
                        "    <div style=\"background: linear-gradient(90deg, #132B7A, #4D87C7); padding: 30px; text-align: center;\">" +
                        "      <h1 style=\"color: white; margin: 0; font-size: 28px;\">TND Brasil</h1>" +
                        "      <p style=\"color: #dfe9ff; margin-top: 8px; font-size: 14px; letter-spacing: 1px;\">Sistema de Comissões</p>" +
                        "    </div>" +

                        // CONTEÚDO
                        "    <div style=\"padding: 40px 35px;\">" +
                        "      <h2 style=\"color: #132B7A; margin-top: 0; font-size: 24px;\">Olá, " + nomeDistribuidor + " 👋</h2>" +

                        "      <p style=\"font-size: 16px; color: #555;\">" +
                        "        Um novo pedido foi registrado no <strong>Sistema de Comissões da TND</strong> e está associado à sua distribuidora." +
                        "      </p>" +

                        // CARD DO PEDIDO
                        "      <div style=\"background-color: #eef3ff; border: 2px dashed #2F5BFF; border-radius: 12px; padding: 20px; margin: 30px 0;\">" +
                        "        <p style=\"margin: 0 0 8px; font-size: 13px; color: #666; text-transform: uppercase; letter-spacing: 1px;\">Código do Pedido</p>" +
                        "        <span style=\"font-size: 26px; font-weight: bold; color: #132B7A; letter-spacing: 3px;\">" + codigoPedido + "</span>" +
                        "        <hr style=\"border: none; border-top: 1px solid #c8d6f5; margin: 16px 0;\">" +
                        "        <p style=\"margin: 0; font-size: 15px; color: #444;\">Cliente: <strong>" + nomeCliente + "</strong></p>" +
                        "      </div>" +

                        "      <p style=\"font-size: 15px; color: #555;\">" +
                        "        O PDF completo do pedido está anexado a este e-mail para sua referência." +
                        "      </p>" +

                        // AVISO
                        "      <div style=\"margin-top: 20px; padding: 18px; background-color: #f8f9fc; border-left: 4px solid #2F5BFF; border-radius: 8px;\">" +
                        "        <p style=\"margin: 0; color: #555; font-size: 14px;\">" +
                        "          Caso tenha dúvidas sobre este pedido, entre em contato com a equipe responsável." +
                        "        </p>" +
                        "      </div>" +

                        "      <p style=\"margin-top: 40px; font-size: 15px; color: #444;\">" +
                        "        Atenciosamente,<br>" +
                        "        <strong style=\"color: #132B7A;\">Equipe TND Brasil</strong>" +
                        "      </p>" +
                        "    </div>" +

                        // FOOTER
                        "    <div style=\"background-color: #f1f4fa; text-align: center; padding: 18px; font-size: 12px; color: #888;\">" +
                        "      © 2026 TND Brasil • Sistema de Comissões" +
                        "    </div>" +
                        "  </div>" +
                        "</div>";

        try {
            MimeMessage message = enviadorEmail.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // true = multipart

            helper.setFrom(EMAIL_ORIGEM, NOME_ENVIADOR);
            helper.setTo(emailDistribuidor);
            helper.setSubject(assunto);
            helper.setText(conteudo, true);
            helper.addAttachment("pedido-" + codigoPedido + ".pdf",
                    new ByteArrayResource(pdfBytes),
                    "application/pdf");

            enviadorEmail.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new EmailNotSendException("Erro ao enviar e-mail ao distribuidor: " + e.getMessage());
        }
    }
}

