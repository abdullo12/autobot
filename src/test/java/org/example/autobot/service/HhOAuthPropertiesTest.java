package org.example.autobot.service;

import org.example.autobot.config.HhOAuthProperties;
import org.example.autobot.repository.HhProfileRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HhOAuthPropertiesTest {

    @ParameterizedTest
    @CsvSource({
            "id1,http://host/cb1,5",
            "client2,https://ex.com/callback,9"
    })
    void buildAuthUrl_variousParams(String clientId, String redirectUri, long chatId) {
        HhOAuthProperties props = new HhOAuthProperties();
        props.setClientId(clientId);
        props.setClientSecret("sec");
        props.setRedirectUri(redirectUri);

        HhAuthService service = new HhAuthService(
                Mockito.mock(HhProfileRepository.class),
                WebClient.builder(), props, "https://hh.ru");

        String url = service.buildAuthUrl(chatId);
        assertEquals(
                String.format(
                        "https://hh.ru/oauth/authorize?response_type=code&client_id=%s&state=%d&redirect_uri=%s",
                        clientId, chatId, redirectUri
                ),
                url
        );
    }
}