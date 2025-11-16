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
    @Column(name = "consultation_id", nullable = false)
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

    @Column(name = "video_url", nullable = true, length = 255)
    private String videoUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    public enum Type {
        ONE, GROUP, BREAK
    }
    
    public enum Status {
        OPEN, CLOSE, CANCELLED
    }

    public void cancel() {
        this.status = Status.CANCELLED;
    }

    public void updateDetails(LocalDateTime startAt, LocalDateTime endAt, Integer capacity) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.capacity = capacity;
    }

    // Consultation 엔티티에 추가
    public void generateSessionId() {
        if (this.id == null) {
            throw new IllegalStateException("ID가 생성되지 않은 상태에서는 세션 ID를 생성할 수 없습니다.");
        }
        this.sessionId = "consultation-" + this.id;
    }

    /**
     * 영상 URL을 업데이트합니다.
     */
    public void updateVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}