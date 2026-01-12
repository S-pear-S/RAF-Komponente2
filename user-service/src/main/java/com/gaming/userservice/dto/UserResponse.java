package com.gaming.userservice.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String role;
}