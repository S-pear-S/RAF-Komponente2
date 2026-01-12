package com.gaming.userservice.service;

import com.gaming.userservice.config.RabbitMQConfig;
import com.gaming.userservice.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationProducer.class);

    public void sendNotification(NotificationMessage message) {
        LOGGER.info(String.format("Sending notification -> %s", message.toString()));
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
    }
}