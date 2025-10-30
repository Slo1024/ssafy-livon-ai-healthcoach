package com.s406.livon.domain.coach.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 그룹(클래스) 상담 엔티티
 * Consultation과 1:1 관계
 */
@Entity
@Table(name = "groupConsultation")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupConsultation {

    @Id
    @Column(name = "id")
    private Long id;  // consultation.id와 동일한 값 사용

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // id 값을 consultation.id로부터 가져옴
    @JoinColumn(name = "id")
    private Consultation consultation;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    /**
     * 현재 참가 인원 수 조회
     * participants 테이블 COUNT 기반
     */
    @Transient  // DB에 저장하지 않음
    private Integer currentParticipants;

    // === 비즈니스 로직 ===

    /**
     * 참가 가능 여부 확인
     */
    public boolean canJoin() {
        if (consultation == null || currentParticipants == null) {
            return false;
        }
        return consultation.getStatus() == Consultation.Status.OPEN
                && currentParticipants < consultation.getCapacity();
    }

    /**
     * 남은 자리 수 조회
     */
    public int getAvailableSeats() {
        if (consultation == null || currentParticipants == null) {
            return 0;
        }
        return Math.max(0, consultation.getCapacity() - currentParticipants);
    }

    /**
     * 만석 여부 확인
     */
    public boolean isFull() {
        return getAvailableSeats() == 0;
    }

    public void updateDetails(String title, String description, String imageUrl) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }
}