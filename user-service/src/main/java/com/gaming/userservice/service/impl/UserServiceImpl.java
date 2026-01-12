package com.gaming.userservice.service.impl;

import com.gaming.userservice.dto.*;
import com.gaming.userservice.model.OrganizerTitle;
import com.gaming.userservice.model.Role;
import com.gaming.userservice.model.User;
import com.gaming.userservice.repository.UserRepository;
import com.gaming.userservice.security.JwtService;
import com.gaming.userservice.service.UserService;
import com.gaming.userservice.service.NotificationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationProducer notificationProducer;
    private final JwtService jwtService;

    @Override
    public User getUserByIdRaw(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Password Check
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Requirement: Prohibition of use checks
        if (!user.isActivated()) {
            throw new RuntimeException("Account is not activated. Please check your email.");
        }

        if (user.isBlocked()) {
            throw new RuntimeException("Account is blocked by Admin.");
        }

        // Generate JWT
        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        return new LoginResponse(token);
    }

    @Override
    public UserResponse registerPlayer(RegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .dateOfBirth(request.getDateOfBirth())
                .role(Role.PLAYER)
                .activated(false)
                .blocked(false)
                .build();


        User savedUser = userRepository.save(user);

        NotificationMessage msg = NotificationMessage.builder()
                .recipientEmail(savedUser.getEmail())
                .type("ACTIVATION")
                .content(savedUser.getId().toString())
                .build();

        notificationProducer.sendNotification(msg);

        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .role(savedUser.getRole().name())
                .build();
    }

    @Override
    public void activateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActivated(true);
        userRepository.save(user);
    }

    @Override
    public UserStatsResponse getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserStatsResponse.builder()
                .userId(user.getId())
                .attendancePercentage(user.getAttendancePercentage())
                .blocked(user.isBlocked())
                .build();
    }

    @Override
    public void incrementRegisteredSessions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTotalRegisteredSessions(user.getTotalRegisteredSessions() + 1);
        userRepository.save(user);
    }

    @Override
    public void processSessionAttendance(SessionAttendanceReport report) {

        User organizer = userRepository.findById(report.getOrganizerId())
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        organizer.setSuccessfulOrganizedSessions(organizer.getSuccessfulOrganizedSessions() + 1);
        organizer.setAttendedSessions(organizer.getAttendedSessions() + 1); // Logic implies organizer attended
        updateOrganizerTitle(organizer); // Check for promotion
        userRepository.save(organizer);

        for (Long userId : report.getPresentUserIds()) {
            User u = userRepository.findById(userId).orElse(null);
            if (u != null) {
                u.setAttendedSessions(u.getAttendedSessions() + 1);
                updatePercentage(u);
                userRepository.save(u);
            }
        }

        for (Long userId : report.getAbsentUserIds()) {
            User u = userRepository.findById(userId).orElse(null);
            if (u != null) {
                u.setAbandonedSessions(u.getAbandonedSessions() + 1);
                updatePercentage(u);
                userRepository.save(u);
            }
        }
    }

    private void updatePercentage(User user) {
        double totalRelevant = user.getAttendedSessions() + user.getAbandonedSessions();
        if (totalRelevant > 0) {
            double pct = (user.getAttendedSessions() / totalRelevant) * 100.0;
            user.setAttendancePercentage(pct);
        } else {
            user.setAttendancePercentage(100.0);
        }
    }

    private void updateOrganizerTitle(User user) {
        int count = user.getSuccessfulOrganizedSessions();
        OrganizerTitle newTitle = OrganizerTitle.ZASTAVNIK;

        if (count >= 100) newTitle = OrganizerTitle.GENERAL;
        else if (count >= 50) newTitle = OrganizerTitle.PUKOVNIK;
        else if (count >= 25) newTitle = OrganizerTitle.KAPETAN;
        else if (count >= 10) newTitle = OrganizerTitle.PORUCNIK;

        if (user.getTitle() != newTitle) {
            user.setTitle(newTitle);

            com.gaming.userservice.dto.NotificationMessage msg = com.gaming.userservice.dto.NotificationMessage.builder()
                    .recipientEmail(user.getEmail())
                    .type("TITLE_UPDATE")
                    .content("Congratulations! New Title: " + newTitle)
                    .build();
            notificationProducer.sendNotification(msg);
        }
    }

    @Override
    public void toggleBlockUser(Long userId, boolean blocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBlocked(blocked);
        userRepository.save(user);
    }

    @Override
    public UserResponse updateUserProfile(Long userId, RegistrationRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(req.getName());
        user.setSurname(req.getSurname());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}