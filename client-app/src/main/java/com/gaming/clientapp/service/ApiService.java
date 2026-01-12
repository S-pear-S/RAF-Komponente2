package com.gaming.clientapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApiService {

    @Value("${gateway.url}")
    private String gatewayUrl;

    public String login(String email, String password) {
        try {
            Map response = WebClient.create(gatewayUrl).post().uri("/user-service/api/users/login")
                    .bodyValue(Map.of("email", email, "password", password))
                    .retrieve().bodyToMono(Map.class).block();
            return (String) response.get("token");
        } catch (Exception e) { return null; }
    }

    public void register(String username, String password, String email, String name, String surname, String dob) {
        WebClient.create(gatewayUrl).post().uri("/user-service/api/users/register")
                .bodyValue(Map.of("username", username, "password", password, "email", email,
                        "name", name, "surname", surname, "dateOfBirth", dob))
                .retrieve().toBodilessEntity().block();
    }

    public List<Map> getSessions(String token, String keyword) {
        String uri = "/session-service/api/sessions";
        if (keyword != null && !keyword.isEmpty()) uri += "?keyword=" + keyword;

        return WebClient.create(gatewayUrl).get().uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve().bodyToMono(List.class).block();
    }

    public void createSession(String token, String name, Long gameId, int maxPlayers, boolean isClosed, String desc, String time) {
        WebClient.create(gatewayUrl).post().uri("/session-service/api/sessions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(Map.of("name", name, "gameId", gameId, "maxPlayers", maxPlayers,
                        "isClosed", isClosed, "description", desc, "startTime", time))
                .retrieve().bodyToMono(Map.class).block();
    }

    public void joinSession(String token, Long sessionId) {
        WebClient.create(gatewayUrl).post().uri("/session-service/api/sessions/" + sessionId + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve().toBodilessEntity().block();
    }

    public void invite(String token, Long sessionId, String email) {
        WebClient.create(gatewayUrl).post()
                .uri(uriBuilder -> uriBuilder.path("/session-service/api/sessions/{id}/invite")
                        .queryParam("email", email).build(sessionId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve().toBodilessEntity().block();
    }

    public void cancel(String token, Long sessionId) {
        WebClient.create(gatewayUrl).post().uri("/session-service/api/sessions/" + sessionId + "/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve().toBodilessEntity().block();
    }

    public void finish(String token, Long sessionId, List<Long> absentIds) {
        WebClient.create(gatewayUrl).post().uri("/session-service/api/sessions/" + sessionId + "/finish")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(Map.of("absentPlayerIds", absentIds))
                .retrieve().toBodilessEntity().block();
    }

    public Long getMyId(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            String search = "\"userId\":";
            int start = payload.indexOf(search) + search.length();
            int end = payload.indexOf(",", start);
            return Long.parseLong(payload.substring(start, end).trim());
        } catch (Exception e) { return 0L; }
    }
}