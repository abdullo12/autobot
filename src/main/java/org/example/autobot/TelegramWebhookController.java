package org.example.autobot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.WebhookBot;

@RestController
@RequestMapping("/telegram")
public class TelegramWebhookController {

    private final WebhookBot bot;

    @Value("${telegram.bot.token}")
    private String botToken;

    public TelegramWebhookController(WebhookBot bot) {
        this.bot = bot;
    }

    @PostMapping("/webhook/{token}")
    public void onUpdateReceived(@RequestBody Update update, @PathVariable String token) {
        if (!token.equals(botToken)) {
            throw new SecurityException("Invalid token");
        }
        bot.onWebhookUpdateReceived(update);
    }
}
