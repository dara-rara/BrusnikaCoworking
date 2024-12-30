package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.KafkaMailMessage;
import com.example.BrusnikaCoworking.listener.MessageMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${server.frontend-url}")
    private String frontEndURL;

    @Async
    public void send(KafkaMailMessage kafkaMailMessage, MessageMode mode) {
        try {
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, true);

            if (mode == MessageMode.EMAIL_VERIFICATION) {
                var url = frontEndURL + "/verification?data=" + kafkaMailMessage.message();
                helper.setText("<div style=\"max-width: 550px; margin: 20px auto; " +
                        "border-radius: 15px; border: 1px solid #c5c5c5;\n" +
                        "background-color: #f9f9f9; padding: 20px; font-family: Arial, sans-serif;\"> " +
                        "<h3 style=\"color: #4A90E2;\">🎉 Подтверждение регистрации 🎉</h3>" +
                        "<p>Здравствуйте!</p>" +
                        "<br>Рады приветствовать вас на нашем сайте. Надеемся, что он облегчит вам жизнь. " +
                        "Пожалуйста, подтвердите актуальность почты, нажав на кнопку ниже:</p> " +
                        "<p style=\"text-align: center; padding: 20px;\">" +
                        "<a href=\"" + url + "\" style=\"padding: 15px 20px; background-color: #4A90E2; " +
                        "color: white; text-decoration: navy; border-radius: 5px;\">ПОДТВЕРДИТЬ</a> " +
                        "</p>" +
                        "<p>С наилучшими пожеланиями,<br>команда разработчиков!</p>" +
                        "</div>",true);
                helper.setSubject("Подтвердите регистрацию");
            } else if(mode == MessageMode.PASSWORD_UPDATE) {
                var url = frontEndURL + "/password?data=" + kafkaMailMessage.message();
                helper.setText("<div style=\"max-width: 570px; \n" +
                        "margin: 20px auto; border-radius: 15px;\n" +
                        "border: 1px solid #c5c5c5; \n" +
                        "background-color: #f9f9f9; \n" +
                        "padding: 20px; font-family: \n" +
                        "Arial, sans-serif;\">\n" +
                        "<h3 style=\"color: #4A90E2;\">&#11088 Обновление пароля на сайте БРУСНИКА.КОВОРКИНГ &#11088</h3>\n" +
                        "<p>Здравствуйте!</p>\n" +
                        "<br>Чтобы сбросить существующий пароль и обновить на новый, пожалуйста, \n" +
                        "подтвердите свои действия, нажав на кнопку ниже:</p> \n" +
                        "<p style=\"text-align: center; padding: 20px;\">\n" +
                        "<a href=\"" + url + "\" style=\"padding: 15px 20px; background-color: #4A90E2;\n" +
                        "color: white; text-decoration: navy; border-radius: 5px;\">ИЗМЕНИТЬ ПАРОЛЬ</a></p>\n" +
                        "<p>С наилучшими пожеланиями,<br>команда разработчиков!</p></div>", true);
                helper.setSubject("Изменение пароля");
            } else {
                helper.setText(kafkaMailMessage.message(), true);
                helper.setSubject("Сообщение от БРУСНИКА.КОВОРКИНГ");
            }

            helper.setTo(kafkaMailMessage.email());
            helper.setFrom("БРУСНИКА.КОВОРКИНГ<brusnikacoworking@gmail.com>"); //?

            mailSender.send(mimeMessage);
            log.info("email send, msg: {}, mode: {}", kafkaMailMessage, mode);
        } catch (Exception e) {
//            log.error("send mail error : {}", e.getMessage());
            log.error("send mail error: {}", e.getMessage(), e); // Логируем полный стектрейс
        }
    }
}
