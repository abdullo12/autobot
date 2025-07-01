package org.example.autobot.service;

import org.example.autobot.web.HhAuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@WebFluxTest(HhAuthController.class)
class HhAuthControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private HhAuthService hhAuthService;

    @Test
    void callback_success() {
        given(hhAuthService.handleCallback(eq("abc"), eq(123L)))
                .willReturn(Mono.just("Профиль успешно привязан"));

        webClient.get().uri("/callback?code=abc&state=123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Профиль успешно привязан");
    }

    @Test
    void callback_error() {
        given(hhAuthService.handleCallback(eq("err"), eq(123L)))
                .willReturn(Mono.just("Не удалось привязать профиль"));

        webClient.get().uri("/callback?code=err&state=123")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Ошибка привязки профиля");
    }

    @Test
    void missing_code_returnsBadRequest() {
        webClient.get().uri("/callback?state=123")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Отсутствует обязательный параметр: code");
    }

    @Test
    void missing_state_returnsBadRequest() {
        webClient.get().uri("/callback?code=abc")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Отсутствует обязательный параметр: state");
    }
}