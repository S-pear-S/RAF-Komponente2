package com.gaming.sessionservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateSessionRequest {
    @NotNull
    private String name;
    @NotNull
    private Long gameId;
    private int maxPlayers;
    private boolean isClosed;
    private LocalDateTime startTime;
    private String description;
}