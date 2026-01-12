package com.gaming.sessionservice.repository;

import com.gaming.sessionservice.model.GamingSession;
import com.gaming.sessionservice.model.SessionStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class SessionSpecification {

    public static Specification<GamingSession> getSessions(
            String keyword,
            Long gameId,
            Boolean isClosed,
            Integer maxPlayers
    ) {
        return (root, query, criteriaBuilder) -> {
            Specification<GamingSession> spec = Specification.where(null);

            spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), SessionStatus.SCHEDULED));

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                spec = spec.and((r, q, cb) ->
                        cb.like(cb.lower(r.get("description")), pattern));
            }

            if (gameId != null) {
                spec = spec.and((r, q, cb) ->
                        cb.equal(r.get("game").get("id"), gameId));
            }

            if (isClosed != null) {
                spec = spec.and((r, q, cb) ->
                        cb.equal(r.get("isClosed"), isClosed));
            }

            if (maxPlayers != null) {
                spec = spec.and((r, q, cb) ->
                        cb.equal(r.get("maxPlayers"), maxPlayers));
            }

            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }
}