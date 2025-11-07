package com.s406.livon.domain.coach.entity;

import com.s406.livon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상담 참가자 엔티티
 * 1:1 상담 및 그룹(클래스) 상담 예약 관리
 */
@Entity
@Table(
    name = "participants",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_participant_user_consultation",
            columnNames = {"user_id", "consultation_id"}
        )
    }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Participant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 예약한 사용자
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;  // 예약된 상담
    
    // === 비즈니스 로직 ===
    
    /**
     * 참가자 생성 (정적 팩토리 메서드)
     */
    public static Participant of(User user, Consultation consultation) {
        return Participant.builder()
                .user(user)
                .consultation(consultation)
                .build();
    }
}