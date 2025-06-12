package org.example.autobot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import io.github.cdimascio.dotenv.Dotenv;


public class TelegramVacancyBot extends TelegramLongPollingBot {

    Dotenv dotenv = Dotenv.configure().load();

    private final String token = dotenv.get("BOT_TOKEN");
    private final String username = dotenv.get("BOT_USERNAME");


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
