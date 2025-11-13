// 클래스(그룹 상담) 관련 API 함수들
import axios from "axios";
import { CONFIG } from "../constants/config";
import { ApiResponse } from "./authApi";

const API_BASE_URL =
  CONFIG.API_BASE_URL ||
  process.env.REACT_APP_API_BASE_URL ||
  "http://k13s406.p.ssafy.io:8082/api/v1";

// ===== 공통 타입 정의 =====

interface CoachInfo {
  id: string; // UUID
  nickname: string;
  profileImage: string;
  job: string;
  introduce: string;
}

interface PaginatedResponse<T> {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  items: T[];
}

// ===== 그룹 상담(클래스) 생성 관련 =====

export interface CreateGroupConsultationRequest {
  title: string; // 최대 200자
  description: string;
  startAt: string; // ISO 8601 형식: "2025-10-30T09:00:00"
  endAt: string; // ISO 8601 형식: "2025-10-30T10:00:00"
  capacity: number; // 1-100
}

export type CreateGroupConsultationResponse = ApiResponse<number>; // consultation id 반환

/**
 * 그룹 상담(클래스) 생성 API
 * POST /api/v1/group-consultations
 * 코치만 호출 가능
 */
export const createGroupConsultationApi = async (
  classData: CreateGroupConsultationRequest,
  classImage?: File
): Promise<CreateGroupConsultationResponse> => {
  const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);

  if (!token) {
    throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
  }

  const formData = new FormData();

  // data 필드: JSON Blob으로 추가 (Content-Type: application/json)
  // filename을 명시하여 Content-Type이 제대로 전달되도록 함
  const jsonBlob = new Blob([JSON.stringify(classData)], {
    type: "application/json",
  });
  formData.append("data", jsonBlob, "data.json");

  // classImage 필드: File 객체를 그대로 사용
  if (classImage) {
    formData.append("classImage", classImage, classImage.name);
  }

  // 디버깅: FormData 내용 확인
  console.log("=== 클래스 생성 API 요청 디버깅 ===");
  console.log("URL:", `${API_BASE_URL}/group-consultations`);
  console.log("Request Data:", classData);
  console.log("classImage:", classImage ? {
    name: classImage.name,
    type: classImage.type,
    size: classImage.size,
  } : "없음");

  // FormData의 모든 키 확인
  console.log("FormData keys:");
  const formDataKeys: string[] = [];
  formData.forEach((value, key) => {
    if (!formDataKeys.includes(key)) {
      formDataKeys.push(key);
      console.log(`  - ${key}`);
    }
  });

  // FormData의 data 필드 내용 확인
  const dataValue = formData.get("data");
  if (dataValue instanceof Blob) {
    dataValue.text().then((text) => {
      console.log("FormData 'data' 필드 내용:", text);
      try {
        const parsed = JSON.parse(text);
        console.log("Parsed JSON:", parsed);
      } catch (e) {
        console.error("JSON 파싱 실패:", e);
      }
    });
  }

  const response = await axios.post<CreateGroupConsultationResponse>(
    `${API_BASE_URL}/group-consultations`,
    formData,
    {
      headers: {
        Authorization: `Bearer ${token}`,
        // Content-Type 헤더를 명시하지 않아 axios가 자동으로 multipart/form-data와 boundary를 설정하도록 함
      },
      // 요청 인터셉터를 통한 디버깅
      onUploadProgress: (progressEvent: { loaded: number; total?: number }) => {
        if (progressEvent.total) {
          const percentCompleted = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total
          );
          console.log(`업로드 진행률: ${percentCompleted}%`);
        }
      },
    } as any // onUploadProgress를 위한 타입 캐스팅
  );

  console.log("=== API 응답 ===");
  console.log("Response:", response.data);

  return response.data;
};

// ===== 그룹 상담(클래스) 목록 조회 =====

export interface GroupConsultationListItem {
  id: number;
  title: string;
  imageUrl: string;
  startAt: string; // ISO 8601
  endAt: string; // ISO 8601
  capacity: number;
  currentParticipants: number;
  availableSeats: number;
  isFull: boolean;
  coachName: string;
  coachProfileImage: string;
}

