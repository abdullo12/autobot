package org.example.autobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.bots.TelegramWebhookBot;

@SpringBootApplication
public class AutobotApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutobotApplication.class, args);
    }

    @Bean
    public TelegramWebhookBot telegramBot(TelegramVacancyBot bot) {
        return bot;
    }
}
