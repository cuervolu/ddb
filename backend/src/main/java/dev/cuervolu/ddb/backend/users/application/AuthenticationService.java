package dev.cuervolu.ddb.backend.users.application;

import dev.cuervolu.ddb.backend.users.api.dto.AuthenticationRequest;
import dev.cuervolu.ddb.backend.users.api.dto.AuthenticationResponse;
import dev.cuervolu.ddb.backend.users.api.dto.RegistrationRequest;

public interface AuthenticationService {

  void register(RegistrationRequest request);

  AuthenticationResponse authenticate(AuthenticationRequest request);

  void activateAccount(String token);
}