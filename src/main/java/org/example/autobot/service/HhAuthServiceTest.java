package org.example.autobot.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.example.autobot.model.HhProfile;
import org.example.autobot.repository.HhProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HhAuthServiceTest {

    private MockWebServer server;
    private InMemoryRepo repo;
    private HhAuthService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        repo = new InMemoryRepo();
        // Внедряем WebClient с базовым URL mock-сервера
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(server.url("/").toString());
        service = new HhAuthService(repo, builder, server.url("/").toString());
        // Устанавливаем приватные поля через рефлексию
        ReflectionTestUtils.setField(service, "clientId", "testId");
        ReflectionTestUtils.setField(service, "clientSecret", "testSecret");
        ReflectionTestUtils.setField(service, "redirectUri", "http://localhost/callback");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void buildAuthUrl_containsParameters() {
        long chatId = 42L;
        String url = service.buildAuthUrl(chatId);

        var params = UriComponentsBuilder.fromUriString(url).build().getQueryParams();
        assertEquals("code", params.getFirst("response_type"));
        assertEquals("testId", params.getFirst("client_id"));
        assertEquals(String.valueOf(chatId), params.getFirst("state"));
        assertEquals("http://localhost/callback", params.getFirst("redirect_uri"));
    }

    @Test
    void handleCallback_savesProfile() {
        // Подготовка mock-ответа сервера
        server.enqueue(new MockResponse()
                .setBody("{\"access_token\":\"AT\",\"refresh_token\":\"RT\"}")
                .addHeader("Content-Type", "application/json"));

        // Выполнение метода и блокировка до завершения
        Mono<Void> result = service.handleCallback("authCode", 7L);
        result.block();

        Optional<HhProfile> saved = repo.findById(7L);
        assertTrue(saved.isPresent(), "Профиль должен быть сохранён");
        assertEquals("AT", saved.get().getAccessToken());
        assertEquals("RT", saved.get().getRefreshToken());
    }

    /**
     * Простая in-memory реализация репозитория
     */
    private static class InMemoryRepo implements HhProfileRepository {
        private final Map<Long, HhProfile> storage = new HashMap<>();

        @Override
        public <S extends HhProfile> S save(S entity) {
            storage.put(entity.getChatId(), entity);
            return entity;
        }

        @Overridepackage org.example.autobot.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.example.autobot.model.HhProfile;
import org.example.autobot.repository.HhProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HhAuthServiceTest {

    private MockWebServer server;
    private InMemoryRepo repo;
    private HhAuthService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        repo = new InMemoryRepo();
        // Внедряем WebClient с базовым URL mock-сервера
        WebClient.Builder builder = WebClient.builder()
            .baseUrl(server.url("/").toString());
        service = new HhAuthService(repo, builder, server.url("/").toString());
        // Устанавливаем приватные поля через рефлексию
        ReflectionTestUtils.setField(service, "clientId", "testId");
        ReflectionTestUtils.setField(service, "clientSecret", "testSecret");
        ReflectionTestUtils.setField(service, "redirectUri", "http://localhost/callback");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void buildAuthUrl_containsParameters() {
        long chatId = 42L;
        String url = service.buildAuthUrl(chatId);

        var params = UriComponentsBuilder.fromUriString(url).build().getQueryParams();
        assertEquals("code", params.getFirst("response_type"));
        assertEquals("testId", params.getFirst("client_id"));
        assertEquals(String.valueOf(chatId), params.getFirst("state"));
        assertEquals("http://localhost/callback", params.getFirst("redirect_uri"));
    }

    @Test
    void handleCallback_savesProfile() {
        // Подготовка mock-ответа сервера
        server.enqueue(new MockResponse()
                .setBody("{\"access_token\":\"AT\",\"refresh_token\":\"RT\"}")
                .addHeader("Content-Type", "application/json"));

        // Выполнение метода и блокировка до завершения
        Mono<Void> result = service.handleCallback("authCode", 7L);
        result.block();

        Optional<HhProfile> saved = repo.findById(7L);
        assertTrue(saved.isPresent(), "Профиль должен быть сохранён");
        assertEquals("AT", saved.get().getAccessToken());
        assertEquals("RT", saved.get().getRefreshToken());
    }

    /**
     * Простая in-memory реализация репозитория
     */
    private static class InMemoryRepo implements HhProfileRepository {
        private final Map<Long, HhProfile> storage = new HashMap<>();

        @Override
        public <S extends HhProfile> S save(S entity) {

            storage.put(entity.getChatId(), entity);
            return entity;
        }

        @Override
        public Optional<HhProfile> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        // Остальные методы не используются в тестах
        @Override public <S extends HhProfile> Iterable<S> saveAll(Iterable<S> entities) { throw new UnsupportedOperationException(); }
        @Override public boolean existsById(Long id) { return storage.containsKey(id); }
        @Override public Iterable<HhProfile> findAll() { return storage.values(); }
        @Override public Iterable<HhProfile> findAllById(Iterable<Long> ids) { throw new UnsupportedOperationException(); }
        @Override public long count() { return storage.size(); }
        @Override public void deleteById(Long id) { storage.remove(id); }
        @Override public void delete(HhProfile entity) { storage.remove(entity.getChatId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { throw new UnsupportedOperationException(); }
        @Override public void deleteAll(Iterable<? extends HhProfile> entities) { throw new UnsupportedOperationException(); }
        @Override public void deleteAll() { storage.clear(); }
    }
}

        public Optional<HhProfile> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        // Остальные методы не используются в тестах
        @Override public <S extends HhProfile> Iterable<S> saveAll(Iterable<S> entities) { throw new UnsupportedOperationException(); }
        @Override public boolean existsById(Long id) { return storage.containsKey(id); }
        @Override public Iterable<HhProfile> findAll() { return storage.values(); }
        @Override public Iterable<HhProfile> findAllById(Iterable<Long> ids) { throw new UnsupportedOperationException(); }
        @Override public long count() { return storage.size(); }
        @Override public void deleteById(Long id) { storage.remove(id); }
        @Override public void delete(HhProfile entity) { storage.remove(entity.getChatId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { throw new UnsupportedOperationException(); }
        @Override public void deleteAll(Iterable<? extends HhProfile> entities) { throw new UnsupportedOperationException(); }
        @Override public void deleteAll() { storage.clear(); }
    }
}
