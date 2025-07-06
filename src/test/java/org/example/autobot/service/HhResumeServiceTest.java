package org.example.autobot.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.example.autobot.model.HhProfile;
import org.example.autobot.repository.HhProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HhResumeServiceTest {

    private MockWebServer server;
    private InMemoryRepo repo;
    private HhResumeService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        repo = new InMemoryRepo();
        WebClient.Builder builder = WebClient.builder().baseUrl(server.url("/").toString());
        service = new HhResumeService(repo, builder, server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void fetchMyResume_returnsJson() throws InterruptedException {
        repo.save(new HhProfile(1L, "AT", "RT"));
        server.enqueue(new MockResponse()
                .setBody("{\"ok\":true}")
                .addHeader("Content-Type", "application/json"));

        String json = service.fetchMyResume(1L).block();
        assertEquals("{\"ok\":true}", json);

        RecordedRequest req = server.takeRequest();
        assertEquals("/resumes/mine", req.getPath());
        assertEquals("Bearer AT", req.getHeader("Authorization"));
    }

    @Test
    void fetchMyResume_noProfile() {
        assertThrows(IllegalStateException.class, () -> service.fetchMyResume(2L).block());
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
