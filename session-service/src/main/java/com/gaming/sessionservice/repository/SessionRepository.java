package com.gaming.sessionservice.repository;

import com.gaming.sessionservice.model.GamingSession;
import com.gaming.sessionservice.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<GamingSession, Long>, JpaSpecificationExecutor<GamingSession> {
}