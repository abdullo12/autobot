package org.example.autobot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HhAuthService {

    @Value("${hh.oauth.client-id}")
    private String clientId;

    @Value("${hh.oauth.redirect-uri}")
    private String redirectUri;

    public String buildAuthUrl(long chatId) {
        return String.format(
                "https://hh.ru/oauth/authorize?response_type=code&client_id=%s&state=%d&redirect_uri=%s",
                clientId,
                chatId,
                redirectUri
        );
    }
}
