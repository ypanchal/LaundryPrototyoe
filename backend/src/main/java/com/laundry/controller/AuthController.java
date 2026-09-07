package com.laundry.controller;

import com.laundry.dto.LoginRequest;
import com.laundry.dto.LoginResponse;
import com.laundry.model.User;
import com.laundry.repository.UserRepository;
import com.laundry.security.TokenStore;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final TokenStore tokenStore;

    public AuthController(UserRepository userRepository, TokenStore tokenStore) {
        this.userRepository = userRepository;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByPhone(request.getPhone().trim());

        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid phone number or password."));
        }

        User user = userOpt.get();
        String token = tokenStore.createToken(user.getPhone(), user.getRole(), user.getName());
        return ResponseEntity.ok(new LoginResponse(token, user.getRole(), user.getName(), user.getPhone()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String header) {
        if (header != null && header.startsWith("Bearer ")) {
            tokenStore.invalidate(header.substring(7));
        }
        return ResponseEntity.ok().build();
    }
}
