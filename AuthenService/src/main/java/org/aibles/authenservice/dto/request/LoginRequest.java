package org.aibles.authenservice.dto.request;

public class LoginRequest {
    private String username;
    private String password;
    private long accessTokenLifeTime;
    private long refreshTokenLifeTime;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getAccessTokenLifeTime() {
        return accessTokenLifeTime;
    }

    public void setAccessTokenLifeTime(long accessTokenLifeTime) {
        this.accessTokenLifeTime = accessTokenLifeTime;
    }

    public long getRefreshTokenLifeTime() {
        return refreshTokenLifeTime;
    }

    public void setRefreshTokenLifeTime(long refreshTokenLifeTime) {
        this.refreshTokenLifeTime = refreshTokenLifeTime;
    }
}