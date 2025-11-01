package dev.cuervolu.ddb.backend.users.infrastructure.seeding.repositories;

import dev.cuervolu.ddb.backend.users.domain.User;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);

}