package dev.cuervolu.ddb.backend.users.application.internal;

import dev.cuervolu.ddb.backend.users.api.dto.AuthenticationRequest;
import dev.cuervolu.ddb.backend.users.api.dto.AuthenticationResponse;
import dev.cuervolu.ddb.backend.users.api.dto.RegistrationRequest;
import dev.cuervolu.ddb.backend.users.application.AuthenticationService;
import dev.cuervolu.ddb.backend.users.domain.AuthProvider;
import dev.cuervolu.ddb.backend.users.domain.Role;
import dev.cuervolu.ddb.backend.users.domain.RoleName;
import dev.cuervolu.ddb.backend.users.domain.Token;
import dev.cuervolu.ddb.backend.users.domain.User;
import dev.cuervolu.ddb.backend.users.infrastructure.seeding.repositories.RoleRepository;
import dev.cuervolu.ddb.backend.users.infrastructure.seeding.repositories.TokenRepository;
import dev.cuervolu.ddb.backend.users.infrastructure.seeding.repositories.UserRepository;
import dev.cuervolu.ddb.backend.users.infrastructure.services.JwtService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;


@Service
public class AuthenticationServiceImpl implements AuthenticationService {

  private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenRepository tokenRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final Environment env;

  public AuthenticationServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, TokenRepository tokenRepository, AuthenticationManager authenticationManager, JwtService jwtService,
      Environment env) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenRepository = tokenRepository;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.env = env;
  }

  @Override
  @Transactional
  public void register(RegistrationRequest request) {
    Set<Role> roles = new HashSet<>();
    roles.add(roleRepository.findByName(RoleName.ROLE_USER)
        .orElseThrow(() -> new IllegalStateException("USER role not found")));

    if (userRepository.count() == 0) {
      roles.add(roleRepository.findByName(RoleName.ROLE_ADMIN)
          .orElseThrow(() -> new IllegalStateException("ADMIN role not found")));
    }

    var user = new User.UserBuilder()
        .username(request.getUsername())
        .displayName(request.getDisplayName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .provider(AuthProvider.LOCAL)
        .roles(roles)
        .isEnabled(false)
        .build();
    userRepository.save(user);

    String activationToken = generateAndSaveActivationToken(user);
    log.info("Activation Token for {}: {}", user.getEmail(), activationToken);
  }

  @Override
  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    var auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );

    var user = (User) auth.getPrincipal();
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);

    return new AuthenticationResponse(jwtToken, refreshToken);
  }

  @Override
  @Transactional
  public void activateAccount(String tokenValue) {
    Token savedToken = tokenRepository.findByToken(tokenValue)
        .orElseThrow(() -> new RuntimeException("Token not found"));

    if (Instant.now().isAfter(savedToken.getExpiresAt())) {
      throw new RuntimeException("Token expired");
    }

    User user = savedToken.getUser();
    user.setEnabled(true);
    userRepository.save(user);

    savedToken.setValidatedAt(Instant.now());
    tokenRepository.save(savedToken);
  }

  private String generateAndSaveActivationToken(User user) {
    String generatedToken = generateActivationCode();
    var token = new Token(
        generatedToken,
        Instant.now(),
        Instant.now().plus(15, ChronoUnit.MINUTES),
        user
    );
    tokenRepository.save(token);
    return generatedToken;
  }

  private String generateActivationCode() {
    String characters = "0123456789";
    StringBuilder codeBuilder = new StringBuilder();
    SecureRandom secureRandom = new SecureRandom();
    for (int i = 0; i < 6; i++) {
      int randomIndex = secureRandom.nextInt(characters.length());
      codeBuilder.append(characters.charAt(randomIndex));
    }
    return codeBuilder.toString();
  }

  private boolean isDevProfile() {
    return env.getActiveProfiles().length > 0 && env.getActiveProfiles()[0].equals("dev");
  }
}