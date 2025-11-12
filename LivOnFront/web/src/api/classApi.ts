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

  // data 필드: JSON Blob으로 추가
  formData.append(
    "data",
    new Blob([JSON.stringify(classData)], { type: "application/json" })
  );

  // classImage 필드: 파일이 있으면 추가
  if (classImage) {
    formData.append("classImage", classImage);
  }

  const response = await axios.post<CreateGroupConsultationResponse>(
    `${API_BASE_URL}/group-consultations`,
    formData,
    {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "multipart/form-data",
      },
    }
  );

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

// ===== 그룹 상담(클래스) 예약 =====

export type ReserveGroupConsultationResponse = ApiResponse<number>; // participant id 반환

/**
 * 그룹 상담(클래스) 예약 API
 * POST /api/v1/group-consultations/{classId}
 * 일반 사용자가 클래스 참가 신청
 */
export const reserveGroupConsultationApi = async (
  classId: number
): Promise<ReserveGroupConsultationResponse> => {
  const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);

  if (!token) {
    throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
  }

  const response = await axios.post<ReserveGroupConsultationResponse>(
    `${API_BASE_URL}/group-consultations/${classId}`,
    {},
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
