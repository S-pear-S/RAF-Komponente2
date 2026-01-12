package com.gaming.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsResponse {
    private Long userId;
    private double attendancePercentage;
    private boolean blocked;
}