package org.example.autobot.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaCommandConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaCommandConsumer.class);

    @Value("${telegram.commands.topic:telegram-commands}")
    private String topic;

    @KafkaListener(topics = "#{'${telegram.commands.topic:telegram-commands}'}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        log.info("Received command from topic {}: {}", topic, message);
        // Здесь можно добавить обработку входящих команд
    }
}
