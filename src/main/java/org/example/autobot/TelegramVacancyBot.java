package org.example.autobot;

import org.example.autobot.command.CommandHandler;
import org.example.autobot.kafka.KafkaUpdateProducer;
import org.example.autobot.config.TelegramBotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TelegramVacancyBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramVacancyBot.class);

    private final CommandHandler commandHandler;
    private final KafkaUpdateProducer updateProducer;  // может быть null
    private final TelegramBotProperties properties;

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

    /**
     * Вспомогательный метод для отправки текстового сообщения.
     */
    public void sendText(long chatId, String text) {
        SendMessage msg = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        try {
            execute(msg);
        } catch (Exception e) {
            log.error("❌ Failed to send message", e);
        }
    }
}
