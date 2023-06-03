package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Token;
import nl.inholland.bank.models.dtos.ExceptionResponse;
import nl.inholland.bank.models.dtos.AuthDTO.LoginRequest;
import nl.inholland.bank.models.dtos.AuthDTO.RefreshTokenRequest;
import nl.inholland.bank.models.dtos.AuthDTO.jwt;
import nl.inholland.bank.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> login(@Validated @RequestBody LoginRequest loginRequest) throws AuthenticationException {
        try {
            Token token = userService.login(loginRequest);
            int id = userService.getUserIdByUsername(loginRequest.username());
            return ResponseEntity.status(200).body(new jwt(
                    token.jwt(),
                    userService.createRefreshToken(loginRequest.username()),
                    id,
                    token.expiresAt()
            ));
        } catch (AuthenticationException e) {
            // In this case, we don't want to tell the user that the username or password is incorrect.
            // This may lead potential attacker to know that the username is correct, and only the password is wrong.
            return ResponseEntity.status(401).body(new ExceptionResponse("Invalid username or password"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@Validated @RequestBody RefreshTokenRequest refreshTokenRequest) throws AuthenticationException {
        return ResponseEntity.status(200).body(userService.refresh(refreshTokenRequest));
    }
}
