package org.example.autobot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramVacancyBot extends TelegramLongPollingBot {

    private final String token = System.getenv("BOT_TOKEN");
    private final String username = System.getenv("BOT_USERNAME");

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String command = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (command.equals("/start") || command.equals("/vacancies")) {
                String response = HhFetcher.fetchAndFormatVacancies();
                sendMsg(chatId, response);
            }
        }
    }

    private void sendMsg(long chatId, String text) {
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
