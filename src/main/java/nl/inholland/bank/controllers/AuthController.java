package nl.inholland.bank.controllers;

import nl.inholland.bank.models.dtos.ExceptionResponse;
import nl.inholland.bank.models.dtos.LoginRequest;
import nl.inholland.bank.models.dtos.RefreshTokenRequest;
import nl.inholland.bank.models.dtos.jwt;
import nl.inholland.bank.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> login(@Validated @RequestBody LoginRequest loginRequest) {
        try {
            return ResponseEntity.status(200).body(new jwt(userService.login(loginRequest), userService.createRefreshToken(loginRequest.username())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExceptionResponse("Unable to login"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@Validated @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            return ResponseEntity.status(200).body(userService.refresh(refreshTokenRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExceptionResponse("Unable to refresh token"));
        }
    }
}
