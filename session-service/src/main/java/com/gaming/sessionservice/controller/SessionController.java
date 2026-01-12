package com.gaming.sessionservice.controller;

import com.gaming.sessionservice.dto.CreateSessionRequest;
import com.gaming.sessionservice.model.Game;
import com.gaming.sessionservice.model.GamingSession;
import com.gaming.sessionservice.repository.GameRepository;
import com.gaming.sessionservice.service.SessionService;
import com.gaming.sessionservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final JwtUtil jwtUtil;
    private final GameRepository gameRepository;

    @GetMapping
    public ResponseEntity<java.util.List<GamingSession>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) Boolean closed,
            @RequestParam(required = false) Integer maxPlayers
    ) {
        return ResponseEntity.ok(sessionService.searchSessions(keyword, gameId, closed, maxPlayers));
    }

    @PostMapping("/{sessionId}/join")
    public ResponseEntity<Void> joinSession(
            @PathVariable Long sessionId,
            @RequestHeader("Authorization") String token) {

        Long userId = jwtUtil.extractUserId(token);
        sessionService.joinSession(sessionId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/games")
    public Game createGame(@RequestBody Game game) {
        return gameRepository.save(game);
    }

    @PostMapping
    public ResponseEntity<GamingSession> createSession(
            @RequestHeader("Authorization") String token,
            @RequestBody CreateSessionRequest request) {

        Long userId = jwtUtil.extractUserId(token);
        return ResponseEntity.ok(sessionService.createSession(request, userId));
    }

    @PostMapping("/{sessionId}/invite")
    public ResponseEntity<Void> invite(
            @PathVariable Long sessionId,
            @RequestHeader("Authorization") String token,
            @RequestParam String email) {
        Long userId = jwtUtil.extractUserId(token);
        sessionService.invitePlayer(sessionId, userId, email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sessionId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long sessionId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        sessionService.cancelSession(sessionId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<Void> finishSession(
            @PathVariable Long sessionId,
            @RequestHeader("Authorization") String token,
            @RequestBody com.gaming.sessionservice.dto.FinishSessionRequest request) {

        Long userId = jwtUtil.extractUserId(token);
        sessionService.finishSession(sessionId, userId, request.getAbsentPlayerIds());
        return ResponseEntity.ok().build();
    }
}