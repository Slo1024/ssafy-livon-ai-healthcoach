package com.s406.livon.domain.ai.gcp.service;


import com.s406.livon.domain.ai.gcp.service.ConsultationVideoService;
import io.livekit.server.EgressServiceClient;
import java.io.IOException;
import java.util.List; // [수정] java.util.List 사용
import livekit.LivekitEgress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

@Slf4j
@Service
@RequiredArgsConstructor
public class EgressPollingService {

    private final EgressServiceClient egressServiceClient;
    private final ConsultationVideoService consultationVideoService;

    // --- 폴링 설정 ---
    private static final long POLLING_INTERVAL_MS = 10000L; // 10초 간격
    private static final int MAX_POLLING_ATTEMPTS = 40;
    // -----------------

    @Async
    public void pollForRecordingCompletion(String egressId, Long consultationId, String sessionId) {
        log.info("[POLLING] Egress 완료 폴링 시작. Egress ID: {}, Session ID: {}", egressId, sessionId);

        for (int i = 0; i < MAX_POLLING_ATTEMPTS; i++) {
            try {
                // 1. [수정] sessionId(방 이름)로 listEgress 호출
                Call<List<LivekitEgress.EgressInfo>> listCall = egressServiceClient.listEgress(sessionId);

                // 2. 헬퍼 메소드로 'Call' 실행
                List<LivekitEgress.EgressInfo> infoList = executeList(listCall);

                // 3. [수정] 반환된 '리스트'에서 우리가 찾는 Egress ID를 '필터링'
                LivekitEgress.EgressInfo info = infoList.stream()
                        .filter(e -> e.getEgressId().equals(egressId))
                        .findFirst()
                        .orElse(null); // 아직 리스트에 없으면 null

                if (info == null) {
                    log.warn("[POLLING] Session '{}' 목록에서 Egress ID {}를 찾을 수 없음. 재시도...", sessionId, egressId);
                    Thread.sleep(POLLING_INTERVAL_MS);
                    continue;
                }


                // 4. 녹화 완료 상태 확인
                if (info.getStatus() == LivekitEgress.EgressStatus.EGRESS_COMPLETE) {
                    log.info("[POLLING] Egress 완료 (EGRESS_COMPLETE). Egress ID: {}", egressId);

                    String location = info.getFileResultsList().stream()
                            .map(LivekitEgress.FileInfo::getLocation)
                            .filter(loc -> loc != null && !loc.isBlank())
                            .findFirst()
                            .orElse(null);

                    if (location != null) {
                        log.info("[POLLING] MinIO URL 발견: {}. 요약 프로세스 시작...", location);
                        consultationVideoService.uploadAndSummarizeFromUrl(consultationId, location, null);
                        return; // ★ 폴링 성공 및 종료
                    } else {
                        // ... (생략)
                    }
                }

                // 5. 녹화 실패 상태 확인
                if (info.getStatus() == LivekitEgress.EgressStatus.EGRESS_FAILED) {
                    // ... (생략)
                }

                // 6. 아직 진행 중...
                log.debug("[POLLING] 진행 중... Egress ID: {} (Status: {}, 시도: {}/{})",
                        egressId, info.getStatus(), i + 1, MAX_POLLING_ATTEMPTS);

                Thread.sleep(POLLING_INTERVAL_MS);

            } catch (Exception e) {
                // ... (생략)
            }
        }

        log.warn("[POLLING] Egress 폴링 타임아웃 ({}회 시도). Egress ID: {}", MAX_POLLING_ATTEMPTS, egressId);
    }

    /**
     * [수정된 헬퍼 메소드]
     * LiveKit Egress 'List' API Call (Call<List<EgressInfo>>)을 실행합니다.
     */
    private List<LivekitEgress.EgressInfo> executeList(Call<List<LivekitEgress.EgressInfo>> call) {
        try {
            Response<List<LivekitEgress.EgressInfo>> response = call.execute();
            if (!response.isSuccessful() || response.body() == null) {
                log.error("LiveKit egress list call failed. code={}, error={}", response.code(), response.errorBody());
                throw new RuntimeException("LiveKit egress list call failed: " + response.code());
            }
            return response.body();
        } catch (IOException e) {
            log.error("LiveKit egress list call error", e);
            throw new RuntimeException(e);
        }
    }
}