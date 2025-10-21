package com.s406.livon.domain.user.dto.response;

import com.s406.livon.domain.user.entity.Organizations;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationsResponseDto {
    private String name;

    public static OrganizationsResponseDto toDto(Organizations organizations){
        return OrganizationsResponseDto.builder()
                .name(organizations.getName())
                .build();
    }
}


