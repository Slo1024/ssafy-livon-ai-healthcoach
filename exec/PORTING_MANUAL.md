# 리브온 프로젝트 포팅 메뉴얼

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
| **OpenVidu V3** | OpenVidu | 화상 회의, 화면 공유, 스트리밍 | WebSocket (시그널링) WebRTC (미디어 전송) |
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
 ![rn_image_picker_lib_temp_276b324b-dbba-4cdc-9faa-9af8354ee1d6](/uploads/30db749ac29df906d5c2021b12f16628/rn_image_picker_lib_temp_276b324b-dbba-4cdc-9faa-9af8354ee1d6.jpg)
 - 상담 종류 선택 화면(그룹 상담 선택)
 - 클래스 리스트 화면(예약할 클래스 선택)
 - 클래스 상세 정보 화면(예약 하기 선택)
 - 현재 예약 화면(기능 선택 -> 채팅 아이콘 선택)
 - 스트리밍 화면
 - 채팅창
2. 웹
 - 회원 별 정보 상세보기