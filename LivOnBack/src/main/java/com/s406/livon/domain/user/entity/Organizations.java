package com.s406.livon.domain.user.entity;


import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.checker.units.qual.C;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Organizations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Organizations_id", nullable = false)
    private Long id;

    @Column
    private String name;
}
