package dev.cuervolu.ddb.backend.users.infrastructure.seeding.repositories;

import dev.cuervolu.ddb.backend.users.domain.Role;
import dev.cuervolu.ddb.backend.users.domain.RoleName;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Long> {

  Optional<Role> findByName(RoleName name);



}