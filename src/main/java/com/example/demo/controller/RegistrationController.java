
package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.SignupRequestDTO;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(
            @RequestBody SignupRequestDTO request) {

        System.out.println("Signup role received: " + request.getRole());

        userService.createPublicUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                    "message",
                    "Account created successfully. Please log in."
                ));
    }
}
