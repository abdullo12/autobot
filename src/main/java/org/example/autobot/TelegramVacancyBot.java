package org.example.autobot;

import org.example.autobot.command.CommandHandler;
import org.example.autobot.kafka.KafkaUpdateProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.autobot.config.TelegramBotProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TelegramVacancyBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramVacancyBot.class);

    private final CommandHandler commandHandler;
    private final KafkaUpdateProducer updateProducer;  // может быть null
    private final TelegramBotProperties properties;

    // Внедряем KafkaUpdateProducer с required=false
    public TelegramVacancyBot(
            CommandHandler commandHandler,
            @Autowired(required = false) KafkaUpdateProducer updateProducer,
            TelegramBotProperties properties
    ) {
        this.commandHandler = commandHandler;
        this.updateProducer = updateProducer;
        this.properties = properties;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("📥 Update received: {}", update);

        // Если KafkaUpdateProducer есть — шлём update в Kafka
        if (updateProducer != null) {
            updateProducer.send(update);
        }

        // Обработка команды
        commandHandler.handle(update);
    }

    @Override
    public String getBotUsername() {
        return properties.getUsername();
    }

    @Override
    public String getBotToken() {
        return properties.getToken();
    }

    // Если используется, можно оставить
    public void sendText(long chatId, String text) {
        try {
            execute(new org.telegram.telegrambots.meta.api.methods.send.SendMessage(
                    String.valueOf(chatId),
                    text
            ));
        } catch (Exception e) {
            log.error("❌ Failed to send message", e);
        }
    }
}

