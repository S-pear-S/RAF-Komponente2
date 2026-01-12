package com.gaming.userservice.service;

import com.gaming.userservice.dto.*;
import com.gaming.userservice.model.User;

public interface UserService {
    UserResponse registerPlayer(RegistrationRequest request);
    LoginResponse login(LoginRequest request);
    void activateAccount(Long userId);
    void incrementRegisteredSessions(Long userId);
    UserStatsResponse getUserStats(Long userId);
    User getUserByIdRaw(Long userId);
    void processSessionAttendance(com.gaming.userservice.dto.SessionAttendanceReport report);
    void toggleBlockUser(Long userId, boolean blocked);
    UserResponse updateUserProfile(Long userId, com.gaming.userservice.dto.RegistrationRequest updateRequest);
}