package org.example.autobot.command;

import org.example.autobot.service.HhAuthService;
import org.example.autobot.service.HhFetcher;
import org.example.autobot.service.HhResumeService;
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
    private final HhAuthService hhAuthService;
    private final HhResumeService hhResumeService;
    private final HhFetcher hhFetcher;

    public CommandHandler(
            TelegramSender sender,
            HhAuthService hhAuthService,
            HhResumeService hhResumeService,
            HhFetcher hhFetcher
    ) {
        this.sender          = sender;
        this.hhAuthService   = hhAuthService;
        this.hhResumeService = hhResumeService;
        this.hhFetcher       = hhFetcher;
    }

    /**
     * Обрабатывает входящее обновление (update) из Telegram.
     * Запускается в отдельном потоке (@Async), чтобы не блокировать polling.
     */
    @Async
    public void handle(Update update) {
        if (update == null
                || !update.hasMessage()
                || !update.getMessage().hasText()
        ) {
            return; // ничего нет — выходим
        }

        String text   = update.getMessage().getText().trim();
        long   chatId = update.getMessage().getChatId();

        // команда — это первое слово (до пробела)
        String command = text.split("\\s+")[0].toLowerCase();

        try {
            switch (command) {
                case "/start":
                    sender.sendText(
                            chatId,
                            "👋 Привет! Я бот для автоматизации откликов на hh.ru.\n" +
                                    "• /vacancies — поиск свежих вакансий\n" +
                                    "• /linkhh — привязать ваш профиль hh.ru\n" +
                                    "• /getresume — получить ваше резюме из hh.ru\n" +
                                    "• /help — показать список команд"
                    );
                    break;

                case "/help":
                    sender.sendText(
                            chatId,
                            "Доступные команды:\n" +
                                    "/start     — приветствие и список команд\n" +
                                    "/vacancies — поиск вакансий по вашим ключам\n" +
                                    "/linkhh    — привязать профиль hh.ru (OAuth)\n" +
                                    "/getresume — получить JSON вашего резюме\n" +
                                    "/help      — эта справка"
                    );
                    break;

                case "/vacancies":
                    // hhFetcher.fetchAndFormatVacancies() возвращает String
                    String vacancies = hhFetcher.fetchAndFormatVacancies();
                    sender.sendText(chatId, vacancies);
                    break;

                case "/linkhh":
                    // генерируем URL OAuth и отправляем пользователю
                    String url = hhAuthService.buildAuthUrl(chatId);
                    sender.sendText(
                            chatId,
                            "🔗 Перейдите по ссылке и авторизуйтесь на hh.ru:\n" + url
                    );
                    break;

                case "/getresume":
                    // пытаемся достать резюме, блокируемся внутри @Async чтобы не мешать основному потоку
                    try {
                        String resumeJson = hhResumeService
                                .fetchMyResume(chatId)
                                .block(); // блок до завершения Mono
                        sender.sendText(chatId, "📄 Ваше резюме:\n" + resumeJson);
                    } catch (Exception e) {
                        log.warn("Ошибка получения резюме для chatId={}", chatId, e);
                        sender.sendText(
                                chatId,
                                "❌ Не могу получить резюме. Возможно, вы ещё не привязали профиль: /linkhh"
                        );
                    }
                    break;

                default:
                    sender.sendText(chatId, "❓ Команда не распознана. Введите /help для списка команд.");
                    break;
            }

        } catch (Exception ex) {
            log.error("Ошибка обработки команды {} от chatId={}", command, chatId, ex);
            sender.sendText(chatId, "⚠️ Произошла внутренняя ошибка. Попробуйте позже.");
        }
    }
}
