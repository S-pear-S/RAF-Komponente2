package com.gaming.sessionservice.dto;

import lombok.Data;

@Data
public class UserStatsResponse {
    private Long userId;
    private double attendancePercentage;
    private boolean blocked;
}