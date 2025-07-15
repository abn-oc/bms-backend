package com.bms.bms.controller;

import com.bms.bms.dto.LoginResponse;
import com.bms.bms.dto.LoginUserDto;
import com.bms.bms.dto.RegisterUserDto;
import com.bms.bms.model.User;
import com.bms.bms.service.AuthenticationService;
import com.bms.bms.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@Tag(name = "Auth Routes")
public class UserController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    public UserController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/auth/signup")
    public String register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto);
            return "Success. User Signed up.";
        }
        catch (Exception e) {
            return "Error. Couldn't Sign up";
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto) {
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);
            String jwtToken = jwtService.generateToken(authenticatedUser);
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(jwtToken);
            authenticatedUser.setPassword("hidden");
            loginResponse.setUser(authenticatedUser);
            return ResponseEntity.ok(loginResponse);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body("Error. Couldn't Login");
        }
    }
}