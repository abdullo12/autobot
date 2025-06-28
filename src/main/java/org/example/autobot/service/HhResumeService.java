package org.example.autobot.service;

import org.example.autobot.model.HhProfile;
import org.example.autobot.repository.HhProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class HhResumeService {
    private static final Logger log = LoggerFactory.getLogger(HhResumeService.class);
    private final WebClient client;
    private final HhProfileRepository repo;

    public HhResumeService(
            HhProfileRepository repo,
            WebClient.Builder builder,
            @Value("${hh.base-url:https://hh.ru}") String baseUrl
    ) {
        this.repo = repo;
        this.client = builder.baseUrl(baseUrl).build();
    }

    /**
     * Возвращает JSON-строку с резюме пользователя.
     */
    public Mono<String> fetchMyResume(Long chatId) {
        return repo.findById(chatId)
                .map(HhProfile::getAccessToken)
                .map(token ->
                        client.get()
                                .uri("/resumes/mine")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .bodyToMono(String.class)
                )
                .orElseGet(() -> {
                    log.warn("Нет токена для chatId={}", chatId);
                    return Mono.error(new IllegalStateException("Профиль hh.ru не привязан"));
                });
    }
}
