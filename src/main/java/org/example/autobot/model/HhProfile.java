package org.example.autobot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class HhProfile {
    @Id
    private Long chatId;
    private String accessToken;
    private String refreshToken;

    public HhProfile() {
    }

    public HhProfile(Long chatId, String accessToken, String refreshToken) {
        this.chatId = chatId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
