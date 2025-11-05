package com.s406.livon.global.util.log;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoggingService {

    private final LogEntryRepository logEntryRepository;

    /**
     * 로그를 DB에 비동기로 저장합니다.
     * * @Async: 이 메서드를 별도의 스레드에서 실행하여,
     * 메인 로직(API 응답 등)을 지연시키지 않습니다.
     * * @Transactional(propagation = Propagation.REQUIRES_NEW):
     * [매우 중요] 별도의 새 트랜잭션에서 로그를 저장합니다.
     * 만약 메인 비즈니스 로직(예: 회원가입)이 실패하고 롤백되더라도,
     * 이 "실패했다는 에러 로그"는 롤백되지 않고 DB에 커밋(저장)됩니다.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(String level, String loggerName, String message) {
        try {
            LogEntry logEntry = LogEntry.builder()
                    .level(level)
                    .loggerName(loggerName)
                    .message(message)
                    .build();
            logEntryRepository.save(logEntry);
        } catch (Exception e) {
            // (주의) 로그 저장에 실패했을 때, 여기서 다시 log.error()를 호출하면
            // 무한 루프에 빠질 수 있으므로, 최소한의 표준 출력만 합니다.
            System.err.println("Failed to save log to DB: " + e.getMessage());
        }
    }
}
