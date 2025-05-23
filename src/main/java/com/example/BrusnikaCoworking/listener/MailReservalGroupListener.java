package com.example.BrusnikaCoworking.listener;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.KafkaMailMessage;
import com.example.BrusnikaCoworking.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MailReservalGroupListener {
    private final MailService mailService;

    @KafkaListener(
            topics = "email_message_reserval_group", groupId = "some"
    )
    void listen(
            KafkaMailMessage kafkaMailMessage
    ) {
        log.info("email message: {} ", kafkaMailMessage);
        mailService.send(kafkaMailMessage, MessageMode.RESERVAL_CROUP_CONFIRMATION);
    }

}
