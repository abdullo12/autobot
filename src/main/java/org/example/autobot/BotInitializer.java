package org.example.autobot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotInitializer {

    private static final Logger log = LoggerFactory.getLogger(BotInitializer.class);

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramVacancyBot bot) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            log.info("✅ Telegram bot registered successfully");
            return botsApi;
        } catch (TelegramApiException e) {
            log.error("❌ Failed to register Telegram bot", e);
            throw new RuntimeException("Failed to initialize bot", e);
        }
    }
}
