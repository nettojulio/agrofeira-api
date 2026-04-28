package br.edu.ufape.agrofeira.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.username}") private val remetente: String,
    @Value("\${app.frontend.url}") private val frontendUrl: String,
) {
    fun sendPasswordResetEmail(
        destinatario: String,
        token: String,
        nomeUsuario: String,
    ) {
        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(remetente, "Agro Feira - LMTS")
            helper.setTo(destinatario)
            helper.setSubject("Recuperação de Senha - Agro Feira")

            val link = "$frontendUrl/reset-password?token=$token"
            val html =
                """
                <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2 style="color: #2e7d32;">Olá, $nomeUsuario!</h2>
                    <p>Recebemos uma solicitação para redefinir a senha da sua conta na <strong>Agro Feira</strong>.</p>
                    <p>Para criar uma nova senha, utilize o token abaixo no aplicativo ou clique no link:</p>
                    <div style="background-color: #f5f5f5; padding: 15px; text-align: center; border-radius: 5px; margin: 20px 0;">
                        <h3 style="letter-spacing: 5px; color: #1b5e20; margin: 0;">$token</h3>
                    </div>
                    <p style="text-align: center;">
                        <a href="$link" style="background-color: #2e7d32; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;">Redefinir Senha</a>
                    </p>
                    <p>Se você não solicitou essa alteração, por favor ignore este e-mail. O token expira em 1 hora.</p>
                    <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 0.8em; color: #777;">Atenciosamente,<br>Equipe LMTS - UFAPE</p>
                </div>
                """.trimIndent()

            helper.setText(html, true)
            mailSender.send(message)
        } catch (e: Exception) {
            // Log do erro real para o desenvolvedor
            println("Erro ao enviar e-mail: ${e.message}")
            throw MailSendException("Entrar em contato com o administrador (lmts@ufape.edu.br)")
        }
    }

    fun sendPasswordChangeNotification(
        destinatario: String,
        nomeUsuario: String,
    ) {
        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(remetente, "Agro Feira - LMTS")
            helper.setTo(destinatario)
            helper.setSubject("Sua senha foi alterada - Agro Feira")

            val html =
                """
                <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2 style="color: #2e7d32;">Olá, $nomeUsuario!</h2>
                    <p>Este é um aviso de segurança para informar que a senha da sua conta na <strong>Agro Feira</strong> foi alterada recentemente.</p>
                    <p>Se você realizou essa alteração, nenhuma ação adicional é necessária.</p>
                    <div style="background-color: #fff3e0; padding: 15px; border-left: 5px solid #ff9800; border-radius: 5px; margin: 20px 0;">
                        <p style="margin: 0; color: #e65100;"><strong>Não foi você?</strong> Se você não alterou sua senha, por favor entre em contato com nossa equipe de suporte imediatamente respondendo a este e-mail ou via <strong>lmts@ufape.edu.br</strong>.</p>
                    </div>
                    <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 0.8em; color: #777;">Atenciosamente,<br>Equipe LMTS - UFAPE</p>
                </div>
                """.trimIndent()

            helper.setText(html, true)
            mailSender.send(message)
        } catch (e: Exception) {
            println("Erro ao enviar notificação de troca de senha: ${e.message}")
            // Não lançamos exceção aqui para não travar o processo principal de troca de senha,
            // já que a troca em si já foi concluída no banco.
        }
    }
}
