# GCP Vertex AI 영상 요약 기능

GCP Vertex AI (Gemini)를 사용하여 1대1 코칭 영상을 자동으로 분석하고 요약하는 기능입니다.

## 📁 패키지 구조

```
com.s406.livon.domain.ai.gcp/
├── config/
│   └── GcpConfig.java                          # GCP 설정 및 Bean 등록
├── controller/
│   ├── ConsultationVideoController.java        # 통합 API (업로드 + 요약)
│   ├── GcpVideoSummaryController.java          # AI 요약 API
│   └── GcsStorageController.java               # 스토리지 API
├── dto/
│   ├── request/
│   │   ├── VideoSummaryRequestDto.java         # 요약 요청 DTO
│   │   └── VideoUploadRequestDto.java          # 업로드 요청 DTO
│   └── response/
│       ├── VideoSummaryResponseDto.java        # 요약 응답 DTO
│       └── VideoUploadResponseDto.java         # 업로드 응답 DTO
└── service/
    ├── ConsultationVideoService.java           # 통합 서비스
    ├── GcpVideoSummaryService.java             # AI 요약 서비스
    └── GcsStorageService.java                  # 스토리지 서비스
```

## 🎯 주요 기능

### 1. 영상 업로드
- MultipartFile을 GCS(Google Cloud Storage)에 업로드
- 자동으로 경로 생성 (`consultations/{consultationId}/{timestamp}_{uuid}.mp4`)
- GCS URI 및 공개 URL 반환

### 2. AI 요약 생성
- Gemini 2.5 Flash Lite 모델 사용
- 영상 분석 및 상세 요약 생성
- 사전 QnA 정보 활용
- DB에 자동 저장

### 3. 통합 처리
- 영상 업로드부터 AI 요약까지 한 번에 처리
- 요약 재생성 기능
- 영상 및 요약 삭제 기능

## 📡 API 엔드포인트

### 통합 API (`/consultations/video`)

#### 1. 영상 업로드 및 요약 생성
```http
POST /api/v1/consultations/video/{consultationId}/upload-and-summarize
Content-Type: multipart/form-data

Parameters:
- file: 영상 파일 (required)
- preQnA: 사전 QnA (optional)
```

**Response:**
```json
{
  "isSuccess": true,
  "code": "COMMON201",
  "message": "생성 성공",
  "result": {
    "consultationId": 123,
    "summary": "코칭 세션 요약 내용..."
  }
}
```

#### 2. 요약 재생성
```http
POST /api/v1/consultations/video/{consultationId}/regenerate-summary
Content-Type: application/json

Parameters:
- preQnA: 사전 QnA (optional)
```

#### 3. 영상 삭제
```http
DELETE /api/v1/consultations/video/{consultationId}
```

### AI 요약 API (`/gcp/video-summary`)

#### 1. 요약 생성
```http
POST /api/v1/gcp/video-summary
Content-Type: application/json

{
  "consultationId": 123,
  "videoUrl": "gs://bucket/video.mp4",
  "preQnA": "사전 질문 내용"
}
```

#### 2. 요약 조회
```http
GET /api/v1/gcp/video-summary/{consultationId}
```

### 스토리지 API (`/gcp/storage`)

#### 1. 영상 업로드
```http
POST /api/v1/gcp/storage/upload/{consultationId}
Content-Type: multipart/form-data

Parameters:
- file: 영상 파일
```

**Response:**
```json
{
  "isSuccess": true,
  "code": "COMMON201",
  "message": "생성 성공",
  "result": {
    "consultationId": 123,
    "gcsUri": "gs://livon-video-uploads/consultations/123/...",
    "publicUrl": "https://storage.googleapis.com/livon-video-uploads/..."
  }
}
```

#### 2. 영상 삭제
```http
DELETE /api/v1/gcp/storage?gcsUri=gs://bucket/video.mp4
```

#### 3. 영상 존재 확인
```http
GET /api/v1/gcp/storage/exists?gcsUri=gs://bucket/video.mp4
```

## ⚙️ 설정

### application.yml
```yaml
gcp:
  project:
    id: livon                           # GCP 프로젝트 ID
  credentials:
    location: ${GCP_KEY_FILE}           # GCP 인증 키 파일 경로
  storage:
    bucket:
      name: livon-video-uploads         # GCS 버킷 이름
  vertex:
    ai:
      location: us-central1             # Vertex AI 리전
      model:
        name: gemini-2.5-flash-lite     # 사용할 모델
```

### 환경 변수
```bash
GCP_KEY_FILE=/path/to/service-account-key.json
```

