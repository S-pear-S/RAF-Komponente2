package com.gaming.userservice.controller;

import com.gaming.userservice.dto.LoginRequest;
import com.gaming.userservice.dto.LoginResponse;
import com.gaming.userservice.dto.RegistrationRequest;
import com.gaming.userservice.dto.UserResponse;
import com.gaming.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegistrationRequest request) {
        return new ResponseEntity<>(userService.registerPlayer(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/activate/{id}")
    public ResponseEntity<String> activate(@PathVariable Long id) {
        userService.activateAccount(id);
        return ResponseEntity.ok("Account successfully activated!");
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<com.gaming.userservice.dto.UserStatsResponse> getUserStats(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserStats(id));
    }

    @PostMapping("/{id}/increment-registered")
    public void incrementRegisteredSessions(@PathVariable Long id) {
        userService.incrementRegisteredSessions(id);
    }

    @PostMapping("/details")
    public ResponseEntity<java.util.List<com.gaming.userservice.dto.UserResponse>> getUserDetails(@RequestBody java.util.List<Long> userIds) {
        java.util.List<com.gaming.userservice.dto.UserResponse> response = userIds.stream()
                .map(id -> userService.getUserStats(id))
                .map(stats -> {
                    com.gaming.userservice.model.User u = userService.getUserByIdRaw(stats.getUserId());
                    return com.gaming.userservice.dto.UserResponse.builder()
                            .id(u.getId())
                            .email(u.getEmail())
                            .username(u.getUsername())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/report-attendance")
    public void reportAttendance(@RequestBody com.gaming.userservice.dto.SessionAttendanceReport report) {
        userService.processSessionAttendance(report);
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long id, @RequestParam boolean block) {
        userService.toggleBlockUser(id, block);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(@PathVariable Long id, @RequestBody @Valid RegistrationRequest req) {
        return ResponseEntity.ok(userService.updateUserProfile(id, req));
    }
}