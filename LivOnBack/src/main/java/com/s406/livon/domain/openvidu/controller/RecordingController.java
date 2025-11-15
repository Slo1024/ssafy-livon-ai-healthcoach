package com.s406.livon.domain.openvidu.controller;

import com.s406.livon.domain.openvidu.dto.request.RecordingStartRequestDto;
import com.s406.livon.domain.openvidu.dto.request.RecordingStopRequestDto;
import com.s406.livon.domain.openvidu.dto.response.RecordingStartResponseDto;
import com.s406.livon.domain.openvidu.dto.response.RecordingStopResponseDto;
import com.s406.livon.domain.openvidu.service.RecordingService;
import com.s406.livon.global.security.jwt.JwtTokenProvider;
import com.s406.livon.global.web.response.ApiResponse;
import com.s406.livon.global.web.response.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/livekit/recordings")
@RequiredArgsConstructor
@Tag(name = "LiveKitRecording", description = "LiveKit 녹화 제어 API")
public class RecordingController {

    private final RecordingService recordingService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/start")
    @Operation(summary = "LiveKit 녹화 시작", description = "상담 세션에 대한 LiveKit Egress 녹화를 시작합니다.")
    public ResponseEntity<ApiResponse<RecordingStartResponseDto>> startRecording(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody RecordingStartRequestDto request
    ) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        RecordingStartResponseDto response = recordingService.startRecording(userId, request);
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.INSERT_SUCCESS, response));
    }

    @PostMapping("/stop")
    @Operation(summary = "LiveKit 녹화 종료", description = "진행 중인 LiveKit Egress 녹화를 종료합니다.")
    public ResponseEntity<ApiResponse<RecordingStopResponseDto>> stopRecording(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody RecordingStopRequestDto request
    ) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        RecordingStopResponseDto response = recordingService.stopRecording(userId, request);
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.SUCCESS, response));
    }
}
