package com.harshith.E_Cart.DTO;

public class AuthResponseDto {

    private final String accessToken;
    private final String tokenType;
    private final long expiresIn;

    public AuthResponseDto(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
