package com.s406.livon.domain.user.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

public class CoachInfo {
    @Id
    @Column(name = "user_id", columnDefinition = "BINARY(16)") // DB 컬럼 설정 (User와 동일하게)
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String job;
    @Column
    private String introduce;
    @Column
    private String professional;

    @OneToMany(mappedBy = "coachInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoachCertificates> coachCertificatesList;


}
