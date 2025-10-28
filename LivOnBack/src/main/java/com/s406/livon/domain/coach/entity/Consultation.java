package com.s406.livon.domain.coach.entity;

import com.s406.livon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 상담 예약 엔티티
 */
@Entity
@Table(name = "consultation")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consultation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User coach;  // 상담을 진행하는 코치
    
    @Column(nullable = false)
    private Integer capacity;
    
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;
    
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;
    
    @Column(name = "session_id", nullable = false, length = 255)
    private String sessionId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    public enum Type {
        ONE, GROUP, BREAK
    }
    
    public enum Status {
        OPEN, CLOSE
    }
}