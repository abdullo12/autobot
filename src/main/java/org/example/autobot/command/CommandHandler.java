package org.example.autobot.command;

import org.example.autobot.HhFetcher;
import org.example.autobot.TelegramSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    private final TelegramSender sender;
    private final HhFetcher hhFetcher;

    public CommandHandler(TelegramSender sender, HhFetcher hhFetcher) {
        this.sender = sender;
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
                case "/start" -> sender.sendText(chatId, "Привет! Я бот, который ищет вакансии. Используй /vacancies для поиска.");
                case "/vacancies" -> sender.sendText(chatId, hhFetcher.fetchAndFormatVacancies());
                case "/help" -> sender.sendText(chatId, "Доступные команды:\n/start - приветствие\n/vacancies - поиск вакансий\n/help - помощь");
                default -> sender.sendText(chatId, "Команда не распознана");
            }
        } catch (Exception e) {
            log.error("Error processing command", e);
            sender.sendText(chatId, "Произошла ошибка. Повторите позже");
        }
    }
}
