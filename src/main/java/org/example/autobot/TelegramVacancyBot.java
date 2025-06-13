package org.example.autobot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
public class TelegramVacancyBot extends TelegramWebhookBot {

    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.bot.username}")
    private String username;
  

    private static final Logger log = LoggerFactory.getLogger(TelegramVacancyBot.class);


    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        log.debug("Received update in TelegramVacancyBot: {}", update);
        return null;
    }

    @Override
    public String getBotPath() {
        return "/webhook";
    }

    public void sendText(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText(text);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
