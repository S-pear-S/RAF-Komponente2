package com.gaming.sessionservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GamingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    private int maxPlayers;

    private boolean isClosed;

    private LocalDateTime startTime;
    private String description;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private Long organizerId;

    @Builder.Default
    @ElementCollection
    private List<Long> participants = new ArrayList<>();
}