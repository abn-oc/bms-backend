package com.bms.bms.controller;

import com.bms.bms.dto.*;
import com.bms.bms.model.User;
import com.bms.bms.repository.UserRepository;
import com.bms.bms.service.AuthenticationService;
import com.bms.bms.service.EmailService;
import com.bms.bms.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@RestController
@Tag(name = "Auth Routes")
public class UserController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    private final UserRepository userRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    public UserController(JwtService jwtService, AuthenticationService authenticationService, UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<String> register(@RequestBody RegisterUserDto registerUserDto) {
        String email = registerUserDto.getEmail();
        String username = registerUserDto.getUsername();
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Error: Email already exists");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Error: Username already exists");
        }
        try {
            authenticationService.signup(registerUserDto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Success: User signed up.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Couldn't sign up. " + e.getMessage());
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

    @PostMapping("/auth/forgot-password")
    public String forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        String email = forgotPasswordDto.getEmail();
        User usr = userRepository.findByEmail(email).orElse(null);
        if (usr == null) {
            return "Error. Given email does not belong to any user";
        }
        if (usr.getCodeGeneratedTime() != null && usr.getCodeGeneratedTime().isAfter(LocalDateTime.now().minusSeconds(60))) {
            return "Error. Wait 60 seconds before regenerating code";
        }
        String seed = email + System.currentTimeMillis();
        String code = String.format("%06d", Math.abs(seed.hashCode()) % 1000000);
        usr.setResetCode(code);
        usr.setCodeGeneratedTime(LocalDateTime.now());
        userRepository.save(usr);
        emailService.sendCodeMail(email, code);
        return "Success. Sent code via email";
    }

    // not tested
    @PostMapping("/auth/reset-password")
    public String resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        String code = resetPasswordDto.getCode();
        String newPassword = resetPasswordDto.getNewPassword();
        User usr = userRepository.findByResetCode(code).orElse(null);
        if (usr == null) {
            return "Error. This code is not associated with any account.";
        }
        if (usr.getCodeGeneratedTime() != null &&
                usr.getCodeGeneratedTime().isBefore(LocalDateTime.now().minusSeconds(60))) {
            return "Error. This code is expired as its more than 60 seconds old";
        }
        String hashedPassword = passwordEncoder.encode(newPassword);
        usr.setPassword(hashedPassword);
        usr.setResetCode(null);
        usr.setCodeGeneratedTime(null);
        userRepository.save(usr);
        return "Success. New Password set";
    }
}