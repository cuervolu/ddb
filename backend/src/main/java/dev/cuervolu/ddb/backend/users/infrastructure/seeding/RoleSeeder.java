package dev.cuervolu.ddb.backend.users.infrastructure.seeding;

import dev.cuervolu.ddb.backend.users.domain.Role;
import dev.cuervolu.ddb.backend.users.domain.RoleName;
import dev.cuervolu.ddb.backend.users.infrastructure.seeding.repositories.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleSeeder implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(RoleSeeder.class);
  private final RoleRepository roleRepository;

  public RoleSeeder(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  public void run(String... args) {
    seedRole(RoleName.ROLE_USER);
    seedRole(RoleName.ROLE_ADMIN);
  }

  private void seedRole(RoleName roleName) {
    if (roleRepository.findByName(roleName).isEmpty()) {
      roleRepository.save(new Role(roleName));
      log.info("Seeding role: {}", roleName);
    }
  }
}