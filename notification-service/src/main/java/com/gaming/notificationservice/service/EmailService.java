package com.gaming.notificationservice.service;

import com.gaming.notificationservice.model.NotificationLog;
import com.gaming.notificationservice.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    public void sendEmail(String to, String type, String rawContent) {
        String subject = "Gaming System Notification";
        String body = "";

        switch (type) {
            case "ACTIVATION":
                subject = "Account Activation";
                body = "Welcome! Click here to activate your account: "
                        + "http://localhost:8080/user-service/api/users/activate/" + rawContent;
                break;
            case "INVITATION":
                subject = "Game Invitation";
                body = "You are invited to join session: " + rawContent
                        + "<br> <a href='http://localhost:8080/session-service/api/sessions'>View Sessions</a>";
                break;
            case "CANCELLATION":
                subject = "Session Cancelled";
                body = "Important: " + rawContent;
                break;
            default:
                body = rawContent;
                break;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom("system@gaming.com");

            mailSender.send(message);
            System.out.println("EMAIL SENT to " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        NotificationLog log = NotificationLog.builder()
                .recipientEmail(to)
                .type(type)
                .content(body)
                .sentAt(LocalDateTime.now())
                .build();

        notificationRepository.save(log);
    }
}