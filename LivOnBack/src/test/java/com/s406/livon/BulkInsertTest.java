package com.s406.livon;

import com.s406.livon.domain.user.entity.HealthSurvey;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Gender;
import com.s406.livon.domain.user.enums.Role;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.domain.user.repository.HealthSurveyRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BulkInsertTest {

    @Autowired UserRepository userRepository;
    @Autowired HealthSurveyRepository healthSurveyRepository;
    @Autowired EntityManager em;

    @Test
    @Commit            // 실제 커밋
    @Transactional     // 트랜잭션 유지
    void insert_10k_users_and_health_surveys() {
        final int TOTAL = 10_000;
        final int BATCH = 1000;

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= TOTAL; i++) {
            // 1) User 생성
            User user = User.builder()
                    .email("user" + i + "@example.com")
                    .password("{noop}pass" + i)
                    .nickname("user" + i)
                    .gender((i % 2 == 0) ? Gender.남자 : Gender.여자)
                    .birthdate(LocalDate.of(1980 + (i % 30), (i % 12) + 1, (i % 27) + 1))
                    .roles(List.of(Role.MEMBER))
                    .build();
            userRepository.save(user);

            // 2) HealthSurvey 생성
            HealthSurvey survey = HealthSurvey.builder()
                    .user(user)
                    .weight(50.0 + (i % 50))
                    .height(150.0 + (i % 50))
                    .steps(i % 20_000)
                    .sleepTime(5 + (i % 5))
                    .disease(null)
                    .sleepQuality((i % 3 == 0) ? "GOOD" : (i % 3 == 1) ? "NORMAL" : "BAD")
                    .medicationsInfo(null)
                    .painArea((i % 10 == 0) ? "NECK" : null)
                    .stressLevel((i % 4 == 0) ? "HIGH" : "LOW")
                    .smokingStatus((i % 7 == 0) ? "SMOKER" : "NON_SMOKER")
                    .avgSleepHours(6 + (i % 3))
                    .activityLevel((i % 2 == 0) ? "ACTIVE" : "SEDENTARY")
                    .caffeineIntakeLevel((i % 5 == 0) ? "HIGH" : "LOW")
                    .build();
            healthSurveyRepository.save(survey);

            // 3) 배치 처리 및 진행 로그
            if (i % BATCH == 0) {
                em.flush();
                em.clear();

                long elapsed = System.currentTimeMillis() - startTime;
                double percent = (i * 100.0 / TOTAL);
                System.out.printf("✅ %d/%d (%.1f%%) inserted | %.2f sec elapsed%n",
                        i, TOTAL, percent, elapsed / 1000.0);
            }
        }

        em.flush();
        em.clear();

        long totalElapsed = System.currentTimeMillis() - startTime;
        System.out.printf("🎉 전체 %d건 삽입 완료! 총 소요시간: %.2f초%n",
                TOTAL, totalElapsed / 1000.0);
    }
}
