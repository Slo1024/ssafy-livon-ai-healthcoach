package com.s406.livon.domain.user.dto.request;

import com.s406.livon.domain.user.entity.CoachCertificates;
import com.s406.livon.domain.user.entity.CoachInfo;
import com.s406.livon.domain.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
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
        // 자격증 문자열 리스트 -> 자격증 엔티티 리스트 변환
        List<CoachCertificates> certificatesList = new ArrayList<>();
        if (certificates != null && !certificates.isEmpty()) {
            certificatesList = certificates.stream()
                    .map(name -> CoachCertificates.builder()
                            .certificatesName(name)
                            .build())
                    .collect(Collectors.toList());
        }

        // CoachInfo 생성 (한 번에 모든 필드 설정)
        return CoachInfo.builder()
                .user(user)
                .job(job)
                .introduce(introduce)
                .coachCertificatesList(certificatesList)
                .build();
    }
}