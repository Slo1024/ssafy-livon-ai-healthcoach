package com.s406.livon.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "coach_certificates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CoachCertificates {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "coach_certificates_id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID coachCertificatesId;

    @Column(nullable = false)
    private String certificatesName;
}