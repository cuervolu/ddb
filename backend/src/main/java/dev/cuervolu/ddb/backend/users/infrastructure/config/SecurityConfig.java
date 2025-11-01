package dev.cuervolu.ddb.backend.users.infrastructure.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import dev.cuervolu.ddb.backend.users.infrastructure.filters.JwtFilter;
import dev.cuervolu.ddb.backend.users.infrastructure.oauth.CustomOAuth2UserService;
import dev.cuervolu.ddb.backend.users.infrastructure.oauth.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

  private final JwtFilter jwtAuthFilter;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

  public SecurityConfig(
      JwtFilter jwtAuthFilter,
      CustomOAuth2UserService customOAuth2UserService,
      OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.customOAuth2UserService = customOAuth2UserService;
    this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      AuthenticationProvider authenticationProvider,
      CorsConfigurationSource corsConfigurationSource) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            req ->
                req.requestMatchers(
                        "/auth/**",
                        "/v2/api-docs",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-resources",
                        "/swagger-resources/**",
                        "/configuration/ui",
                        "/configuration/security",
                        "/swagger-ui/**",
                        "/webjars/**",
                        "/chat/ai/**",
                        "/ws/**",
                        "/swagger-ui.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(oauth2 -> oauth2
            .authorizationEndpoint(auth -> auth
                .baseUri("/oauth2/authorization")
            )
            .redirectionEndpoint(redirect -> redirect
                .baseUri("/login/oauth2/code/*")
            )
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService)
            )
            .successHandler(oAuth2LoginSuccessHandler)
        )
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }


  @Bean
  public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public AuditorAware<String> auditorAware() {
    return new ApplicationAuditAware();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}