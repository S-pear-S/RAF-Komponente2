package com.gaming.sessionservice.client;

import com.gaming.sessionservice.dto.UserStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}/stats")
    UserStatsResponse getUserStats(@PathVariable("id") Long id);

    @PostMapping("/api/users/{id}/increment-registered")
    void incrementRegisteredCount(@PathVariable("id") Long id);

    @org.springframework.web.bind.annotation.PostMapping("/api/users/details")
    java.util.List<com.gaming.sessionservice.dto.UserResponse> getUserDetails(java.util.List<Long> ids);

    @org.springframework.web.bind.annotation.PostMapping("/api/users/report-attendance")
    void reportAttendance(com.gaming.sessionservice.dto.SessionAttendanceReport report);
}