package org.example.autobot.kafka;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class KafkaUpdateProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaUpdateProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Gson gson = new Gson();

    @Value("${telegram.update.topic:telegram-updates}")
    private String topic;

    public KafkaUpdateProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(Update update) {
        try {
            String json = gson.toJson(update);
            kafkaTemplate.send(topic, json);
            log.debug("Sent update to Kafka topic {}", topic);
        } catch (Exception e) {
            log.error("Failed to send update to Kafka", e);
        }
    }
}