export type GroupConsultationListResponse = ApiResponse<
  PaginatedResponse<GroupConsultationListItem>
>;

/**
 * 그룹 상담(클래스) 목록 조회 API
 * GET /api/v1/group-consultations
 * @param sameOrganization - 같은 소속만 조회 여부
 * @param page - 페이지 번호 (0부터 시작)
 * @param size - 페이지 크기
 */
export const getGroupConsultationsApi = async (
  sameOrganization: boolean = false,
  page: number = 0,
  size: number = 10
): Promise<GroupConsultationListResponse> => {
  const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);

  if (!token) {
    throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
  }

  const response = await axios.get<GroupConsultationListResponse>(
    `${API_BASE_URL}/group-consultations`,
    {
      params: { sameOrganization, page, size },
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  return response.data;
};

// ===== 그룹 상담(클래스) 상세 조회 =====

export interface GroupConsultationDetail {
  id: number;
  title: string;
  description: string;
  imageUrl: string;
  startAt: string;
  endAt: string;
  capacity: number;
  currentParticipants: number;
  availableSeats: number;
  isFull: boolean;
  coach: CoachInfo;
}

export type GroupConsultationDetailResponse =
  ApiResponse<GroupConsultationDetail>;

/**
 * 그룹 상담(클래스) 상세 조회 API
 * GET /api/v1/group-consultations/{id}
 * 인증 불필요
 */
export const getGroupConsultationDetailApi = async (
  id: number
): Promise<GroupConsultationDetailResponse> => {
  const response = await axios.get<GroupConsultationDetailResponse>(
    `${API_BASE_URL}/group-consultations/${id}`
  );

  return response.data;
};

// ===== 그룹 상담(클래스) 수정 =====

export interface UpdateGroupConsultationRequest {
  title?: string; // 최대 200자
  description?: string;
  imageUrl?: string;
  startAt?: string;
  endAt?: string;
  capacity?: number; // 1-100
}

export type UpdateGroupConsultationResponse = ApiResponse<void>;

/**
 * 그룹 상담(클래스) 수정 API
 * PUT /api/v1/group-consultations/{id}
 * 코치만 호출 가능, 예약자가 있으면 수정 불가
 */
export const updateGroupConsultationApi = async (
  id: number,
  classData: UpdateGroupConsultationRequest
): Promise<UpdateGroupConsultationResponse> => {
  const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);

  if (!token) {
    throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
  }

  const response = await axios.put<UpdateGroupConsultationResponse>(
    `${API_BASE_URL}/group-consultations/${id}`,
    classData,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  return response.data;
};

// ===== 그룹 상담(클래스) 삭제 =====

export type DeleteGroupConsultationResponse = ApiResponse<void>;

/**
 * 그룹 상담(클래스) 삭제(취소) API
 * DELETE /api/v1/group-consultations/{id}
 * 코치만 호출 가능
 */
export const deleteGroupConsultationApi = async (
  id: number
): Promise<DeleteGroupConsultationResponse> => {
  const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);

  if (!token) {
    throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
  }

  const response = await axios.delete<DeleteGroupConsultationResponse>(
    `${API_BASE_URL}/group-consultations/${id}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  return response.data;
};


// ===== 그룹 상담(클래스) 참여 취소 =====

export type CancelGroupConsultationResponse = ApiResponse<void>;

/**
 * 그룹 상담(클래스) 참여 취소 API
 * DELETE /api/v1/group-consultations/{consultationId}/participants
 * 당일 취소 불가
 */
export const cancelGroupConsultationParticipationApi = async (
  consultationId: number
): Promise<CancelGroupConsultationResponse> => {
  const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);

  if (!token) {
    throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
  }

  const response = await axios.delete<CancelGroupConsultationResponse>(
    `${API_BASE_URL}/group-consultations/${consultationId}/participants`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  return response.data;
};

// ===== 유틸리티 함수 =====

/**
 * 토큰을 가져오는 헬퍼 함수
 */
const getAuthToken = (): string => {
  const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);
  if (!token) {
    throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
  }
  return token;
};

/**
 * Authorization 헤더를 생성하는 헬퍼 함수
 */
const getAuthHeaders = () => ({
  Authorization: `Bearer ${getAuthToken()}`,
});
