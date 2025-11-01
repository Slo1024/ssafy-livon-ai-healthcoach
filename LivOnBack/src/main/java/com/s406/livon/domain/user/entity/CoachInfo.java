package com.s406.livon.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CoachInfo {
    @Id
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String job;

    @Column
    private String introduce;

    // 단방향 1:N + 자식 테이블 FK
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(
            name = "coach_info_id",
            referencedColumnName = "user_id",
            nullable = false
    )
    @Builder.Default
    private List<CoachCertificates> coachCertificatesList = new ArrayList<>();
}
