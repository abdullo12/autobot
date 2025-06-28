package org.example.autobot.service;

import org.example.autobot.model.HhProfile;
import org.example.autobot.repository.HhProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class HhAuthService {

    @Value("${hh.oauth.client-id}")
    private String clientId;

    @Value("${hh.oauth.client-secret}")
    private String clientSecret;

    @Value("${hh.oauth.redirect-uri}")
    private String redirectUri;

    private final HhProfileRepository repo;
    private final WebClient client;

    public HhAuthService(
            HhProfileRepository repo,
            WebClient.Builder builder,
            @Value("${hh.base-url:https://hh.ru}") String baseUrl
    ) {
        this.repo = repo;
        this.client = builder.baseUrl(baseUrl).build();
    }

    /**
     * Генерирует ссылку на авторизацию для конкретного пользователя.
     */
    public String buildAuthUrl(long chatId) {
        return String.format(
                "https://hh.ru/oauth/authorize?response_type=code&client_id=%s&state=%d&redirect_uri=%s",
                clientId,
                chatId,
                redirectUri
        );
    }

    /**
     * Обменивает код авторизации на токены и сохраняет их в репозитории.
     */
    public Mono<Void> handleCallback(String code, Long chatId) {
        return client.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                        BodyInserters.fromFormData("grant_type", "authorization_code")
                                .with("client_id", clientId)
                                .with("client_secret", clientSecret)
                                .with("code", code)
                                .with("redirect_uri", redirectUri)
                )
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(resp -> repo.save(new HhProfile(chatId, resp.accessToken(), resp.refreshToken())))
                .then();
    }

    /**
     * Ответ от hh.ru при получении access и refresh токенов.
     */
    private record TokenResponse(
            @Value("${access_token}") String accessToken,
            @Value("${refresh_token}") String refreshToken
    ) {}
}

