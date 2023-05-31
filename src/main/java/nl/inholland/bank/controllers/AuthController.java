package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Token;
import nl.inholland.bank.models.dtos.ExceptionResponse;
import nl.inholland.bank.models.dtos.AuthDTO.LoginRequest;
import nl.inholland.bank.models.dtos.AuthDTO.RefreshTokenRequest;
import nl.inholland.bank.models.dtos.AuthDTO.jwt;
import nl.inholland.bank.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> login(@Validated @RequestBody LoginRequest loginRequest) throws AuthenticationException {
        Token token = userService.login(loginRequest);
        return ResponseEntity.status(200).body(new jwt(
                token.jwt(),
                userService.createRefreshToken(loginRequest.username()),
                loginRequest.username(),
                token.expiresAt()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@Validated @RequestBody RefreshTokenRequest refreshTokenRequest) throws AuthenticationException {
        return ResponseEntity.status(200).body(userService.refresh(refreshTokenRequest));
    }
}
