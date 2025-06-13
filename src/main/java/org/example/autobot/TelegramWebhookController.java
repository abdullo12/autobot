package org.example.autobot;

import org.example.autobot.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class TelegramWebhookController {

    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookController.class);
    private final CommandHandler commandHandler;

    public TelegramWebhookController(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @PostMapping("/webhook")
    public void onUpdateReceived(@RequestBody Update update) {
        log.info("Update received: {}", update);
        commandHandler.handle(update);
    }
}
