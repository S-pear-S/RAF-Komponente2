package com.gaming.notificationservice.consumer;

import com.gaming.notificationservice.dto.NotificationMessage;
import com.gaming.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "notification_queue")
    public void handleNotification(NotificationMessage message) {
        System.out.println("RECEIVED MESSAGE: " + message);
        emailService.sendEmail(
                message.getRecipientEmail(),
                message.getType(),
                message.getContent()
        );
    }
}