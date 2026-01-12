package com.gaming.notificationservice.repository;

import com.gaming.notificationservice.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByRecipientEmail(String email);
}