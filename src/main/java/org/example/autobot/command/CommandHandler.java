package org.example.autobot.command;

import org.example.autobot.HhFetcher;
import org.example.autobot.TelegramVacancyBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    private final TelegramVacancyBot bot;
    private final HhFetcher hhFetcher;

    @Autowired
    public CommandHandler(TelegramVacancyBot bot, HhFetcher hhFetcher) {
        this.bot = bot;
        this.hhFetcher = hhFetcher;
    }

    @Async
    public void handle(Update update) {
        log.info("Incoming update: {}", update);
        if (update == null || !update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        String command = text.split("\\s+")[0];
        try {
            switch (command) {
                case "/start" -> handleStart(chatId);
                case "/vacancies" -> handleVacancies(chatId);
                case "/help" -> handleHelp(chatId);
                default -> bot.sendText(chatId, "Команда не распознана");
            }
        } catch (Exception e) {
            log.error("Error processing command", e);
            bot.sendText(chatId, "Произошла ошибка. Повторите позже");
        }
    }

    private void handleStart(long chatId) {
        String msg = "Привет! Я бот, который ищет вакансии. Используй /vacancies для поиска.";
        bot.sendText(chatId, msg);
    }

    private void handleVacancies(long chatId) {
        String response = hhFetcher.fetchAndFormatVacancies();
        bot.sendText(chatId, response);
    }

    private void handleHelp(long chatId) {
        String msg = "Доступные команды:\n/start - приветствие\n/vacancies - поиск вакансий\n/help - помощь";
        bot.sendText(chatId, msg);
    }
}
