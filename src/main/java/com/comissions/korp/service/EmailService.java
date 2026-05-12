package com.comissions.korp.service;

import com.comissions.korp.entity.Usuario;
import com.comissions.korp.exception.EmailNotSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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

    public EmailService(JavaMailSender enviadorEmail) {
        this.enviadorEmail = enviadorEmail;
    }
    @Async
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
}

