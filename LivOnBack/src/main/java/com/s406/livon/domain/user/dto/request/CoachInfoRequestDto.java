package com.s406.livon.domain.user.dto.request;

import com.s406.livon.domain.user.entity.CoachCertificates;
import com.s406.livon.domain.user.entity.CoachInfo;
import com.s406.livon.domain.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CoachInfoRequestDto {
    private String job;
    private String introduce;
    private List<String> certificates;

    public CoachInfo toEntity(User user) {
        // 1) 부모 생성 (단방향 1:N 이므로 리스트는 기본값으로 비어 있음)
        CoachInfo coachInfo = CoachInfo.builder()
                .id(user.getId())          // @MapsId 이므로 user의 PK 사용
                .user(user)
                .job(job)
                .introduce(introduce)
                .build();                  // coachCertificatesList 는 [] 로 초기화됨

        // 2) 자격증 문자열 리스트 -> 자격증 엔티티 리스트
        if (certificates != null && !certificates.isEmpty()) {
            coachInfo.getCoachCertificatesList().addAll(
                    certificates.stream()
                            .map(name -> CoachCertificates.builder()
                                    .certificatesName(name)
                                    .build())
                            .collect(Collectors.toList())
            );
        }

        return coachInfo;
    }
}
