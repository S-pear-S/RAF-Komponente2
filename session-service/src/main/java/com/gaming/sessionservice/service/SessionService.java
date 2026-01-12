package com.gaming.sessionservice.service;

import com.gaming.sessionservice.client.UserServiceClient;
import com.gaming.sessionservice.dto.CreateSessionRequest;
import com.gaming.sessionservice.dto.NotificationMessage;
import com.gaming.sessionservice.dto.UserResponse;
import com.gaming.sessionservice.dto.UserStatsResponse;
import com.gaming.sessionservice.model.Game;
import com.gaming.sessionservice.model.GamingSession;
import com.gaming.sessionservice.model.SessionStatus;
import com.gaming.sessionservice.repository.GameRepository;
import com.gaming.sessionservice.repository.SessionRepository;
import com.gaming.sessionservice.repository.SessionSpecification;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final GameRepository gameRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationProducer notificationProducer;

    public List<GamingSession> searchSessions(String keyword, Long gameId, Boolean isClosed, Integer maxPlayers) {
        Specification<GamingSession> spec = SessionSpecification.getSessions(keyword, gameId, isClosed, maxPlayers);
        return sessionRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "startTime"));
    }

    public void joinSession(Long sessionId, Long userId) {
        UserStatsResponse stats = userServiceClient.getUserStats(userId);
        if (stats.isBlocked()) {
            throw new RuntimeException("Blocked user cannot join sessions.");
        }

        GamingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getParticipants().contains(userId)) {
            throw new RuntimeException("Already joined!");
        }
        if (session.getParticipants().size() >= session.getMaxPlayers()) {
            throw new RuntimeException("Session is full.");
        }
        if (session.isClosed()) {
            throw new RuntimeException("Cannot join closed session without invitation.");
        }

        session.getParticipants().add(userId);
        sessionRepository.save(session);

        userServiceClient.incrementRegisteredCount(userId);
    }

    public void invitePlayer(Long sessionId, Long organizerId, String email) {
        GamingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getOrganizerId().equals(organizerId)) {
            throw new RuntimeException("Only organizer can invite players.");
        }

        NotificationMessage msg = NotificationMessage.builder()
                .recipientEmail(email)
                .type("INVITATION")
                .content(session.getName() + " (ID: " + session.getId() + ")")
                .build();

        notificationProducer.sendNotification(msg);
    }

    public void cancelSession(Long sessionId, Long organizerId) {
        GamingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getOrganizerId().equals(organizerId)) {
            throw new RuntimeException("Only organizer can cancel.");
        }

        session.setStatus(SessionStatus.CANCELLED);
        sessionRepository.save(session);


        if (!session.getParticipants().isEmpty()) {
            List<UserResponse> users = userServiceClient.getUserDetails(session.getParticipants());

            for (UserResponse u : users) {
                if(u.getId().equals(organizerId)) continue;

                NotificationMessage msg = NotificationMessage.builder()
                        .recipientEmail(u.getEmail())
                        .type("CANCELLATION")
                        .content("Session " + session.getName() + " was cancelled.")
                        .build();
                notificationProducer.sendNotification(msg);
            }
        }
    }

    @Retry(name = "userService")
    public GamingSession createSession(CreateSessionRequest request, Long userId) {
        System.out.println("Attempting to check user stats...");
        UserStatsResponse stats = userServiceClient.getUserStats(userId);

        if (stats.isBlocked()) {
            throw new RuntimeException("Blocked users cannot create sessions");
        }

        if (stats.getAttendancePercentage() < 90.0) {
            throw new RuntimeException("Attendance too low (<90%) to organize a session.");
        }

        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found"));

        GamingSession session = GamingSession.builder()
                .name(request.getName())
                .game(game)
                .maxPlayers(request.getMaxPlayers())
                .isClosed(request.isClosed())
                .startTime(request.getStartTime())
                .description(request.getDescription())
                .status(SessionStatus.SCHEDULED)
                .organizerId(userId)
                .build();

        session.getParticipants().add(userId);

        return sessionRepository.save(session);
    }

    @jakarta.transaction.Transactional
    public void finishSession(Long sessionId, Long organizerId, List<Long> absentIds) {
        GamingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getOrganizerId().equals(organizerId)) {
            throw new RuntimeException("Only organizer can finish session.");
        }
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new RuntimeException("Session not active.");
        }

        List<Long> allParticipants = session.getParticipants();

        List<Long> playersToCheck = new java.util.ArrayList<>(allParticipants);
        playersToCheck.remove(organizerId);

        List<Long> presentIds = new java.util.ArrayList<>();
        List<Long> validatedAbsentIds = new java.util.ArrayList<>();

        for (Long pId : playersToCheck) {
            if (absentIds.contains(pId)) {
                validatedAbsentIds.add(pId);
            } else {
                presentIds.add(pId);
            }
        }

        if (presentIds.isEmpty()) {
            throw new RuntimeException("Cannot finish session: No other players attended!");
        }

        session.setStatus(SessionStatus.COMPLETED);
        sessionRepository.save(session);

        com.gaming.sessionservice.dto.SessionAttendanceReport report = new com.gaming.sessionservice.dto.SessionAttendanceReport();
        report.setOrganizerId(organizerId);
        report.setPresentUserIds(presentIds);
        report.setAbsentUserIds(validatedAbsentIds);

        userServiceClient.reportAttendance(report);
    }
}