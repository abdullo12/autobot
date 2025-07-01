package org.example.autobot.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.example.autobot.model.HhProfile;
import org.example.autobot.repository.HhProfileRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HhAuthControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private HhProfileRepository repository;

    private static MockWebServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        server.shutdown();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("hh.base-url", () -> server.url("/").toString());
    }

    @BeforeEach
    void cleanRepo() {
        repository.deleteAll();
    }

    @Test
    void callback_success_persistsProfile() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"access_token\":\"AT\",\"refresh_token\":\"RT\"}"));

        webClient.get().uri("/callback?code=test&state=123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Профиль успешно привязан");

        Optional<HhProfile> saved = repository.findById(123L);
        assertTrue(saved.isPresent());
        assertEquals("AT", saved.get().getAccessToken());
        assertEquals("RT", saved.get().getRefreshToken());
    }

    @Test
    void callback_error_returns500() {
        server.enqueue(new MockResponse().setResponseCode(500));

        webClient.get().uri("/callback?code=test&state=123")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Ошибка привязки профиля");

        assertTrue(repository.findById(123L).isEmpty());
    }
}