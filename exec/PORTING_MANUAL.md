# 리브온 프로젝트 포팅 메뉴얼

---

## 개요
리브온은 AI·휴먼코치 기반 라이브 스트리밍 실시간 상담 및 코칭 서비스 입니다.
 - BackEnd : Java, Spring Boot
 - FrontEnd: Kotlin, React
 - AI : GPT, VertexAI
 - Infra: EC2, Jenkins, Gitlab, Docker, Nginx, MM, openvidu v3(caddy, ingress, outgress, meet, dashboard, operater, mongoDB, redis, minio), GCP(gcs, vertex AI, notebook api)

 ---

 ### Gitlab 소스 클론 이후 빌드 및 배포할 수 있도록 정리한 문서

---

### 프로젝트에서 사용하는 외부 서비스 정보를 정리한 문서
| **서비스명** | **제공사** | **주요 역할 (Purpose)** | **통신 방식** |
| --- | --- | --- | --- |
| **OpenVidu V3** | OpenVidu | 화상 회의, 화면 공유, 스트리밍 | WebSocket (시그널링)
WebRTC (미디어 전송) |
| **Vertex AI** | Google Cloud | 1:1 상담 영상 기반 요약 정보 제공 | REST API / gRPC |
| GPT | OpenAI | 사용자의 생체 정보 AI 요약 정보 제공 | REST API (HTTPS) |
| mongodb | MongoDB Inc. | 비정형 데이터 저장 | MongoDB Wire Protocol (TLS) |
| S3 | Amazon | 사용자 프로필 사진 저장,  | REST API (HTTPS), SDK 호출 |
| MinIO | MinIO, Inc. | 상담 영상 저장 | S3 API(HTTPS), MinIO SDK |
| SMTP | email | 인증 메일 | SMTP / SMTPS (TLS) |
| redis | Redis Inc. | 토큰 저장, 세션 캐싱, 일시적 데이터 관리 | RESP 프로토콜 (TCP) |

---
### 시연 시나리오
1. 앱
 - 메인 화면(예약 하기 선택)
 ![rn_image_picker_lib_temp_276b324b-dbba-4cdc-9faa-9af8354ee1d6](/uploads/8f590966128f1dd280a5740ccac3a293/rn_image_picker_lib_temp_276b324b-dbba-4cdc-9faa-9af8354ee1d6.jpg)
 - 상담 종류 선택 화면(그룹 상담 선택)
 ![rn_image_picker_lib_temp_be9a1e4d-1af1-4ff5-905b-1edbf5f6acaa](/uploads/160919960e8ffc6de7a657c9f305d98d/rn_image_picker_lib_temp_be9a1e4d-1af1-4ff5-905b-1edbf5f6acaa.jpg)
 - 클래스 리스트 화면(예약할 클래스 선택)
  ![rn_image_picker_lib_temp_95d10a44-9a49-42a9-9905-79583888ff19](/uploads/c8a2a9086a3d86f43fc0ee609b13b252/rn_image_picker_lib_temp_95d10a44-9a49-42a9-9905-79583888ff19.jpg)
 - 클래스 상세 정보 화면(예약 하기 선택)
 ![rn_image_picker_lib_temp_cd4ffb6a-36a5-4b27-b5e5-36821cb76af4](/uploads/01b5109357b72aa2bed97939aa1cec9b/rn_image_picker_lib_temp_cd4ffb6a-36a5-4b27-b5e5-36821cb76af4.jpg)
 - 현재 예약 화면(기능 선택 -> 채팅 아이콘 선택)
 ![rn_image_picker_lib_temp_95d10a44-9a49-42a9-9905-79583888ff19](/uploads/87604256346c7b6bb7a48ffb5154829c/rn_image_picker_lib_temp_95d10a44-9a49-42a9-9905-79583888ff19.jpg)
 - 스트리밍 화면
 ![rn_image_picker_lib_temp_f79ae315-86a1-4cfb-82fd-669d108ca52a](/uploads/d6d908f90f549e62093337f555b54ce4/rn_image_picker_lib_temp_f79ae315-86a1-4cfb-82fd-669d108ca52a.jpg)
 - 채팅창
 ![rn_image_picker_lib_temp_6bf586dd-9e3d-4fab-b80d-b7771d6b24f8](/uploads/4502f9b5a05bac48cc69682a715a4f3a/rn_image_picker_lib_temp_6bf586dd-9e3d-4fab-b80d-b7771d6b24f8.jpg)
2. 웹
 - 회원 별 정보 상세보기
 ![스크린샷_2025-11-18_222442__1_](/uploads/5c94f1cc3925a6b21676d1ff1e65eca7/스크린샷_2025-11-18_222442__1_.png){width=2160 height=1092}