package org.example.autobot.controller;

import org.example.autobot.service.HhAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HhAuthController {
    private final HhAuthService hhAuthService;

    public HhAuthController(HhAuthService hhAuthService) {
        this.hhAuthService = hhAuthService;
    }

    @GetMapping("/callback")
    public Mono<String> callback(
            @RequestParam String code,
            @RequestParam("state") Long chatId
    ) {
        return hhAuthService.handleCallback(code, chatId)
                .thenReturn("Профиль успешно привязан");
    }
}