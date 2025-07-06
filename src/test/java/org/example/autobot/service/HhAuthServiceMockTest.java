package org.example.autobot.service;

import org.example.autobot.config.HhOAuthProperties;
import org.example.autobot.model.HhProfile;
import org.example.autobot.repository.HhProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HhAuthServiceMockTest {

    @Test
    void handleCallback_success() {
        ExchangeFunction exchange = request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .body("{\"access_token\":\"AT\",\"refresh_token\":\"RT\"}")
                        .build());

        InMemoryRepo repo = new InMemoryRepo();
        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchange);
        HhOAuthProperties props = new HhOAuthProperties();
        props.setClientId("id");
        props.setClientSecret("sec");
        props.setRedirectUri("http://localhost/callback");

        HhAuthService service = new HhAuthService(repo, builder, props, "http://test");

        String msg = service.handleCallback("code", 5L).block();
        assertEquals("Профиль успешно привязан", msg);

        Optional<HhProfile> saved = repo.findById(5L);
        assertTrue(saved.isPresent());
        assertEquals("AT", saved.get().getAccessToken());
        assertEquals("RT", saved.get().getRefreshToken());
    }

    @Test
    void handleCallback_error() {
        ExchangeFunction exchange = request -> Mono.error(new RuntimeException("fail"));

        InMemoryRepo repo = new InMemoryRepo();
        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchange);
        HhOAuthProperties props = new HhOAuthProperties();
        props.setClientId("id");
        props.setClientSecret("sec");
        props.setRedirectUri("http://localhost/callback");

        HhAuthService service = new HhAuthService(repo, builder, props, "http://test");

        String msg = service.handleCallback("code", 5L).block();
        assertEquals("Не удалось привязать профиль", msg);
        assertTrue(repo.findById(5L).isEmpty());
    }

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

        // Unused methods
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