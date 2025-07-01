package org.example.autobot.service;

import org.example.autobot.model.HhProfile;
import org.example.autobot.repository.HhProfileRepository;
import org.example.autobot.config.HhOAuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.annotation.JsonProperty;

@Service
public class HhAuthService {
    private static final Logger log = LoggerFactory.getLogger(HhAuthService.class);

    private final HhOAuthProperties props;
    private final HhProfileRepository repo;
    private final WebClient client;
    private final String baseUrl;

    public HhAuthService(
            HhProfileRepository repo,
            WebClient.Builder builder,
            HhOAuthProperties props,
            @Value("${hh.base-url:https://hh.ru}") String baseUrl
    ) {
        this.repo = repo;
        this.props = props;
        this.client = builder.baseUrl(baseUrl).build();
        this.baseUrl = baseUrl;
    }

    /**
     * Собирает URL для начала OAuth-потока.
     */
    public String buildAuthUrl(long chatId) {
        String authorizeBase = baseUrl + "/oauth/authorize";
        return UriComponentsBuilder
                .fromHttpUrl(authorizeBase)
                .queryParam("response_type", "code")
                .queryParam("client_id", props.getClientId())
                .queryParam("state", chatId)
                .queryParam("redirect_uri", props.getRedirectUri())
                .encode()
                .toUriString();
    }

    /**
     * Обменивает code→токены, сохраняет профиль и возвращает сообщение.
     */
    public Mono<String> handleCallback(String code, long chatId) {
        return client.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", props.getClientId())
                        .with("client_secret", props.getClientSecret())
                        .with("code", code)
                        .with("redirect_uri", props.getRedirectUri()))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(tr -> {
                    repo.save(new HhProfile(chatId, tr.accessToken(), tr.refreshToken()));
                    log.info("Профиль {} сохранён с access_token={}", chatId, tr.accessToken());
                    return "Профиль успешно привязан";
                })
                .onErrorResume(e -> {
                    log.error("Ошибка привязки профиля {}", chatId, e);
                    return Mono.just("Не удалось привязать профиль");
                });
    }

    private static record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken
    ) {}
}
