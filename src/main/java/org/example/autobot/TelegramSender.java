package org.example.autobot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramSender {

    private static final Logger log = LoggerFactory.getLogger(TelegramSender.class);

    private final TelegramVacancyBot bot;

    public TelegramSender(TelegramVacancyBot bot) {
        this.bot = bot;
    }

    public void sendText(long chatId, String text) {
        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        try {
            bot.execute(msg);
        } catch (TelegramApiException e) {
            log.error("❌ Failed to send message", e);
        }
    }
}
