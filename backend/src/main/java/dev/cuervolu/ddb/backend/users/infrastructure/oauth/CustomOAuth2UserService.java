package dev.cuervolu.ddb.backend.users.infrastructure.oauth;

import dev.cuervolu.ddb.backend.users.domain.AuthProvider;
import dev.cuervolu.ddb.backend.users.domain.Role;
import dev.cuervolu.ddb.backend.users.domain.RoleName;
import dev.cuervolu.ddb.backend.users.domain.User;
import dev.cuervolu.ddb.backend.users.infrastructure.seeding.repositories.RoleRepository;
import dev.cuervolu.ddb.backend.users.infrastructure.seeding.repositories.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  public CustomOAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    Map<String, Object> attributes = oAuth2User.getAttributes();
    processOAuth2User(attributes);

    return oAuth2User;
  }

  private void processOAuth2User(Map<String, Object> attributes) {
    String email = (String) attributes.get("email");
    String username = (String) attributes.get("login");

    if (email == null) {
      email = username + "@github.user";
    }

    Optional<User> userOptional = userRepository.findByEmail(email);

    if (userOptional.isEmpty()) {
      userOptional = userRepository.findByUsername(username);
    }

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      if (user.getProvider() == AuthProvider.LOCAL) {
        user.setProvider(AuthProvider.GITHUB);
        user.setProviderId(String.valueOf(attributes.get("id")));
        user.setAvatarUrl((String) attributes.get("avatar_url"));
        user.setEmail(email);
        user.setEnabled(true);
        userRepository.save(user);
      }
    } else {
      Set<Role> roles = new HashSet<>();
      roles.add(roleRepository.findByName(RoleName.ROLE_USER)
          .orElseThrow(() -> new IllegalStateException("USER role not found")));

      if (userRepository.count() == 0) {
        roles.add(roleRepository.findByName(RoleName.ROLE_ADMIN)
            .orElseThrow(() -> new IllegalStateException("ADMIN role not found")));
      }

      User user = new User.UserBuilder()
          .username(username)
          .email(email)
          .displayName((String) attributes.get("name"))
          .avatarUrl((String) attributes.get("avatar_url"))
          .provider(AuthProvider.GITHUB)
          .providerId(String.valueOf(attributes.get("id")))
          .isEnabled(true)
          .roles(roles)
          .build();

      userRepository.save(user);
    }
  }
}