## 📝 AI 요약 내용

생성되는 요약에는 다음 정보가 포함됩니다:

1. **코칭 세션의 주요 주제와 목표**
   - 세션의 전체적인 방향성
   - 클라이언트가 달성하고자 하는 목표

2. **논의된 핵심 내용 및 문제점**
   - 주요 토픽과 이슈
   - 클라이언트가 직면한 문제

3. **코치가 제시한 조언 및 솔루션**
   - 구체적인 해결 방안
   - 실천 가능한 조언

4. **클라이언트의 반응 및 인사이트**
   - 세션 중 얻은 깨달음
   - 클라이언트의 변화

5. **다음 세션을 위한 액션 아이템**
   - 실행할 과제
   - 다음 세션까지의 목표

## 🔧 사용 예시

### Java/Spring Service에서 사용

```java
@Service
@RequiredArgsConstructor
public class YourService {
    
    private final ConsultationVideoService consultationVideoService;
    
    public void processVideo(Long consultationId, MultipartFile videoFile) 
            throws IOException {
        // 영상 업로드 및 요약 생성
        VideoSummaryResponseDto result = consultationVideoService
                .uploadAndSummarize(consultationId, videoFile, "사전 QnA 내용");
        
        System.out.println("요약: " + result.getSummary());
    }
}
```

### cURL 예시

```bash
# 영상 업로드 및 요약 생성
curl -X POST "http://localhost:8081/api/v1/consultations/video/123/upload-and-summarize" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@coaching_session.mp4" \
  -F "preQnA=클라이언트가 운동 목표에 대해 질문했습니다."

# 요약 조회
curl -X GET "http://localhost:8081/api/v1/gcp/video-summary/123"

# 요약 재생성
curl -X POST "http://localhost:8081/api/v1/consultations/video/123/regenerate-summary" \
  -H "Content-Type: application/json" \
  -d '{"preQnA": "추가 정보"}'

# 영상 삭제
curl -X DELETE "http://localhost:8081/api/v1/consultations/video/123"
```

## 🚨 에러 처리

프로젝트의 표준 에러 응답 구조를 따릅니다:

```json
{
  "isSuccess": false,
  "code": "GCP5002",
  "message": "Vertex AI 처리 중 오류가 발생했습니다.",
  "result": null
}
```

### 주요 에러 코드

| 코드 | 메시지 | 설명 |
|------|--------|------|
| GCP4000 | 업로드된 영상을 찾을 수 없습니다 | 영상이 존재하지 않음 |
| GCP4001 | 요약이 아직 생성되지 않았습니다 | 요약이 생성되지 않은 상태 |
| GCP5000 | 영상 업로드에 실패했습니다 | 업로드 중 오류 발생 |
| GCP5001 | 영상 삭제에 실패했습니다 | 삭제 중 오류 발생 |
| GCP5002 | Vertex AI 처리 중 오류가 발생했습니다 | AI 분석 중 오류 발생 |

## 📦 의존성

```gradle
// GCP Vertex AI
implementation platform('com.google.cloud:libraries-bom:26.43.0')
implementation 'com.google.cloud:google-cloud-vertexai'
implementation 'com.google.cloud:google-cloud-storage'
```

## 🔐 보안 고려사항

1. **GCP 인증 키 파일**
   - 절대 Git에 커밋하지 마세요
   - 환경 변수로 경로 관리
   - `.gitignore`에 추가

2. **GCS 버킷 권한**
   - 적절한 IAM 권한 설정
   - 공개 접근 제어

3. **영상 파일 크기**
   - 적절한 파일 크기 제한 설정
   - 타임아웃 설정 고려

## 🧪 테스트

```java
@SpringBootTest
class GcpVideoSummaryServiceTest {
    
    @Autowired
    private GcpVideoSummaryService service;
    
    @Test
    void 영상_요약_생성_테스트() {
        VideoSummaryRequestDto request = VideoSummaryRequestDto.builder()
                .consultationId(1L)
                .videoUrl("gs://test-bucket/test-video.mp4")
                .build();
        
        VideoSummaryResponseDto response = service.generateVideoSummary(request);
        
        assertNotNull(response.getSummary());
    }
}
```

## 📚 참고 자료

- [GCP Vertex AI Documentation](https://cloud.google.com/vertex-ai/docs)
- [Gemini API Guide](https://cloud.google.com/vertex-ai/docs/generative-ai/model-reference/gemini)
- [Google Cloud Storage Documentation](https://cloud.google.com/storage/docs)

