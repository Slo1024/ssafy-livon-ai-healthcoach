package com.s406.livon.domain.openvidu.service;

import com.s406.livon.domain.ai.gcp.service.EgressPollingService;
import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.repository.ParticipantRepository;
import com.s406.livon.domain.consultation.repository.ConsultationRepository;
import com.s406.livon.domain.openvidu.dto.request.RecordingStartRequestDto;
import com.s406.livon.domain.openvidu.dto.request.RecordingStopRequestDto;
import com.s406.livon.domain.openvidu.dto.response.RecordingFileResultDto;
import com.s406.livon.domain.openvidu.dto.response.RecordingStartResponseDto;
import com.s406.livon.domain.openvidu.dto.response.RecordingStopResponseDto;
import com.s406.livon.global.config.properties.LivekitEgressProperties;
import com.s406.livon.global.config.properties.MinioProperties;
import com.s406.livon.global.error.handler.CoachHandler;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import io.livekit.server.EgressServiceClient;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import livekit.LivekitEgress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import retrofit2.Call;
import retrofit2.Response;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecordingService {

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ConsultationRepository consultationRepository;
    private final ParticipantRepository participantRepository;
    private final EgressServiceClient egressServiceClient;
    private final LivekitEgressProperties egressProperties;
    private final MinioProperties minioProperties;

    private final EgressPollingService egressPollingService;

    @Transactional
    public RecordingStartResponseDto startRecording(UUID userId, RecordingStartRequestDto request) {
        Consultation consultation = consultationRepository.findById(request.consultationId())
                .orElseThrow(() -> new CoachHandler(ErrorStatus.CONSULTATION_NOT_FOUND));

        validateAccess(userId, consultation);

        String layout = StringUtils.hasText(request.layout()) ? request.layout() : egressProperties.getLayout();
        String filePath = buildFilePath(consultation, request.filename());

        LivekitEgress.S3Upload s3Upload = buildS3Upload();

        LivekitEgress.EncodedFileOutput fileOutput = LivekitEgress.EncodedFileOutput.newBuilder()
                .setFileType(LivekitEgress.EncodedFileType.MP4)
                .setFilepath(filePath)
                .setS3(s3Upload)
                .build();

        LivekitEgress.EgressInfo info = execute(
                egressServiceClient.startRoomCompositeEgress(
                        consultation.getSessionId(),
                        fileOutput,
                        layout,
                        null,
                        null,
                        false,
                        false
                )
        );

        return RecordingStartResponseDto.builder()
                .egressId(info.getEgressId())
                .consultationId(consultation.getId())
                .sessionId(consultation.getSessionId())
                .status(info.getStatus().name())
                .filePath(filePath)
                .startedAt(info.getStartedAt())
                .build();
    }

    @Transactional
    public RecordingStopResponseDto stopRecording(UUID userId, RecordingStopRequestDto request) {
        Consultation consultation = consultationRepository.findById(request.consultationId())
                .orElseThrow(() -> new CoachHandler(ErrorStatus.CONSULTATION_NOT_FOUND));

        validateAccess(userId, consultation);

        LivekitEgress.EgressInfo info = execute(egressServiceClient.stopEgress(request.egressId()));

        egressPollingService.pollForRecordingCompletion(info.getEgressId(), consultation.getId(), consultation.getSessionId());

        log.info("녹화 중지 요청 수신 및 백그라운드 폴링 시작. Egress ID: {}", info.getEgressId());

        List<RecordingFileResultDto> files = info.getFileResultsList().stream()
                .map(file -> RecordingFileResultDto.builder()
                        .filename(file.getFilename())
                        .location(file.getLocation())
                        .duration(file.getDuration())
                        .size(file.getSize())
                        .build())
                .collect(Collectors.toList());

        return RecordingStopResponseDto.builder()
                .egressId(info.getEgressId())
                .consultationId(consultation.getId())
                .status(info.getStatus().name())
                .startedAt(info.getStartedAt())
                .endedAt(info.getEndedAt())
                .files(files)
                .build();
    }

    private void validateAccess(UUID userId, Consultation consultation) {
        if (consultation.getCoach().getId().equals(userId)) {
            return;
        }
        boolean isParticipant = participantRepository.existsByUserIdAndConsultationId(userId, consultation.getId());
        if (!isParticipant) {
            throw new CoachHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
    }

    private String buildFilePath(Consultation consultation, String filename) {
        String prefix = egressProperties.getFilePrefix();
        String baseName = StringUtils.hasText(filename)
                ? filename
                : consultation.getSessionId() + "_" + FILE_TS.format(LocalDateTime.now());
        if (!baseName.endsWith(".mp4")) {
            baseName = baseName + ".mp4";
        }
        return String.format("%s/%s/%s", prefix, consultation.getSessionId(), baseName);
    }

    private LivekitEgress.S3Upload buildS3Upload() {
        LivekitEgress.S3Upload.Builder builder = LivekitEgress.S3Upload.newBuilder()
                .setAccessKey(minioProperties.getAccessKey())
                .setSecret(minioProperties.getSecretKey())
                .setEndpoint(minioProperties.getEndpoint())
                .setBucket(minioProperties.getBucket())
                .setForcePathStyle(minioProperties.isForcePathStyle());

        if (StringUtils.hasText(minioProperties.getRegion())) {
            builder.setRegion(minioProperties.getRegion());
        }
        return builder.build();
    }

    private LivekitEgress.EgressInfo execute(Call<LivekitEgress.EgressInfo> call) {
        try {
            Response<LivekitEgress.EgressInfo> response = call.execute();
            if (!response.isSuccessful() || response.body() == null) {
                log.error("LiveKit egress call failed. code={}, error={}", response.code(), response.errorBody());
                throw new CoachHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
            }
            return response.body();
        } catch (IOException e) {
            log.error("LiveKit egress call error", e);
            throw new CoachHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }
}
