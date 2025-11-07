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
@Table(name = "individualConsultation")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualConsultation {

    @Id
    @Column(name = "id")
    private Long id;  // consultation.id와 동일한 값 사용

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // id 값을 consultation.id로부터 가져옴
    @JoinColumn(name = "id")
    private Consultation consultation;

    @Column(columnDefinition = "TEXT")
    private String preQnA; // 사전 QnA

    @Column(columnDefinition = "TEXT")
    private String aiSummary; // AI 요약
}