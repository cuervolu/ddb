package dev.cuervolu.ddb.backend.users.infrastructure.config;

import dev.cuervolu.ddb.backend.users.domain.User;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ApplicationAuditAware implements AuditorAware<String> {
  private static final Long OAUTH2_REGISTRATION_USER_ID = -1L;
  private static final ThreadLocal<String> CURRENT_AUDITOR = new ThreadLocal<>();

  public static void setCurrentAuditor(String userId) {
    CURRENT_AUDITOR.set(userId);
  }

  public static void clearCurrentAuditor() {
    CURRENT_AUDITOR.remove();
  }

  @Override
  @NonNull
  public Optional<String> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
      return Optional.empty();
    }
    User userPrincipal = (User) authentication.getPrincipal();
    return Optional.of(userPrincipal.getId().toString());
  }
}