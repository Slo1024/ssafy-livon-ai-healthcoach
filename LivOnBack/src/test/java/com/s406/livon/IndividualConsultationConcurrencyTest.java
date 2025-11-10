package com.s406.livon;

import com.s406.livon.domain.coach.dto.request.IndivualConsultationReservationRequestDto;
import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.repository.ConsultationReservationRepository;
import com.s406.livon.domain.coach.repository.IndividualConsultationRepository;
import com.s406.livon.domain.coach.repository.ParticipantRepository;
import com.s406.livon.domain.coach.service.IndividualConsultationService;
import com.s406.livon.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Gender;
import com.s406.livon.domain.user.enums.Role;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.error.handler.CoachHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class IndividualConsultationConcurrencyTest {

    @Autowired
    private IndividualConsultationService individualConsultationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsultationReservationRepository consultationRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private GoodsChatRoomRepository goodsChatRoomRepository;

    @Autowired
    private IndividualConsultationRepository individualConsultationRepository;

    private User coach;
    private List<User> users;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화 - FK 제약조건 순서에 맞게 삭제

        // 1. goods_chat_room 먼저 삭제 (consultation을 참조하고 있으므로)
        if (goodsChatRoomRepository != null) {
            goodsChatRoomRepository.deleteAll();
        }

        // 2. individualConsultation 삭제 (consultation과 1:1 관계)
        individualConsultationRepository.deleteAll();

        // 3. participants 삭제
        participantRepository.deleteAll();

        // 4. consultation 삭제
        consultationRepository.deleteAll();

        // 5. user 삭제
        userRepository.deleteAll();

        // 코치 생성
        List<Role> coachrole = new ArrayList();
        coachrole.add(Role.COACH);
        coach = User.builder()
                .email("coach@test.com")
                .nickname("테스트코치")
                .password("password")
                .gender(Gender.남자)
                .birthdate(LocalDate.of(1990, 1, 1))
                .roles(coachrole)
                .build();
        coach = userRepository.save(coach);

        // 일반 사용자 10명 생성
        List<Role> memberrole = new ArrayList();
        memberrole.add(Role.COACH);
        users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = User.builder()
                    .email("user" + i + "@test.com")
                    .nickname("사용자" + i)
                    .password("password")
                    .gender(Gender.남자)
                    .birthdate(LocalDate.of(1995, 1, 1))
                    .roles(memberrole)
                    .build();
            users.add(userRepository.save(user));
        }

        // 상담 시간 설정
        startAt = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
        endAt = startAt.plusHours(1);
    }

    @Test
    @DisplayName("동시에 10명이 같은 시간대 1:1 상담 예약 시 1명만 성공해야 한다")
    void concurrentReservationTest() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            int index = i;
            executorService.submit(() -> {
                try {
                    IndivualConsultationReservationRequestDto requestDto =
                            new IndivualConsultationReservationRequestDto(
                                    coach.getId(),
                                    startAt,
                                    endAt,
                                    "사전 질문 " + index
                            );

                    individualConsultationService.reserveConsultation(
                            users.get(index).getId(),
                            requestDto
                    );
                    successCount.incrementAndGet();
                    System.out.println("✅ 성공: 사용자" + index);

                } catch (CoachHandler e) {
                    failCount.incrementAndGet();
                    // ✅ 안전한 메시지 출력
                    String errorMessage = e.getCode() != null
                            ? e.getCode().getMessage()
                            : e.getMessage();
                    System.out.println("❌ 실패(CoachHandler): 사용자" + index + " - " + errorMessage);

                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("❌ 실패(Exception): 사용자" + index + " - " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    e.printStackTrace(); // 디버깅용

                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        System.out.println("\n=== 테스트 결과 ===");
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());

        // 정확히 1명만 성공해야 함
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);

        // DB에 1개의 상담만 생성되었는지 확인
        List<Consultation> consultations = consultationRepository.findAll();
        assertThat(consultations).hasSize(1);

        // DB에 1개의 참가자만 등록되었는지 확인
        long participantCount = participantRepository.count();
        assertThat(participantCount).isEqualTo(1);
    }

    @Test
    @DisplayName("다른 시간대 예약은 동시에 진행 가능해야 한다")
    void differentTimeSlotReservationTest() throws InterruptedException {
        // given
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        // when - 각각 다른 시간대로 예약
        for (int i = 0; i < threadCount; i++) {
            int hourOffset = i;
            executorService.submit(() -> {
                try {
                    LocalDateTime slotStart = startAt.plusHours(hourOffset);
                    LocalDateTime slotEnd = slotStart.plusHours(1);

                    IndivualConsultationReservationRequestDto requestDto =
                            new IndivualConsultationReservationRequestDto(
                                    coach.getId(),
                                    slotStart,
                                    slotEnd,
                                    "사전 질문 " + hourOffset
                            );

                    individualConsultationService.reserveConsultation(
                            users.get(hourOffset).getId(),
                            requestDto
                    );
                    successCount.incrementAndGet();
                    System.out.println("✅ 성공: " + hourOffset + "시간 후 예약");

                } catch (Exception e) {
                    System.out.println("❌ 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        System.out.println("\n=== 다른 시간대 예약 결과 ===");
        System.out.println("성공 횟수: " + successCount.get());

        // 모두 성공해야 함
        assertThat(successCount.get()).isEqualTo(threadCount);

        // DB에 5개의 상담이 생성되었는지 확인
        List<Consultation> consultations = consultationRepository.findAll();
        assertThat(consultations).hasSize(threadCount);
    }

    @Test
    @DisplayName("같은 시간대, 다른 코치 예약은 동시에 진행 가능해야 한다")
    void differentCoachReservationTest() throws InterruptedException {
        // given
        // 추가 코치 생성
        List role = new ArrayList<>();
        role.add(Role.COACH);
        User coach2 = User.builder()
                .email("coach2@test.com")
                .nickname("테스트코치2")
                .password("password")
                .gender(Gender.여자)
                .birthdate(LocalDateTime.of(1990, 1, 1, 0, 0).toLocalDate())
                .roles(role)
                .build();
        coach2 = userRepository.save(coach2);

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        // when - 같은 시간, 다른 코치에게 예약
        List<User> coaches = List.of(coach, coach2);
        for (int i = 0; i < threadCount; i++) {
            int index = i;
            executorService.submit(() -> {
                try {
                    IndivualConsultationReservationRequestDto requestDto =
                            new IndivualConsultationReservationRequestDto(
                                    coaches.get(index).getId(),
                                    startAt,
                                    endAt,
                                    "사전 질문 " + index
                            );

                    individualConsultationService.reserveConsultation(
                            users.get(index).getId(),
                            requestDto
                    );
                    successCount.incrementAndGet();
                    System.out.println("✅ 성공: 코치" + (index + 1) + " 예약");

                } catch (Exception e) {
                    System.out.println("❌ 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        System.out.println("\n=== 다른 코치 예약 결과 ===");
        System.out.println("성공 횟수: " + successCount.get());

        // 모두 성공해야 함 (다른 코치니까)
        assertThat(successCount.get()).isEqualTo(threadCount);

        // DB에 2개의 상담이 생성되었는지 확인
        List<Consultation> consultations = consultationRepository.findAll();
        assertThat(consultations).hasSize(threadCount);
    }
}