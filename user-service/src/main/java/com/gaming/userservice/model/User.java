package com.gaming.userservice.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private boolean activated;
    private boolean blocked;

    private int totalRegisteredSessions;
    private int attendedSessions;
    private int abandonedSessions;

    private double attendancePercentage;

    private int successfulOrganizedSessions;

    @Enumerated(EnumType.STRING)
    private OrganizerTitle title;

    @PrePersist
    protected void onCreate() {
        if (this.role == Role.PLAYER) {
            this.attendancePercentage = 100.0;
            this.title = OrganizerTitle.ZASTAVNIK;
        }
    }
}
