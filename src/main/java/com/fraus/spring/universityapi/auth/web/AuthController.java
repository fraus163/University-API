package com.fraus.spring.universityapi.auth.web;

import com.fraus.spring.universityapi.auth.domain.AuthService;
import com.fraus.spring.universityapi.auth.web.dto.JwtResponse;
import com.fraus.spring.universityapi.auth.web.dto.LoginRequest;
import com.fraus.spring.universityapi.auth.web.dto.SignupRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public JwtResponse authenticateUser(
            @RequestBody @Valid LoginRequest loginRequest
    ) {
        log.info("Controller authenticateUser is called");
        return authService.signIn(loginRequest);
    }

    @PostMapping("/signup")
    public void registerUser(
            @RequestBody @Valid SignupRequest request
    ) {
        log.info("Controller registerUser is called");
        authService.signUp(request);
    }
}
