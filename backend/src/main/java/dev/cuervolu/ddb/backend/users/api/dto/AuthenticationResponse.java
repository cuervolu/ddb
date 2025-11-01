package dev.cuervolu.ddb.backend.users.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationResponse {

  private String token;

  @JsonProperty("refresh_token")
  private String refreshToken;

  public AuthenticationResponse(String token, String refreshToken) {
    this.token = token;
    this.refreshToken = refreshToken;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}