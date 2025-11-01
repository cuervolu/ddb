package dev.cuervolu.ddb.backend.users.api;

import dev.cuervolu.ddb.backend.users.api.dto.AuthenticationRequest;
import dev.cuervolu.ddb.backend.users.api.dto.AuthenticationResponse;
import dev.cuervolu.ddb.backend.users.api.dto.RegistrationRequest;
import dev.cuervolu.ddb.backend.users.application.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

  private final AuthenticationService authService;

  public AuthenticationController(AuthenticationService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ResponseEntity<Void> register(
      @RequestBody @Valid RegistrationRequest request
  ) {
    authService.register(request);
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @RequestBody @Valid AuthenticationRequest request
  ) {
    return ResponseEntity.ok(authService.authenticate(request));
  }

  @GetMapping("/activate-account")
  public ResponseEntity<Void> activateAccount(
      @RequestParam String token
  ) {
    authService.activateAccount(token);
    return ResponseEntity.ok().build();
  }
}
