package org.example.autobot.web;

import org.example.autobot.service.HhAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.MissingRequestValueException;
import reactor.core.publisher.Mono;

@RestController
public class HhAuthController {
    private final HhAuthService hhAuthService;
    private static final String SUCCESS = "Профиль успешно привязан";
    private static final String FAIL = "Не удалось привязать профиль";

    public HhAuthController(HhAuthService hhAuthService) {
        this.hhAuthService = hhAuthService;
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<String>> callback(@RequestParam(name = "code", required = true) String code,
                                                 @RequestParam(name = "state", required = true) long chatId) {
        return hhAuthService.handleCallback(code, chatId)
                .map(msg -> {
                    if (SUCCESS.equals(msg)) {
                        return ResponseEntity.ok(msg);
                    }
                    if (FAIL.equals(msg)) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Ошибка привязки профиля");
                    }
                    return ResponseEntity.ok(msg);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Ошибка привязки профиля")));
    }

    @ExceptionHandler(MissingRequestValueException.class)
    public Mono<ResponseEntity<String>> handleMissing(MissingRequestValueException ex) {
        return Mono.just(ResponseEntity.badRequest()
                .body("Отсутствует обязательный параметр: " + ex.getName()));
    }
}