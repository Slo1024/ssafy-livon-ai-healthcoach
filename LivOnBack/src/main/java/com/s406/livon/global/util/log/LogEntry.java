package com.s406.livon.global.util.log;

import com.s406.livon.global.util.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // CreatedDate 자동 생성을 위해 추가
public class LogEntry extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 10)
    private String level; // "INFO", "ERROR" 등

    @Column(nullable = false)
    private String loggerName; // 예: com.s406.livon.global.aop.LogAspect

    @Lob // LONGTEXT 타입
    @Column(nullable = false)
    private String message; // 로그 본문

    @Builder
    public LogEntry(String level, String loggerName, String message) {
        this.level = level;
        this.loggerName = loggerName;
        this.message = message;
    }
}
