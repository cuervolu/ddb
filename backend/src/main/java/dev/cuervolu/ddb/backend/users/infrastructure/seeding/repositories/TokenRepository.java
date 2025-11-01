package dev.cuervolu.ddb.backend.users.infrastructure.seeding.repositories;

import dev.cuervolu.ddb.backend.users.domain.Token;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends CrudRepository<Token, Integer> {

  Optional<Token> findByToken(String token);
}