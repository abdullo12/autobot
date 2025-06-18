package org.example.autobot;

import org.example.autobot.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TelegramVacancyBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramVacancyBot.class);

    private final CommandHandler commandHandler;

    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.bot.username}")
    private String username;

    public TelegramVacancyBot(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("📥 Update received: {}", update);
        commandHandler.handle(update);
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    // sendText(chatId, text) — если ты его использовал, оставь метод
    public void sendText(long chatId, String text) {
        try {
            execute(new org.telegram.telegrambots.meta.api.methods.send.SendMessage(String.valueOf(chatId), text));
        } catch (Exception e) {
            log.error("❌ Failed to send message", e);
        }
    }
}
