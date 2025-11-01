package dev.cuervolu.ddb.backend.users.infrastructure.oauth;

import dev.cuervolu.ddb.backend.users.domain.User;
import dev.cuervolu.ddb.backend.users.infrastructure.seeding.repositories.UserRepository;
import dev.cuervolu.ddb.backend.users.infrastructure.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtService jwtService;
  private final UserRepository userRepository;

  @Value("${application.oauth2.redirect-uri}")
  private String redirectUri;

  public OAuth2LoginSuccessHandler(JwtService jwtService, UserRepository userRepository) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

    String email = oAuth2User.getAttribute("email");
    String username = oAuth2User.getAttribute("login");

    if (email == null) {
      email = username + "@github.user";
    }

    User user = userRepository.findByEmail(email)
        .or(() -> userRepository.findByUsername(username))
        .orElseThrow(() -> new IllegalStateException("User not found in DB after OAuth login"));

    String token = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
        .queryParam("token", token)
        .queryParam("refresh_token", refreshToken)
        .build().toUriString();

    clearAuthenticationAttributes(request);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }
}