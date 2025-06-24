package com.example.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name")
    private String user;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @Column(name = "ip")
    private String ip;

    @Column(name = "agent")
    private String agent;

    @Column(name = "uri")
    private String uri;

    @Column(name = "response_code")
    private String responseCode;

    @Column(name = "response_message")
    private String responseMessage;
}