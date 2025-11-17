// 예약 관련 API 함수들
import axios from "axios";

const API_BASE_URL =
  process.env.REACT_APP_API_BASE_URL || "http://k13s406.p.ssafy.io:8082/api/v1";

// 공통 타입
interface CoachInfo {
  userId: string;
  nickname: string;
  job: string;
  introduce: string;
  profileImage: string;
  certificates: string[];
  organizations: string;
}

// ===== 1:1 상담 관련 =====

interface IndividualReservationRequest {
  coachId: string; // UUID
  startAt: string; // ISO 8601 format
  endAt: string; // ISO 8601 format
  preQnA?: string;
}

// 1:1 상담 예약 생성
export const createIndividualConsultationApi = async (
  token: string,
  data: IndividualReservationRequest
): Promise<number> => {
  const response = await axios.post<{
    isSuccess: boolean;
    code: string;
    message: string;
    result: number;
  }>(`${API_BASE_URL}/individual-consultations`, data, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return response.data.result;
};

// 1:1 상담 취소 (코치용)
// 백엔드 스펙 기준: DELETE /api/v1/coaches/consultations/{consultationId}
export const cancelIndividualConsultationApi = async (
  token: string,
  consultationId: number
): Promise<void> => {
  await axios.delete(
    `${API_BASE_URL}/coaches/consultations/${consultationId}`,
    {
      headers: { Authorization: `Bearer ${token}` },
    }
  );
};

// ===== 그룹 상담(클래스) 관련 =====

interface GroupConsultationCreateRequest {
  title: string; // max 200자
  description: string;
  startAt: string;
  endAt: string;
  capacity: number; // 1-100
}

interface GroupConsultationListItem {
  id: number;
  title: string;
  imageUrl: string;
  startAt: string;
  endAt: string;
  capacity: number;
  currentParticipants: number;
  availableSeats: number;
  isFull: boolean;
  coachName: string;
  coachProfileImage: string;
}

interface GroupConsultationDetail {
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

interface PaginatedResponse<T> {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  items: T[];
}

// 클래스 목록 조회
export const getGroupConsultationsApi = async (
  token: string,
  sameOwner: boolean = false,
  page: number = 0,
  size: number = 10
): Promise<PaginatedResponse<GroupConsultationListItem>> => {
  const response = await axios.get<{
    isSuccess: boolean;
    code: string;
    message: string;
    result: PaginatedResponse<GroupConsultationListItem>;
  }>(`${API_BASE_URL}/group-consultations`, {
    params: { sameOrganization: sameOwner, page, size },
    headers: { Authorization: `Bearer ${token}` },
  });
  return response.data.result;
};

// 클래스 상세 조회
export const getGroupConsultationDetailApi = async (
  id: number
): Promise<GroupConsultationDetail> => {
  const response = await axios.get<{
    isSuccess: boolean;
    code: string;
    message: string;
    result: GroupConsultationDetail;
  }>(`${API_BASE_URL}/group-consultations/${id}`);
  return response.data.result;
};

// 클래스 생성 (코치용)
export const createGroupConsultationApi = async (
  token: string,
  data: GroupConsultationCreateRequest,
  classImage?: File
): Promise<number> => {
  const formData = new FormData();
  formData.append(
    "data",
    new Blob([JSON.stringify(data)], { type: "application/json" })
  );
  if (classImage) {
    formData.append("classImage", classImage);
  }

  const response = await axios.post<{
    isSuccess: boolean;
    code: string;
    message: string;
    result: number;
  }>(`${API_BASE_URL}/group-consultations`, formData, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "multipart/form-data",
    },
  });
  return response.data.result;
};

// 클래스 참여 취소 (코치/참가자 공용: 참가자 취소 시 해당 엔드포인트 사용)
export const cancelGroupConsultationParticipationApi = async (
  token: string,
  consultationId: number
): Promise<void> => {
  await axios.delete(
    `${API_BASE_URL}/group-consultations/${consultationId}/participants`,
    {
      headers: { Authorization: `Bearer ${token}` },
    }
  );
};

// ===== 코치 시간 블록(막기) 관련 =====

interface BlockedTimesRequest {
  blockedTimes: string[]; // 최대 8개, 시간 문자열 배열 (예: ["09:00", "10:00", "14:00"])
}

interface BlockedTimesResponse {
  date: string; // YYYY-MM-DD 형식
  blockedTimes: string[]; // 막힌 시간대 목록
}

/**
 * 코치가 막아놓은 시간대 조회 API
 * GET /api/v1/coaches/block-times
 * @param token - 인증 토큰
 * @param date - 조회할 날짜 (YYYY-MM-DD 형식)
 */
export const getCoachBlockedTimesApi = async (
  token: string,
  date: string
): Promise<BlockedTimesResponse> => {
  const response = await axios.get<{
    isSuccess: boolean;
    code: string;
    message: string;
    result: BlockedTimesResponse;
  }>(`${API_BASE_URL}/coaches/block-times`, {
    params: { date },
    headers: { Authorization: `Bearer ${token}` },
  });
  return response.data.result;
};

/**
 * 코치가 특정 날짜의 시간대를 막기 (업데이트) API
 * PUT /api/v1/coaches/block-times
 * @param token - 인증 토큰
 * @param date - 막을 날짜 (YYYY-MM-DD 형식)
 * @param blockedTimes - 막을 시간대 배열 (최대 8개)
 */
export const updateCoachBlockedTimesApi = async (
  token: string,
  date: string,
  blockedTimes: string[]
): Promise<void> => {
  // 최대 8개 제한 확인
  if (blockedTimes.length > 8) {
    throw new Error("막을 수 있는 시간대는 최대 8개입니다.");
  }

  const response = await axios.put<{
    isSuccess: boolean;
    code: string;
    message: string;
  }>(
    `${API_BASE_URL}/coaches/block-times`,
    { blockedTimes },
    {
      params: { date },
      headers: { Authorization: `Bearer ${token}` },
    }
  );

  if (!response.data.isSuccess) {
    throw new Error(response.data.message || "시간대 막기에 실패했습니다.");
  }
};

// ===== 코치 예약 가능 시간 조회 =====

interface AvailableTime {
  time: string; // "HH:mm" 형식
  isAvailable: boolean;
}

interface AvailableTimesResponse {
  date: string; // YYYY-MM-DD 형식
  availableTimes: AvailableTime[];
}

/**
 * 코치의 예약 가능 시간대 조회 API
 * GET /api/v1/coaches/{coachId}/available-times
 * @param coachId - 코치 UUID
 * @param date - 조회할 날짜 (YYYY-MM-DD 형식)
 */
export const getCoachAvailableTimesApi = async (
  coachId: string,
  date: string
): Promise<AvailableTimesResponse> => {
  const response = await axios.get<{
    isSuccess: boolean;
    code: string;
    message: string;
    result: AvailableTimesResponse;
  }>(`${API_BASE_URL}/coaches/${coachId}/available-times`, {
    params: { date },
  });
  return response.data.result;
};

// ===== 코치용 상담 관리 =====

export interface CoachConsultation {
  consultationId: number;
  type: string;
  status: string;
  startAt: string;
  endAt: string;
  sessionId: string;
  preQna?: string;
  aiSummary?: string;
  title?: string;
  description?: string;
  imageUrl?: string;
  capacity?: number;
  currentParticipants?: number;
  participants?: Array<{
    userId: string;
    nickname: string;
    profileImage: string;
    email: string;
  }>;
}

// 코치 상담 목록 조회
export const getCoachConsultationsApi = async (
  token: string,
  status: "upcoming" | "past",
  type?: "ONE" | "GROUP",
  page: number = 0,
  size: number = 10
): Promise<PaginatedResponse<CoachConsultation>> => {
  const response = await axios.get<{
    isSuccess: boolean;
    code: string;
    message: string;
    result: PaginatedResponse<CoachConsultation>;
  }>(`${API_BASE_URL}/coaches/consultations`, {
    params: { status, type, page, size },
    headers: { Authorization: `Bearer ${token}` },
  });
  return response.data.result;
};

// ===== 회원 정보 조회 =====

export interface MemberInfo {
  userId: string;
  nickname: string;
  email: string;
  profileImage?: string;
  height?: number;
  weight?: number;
  sleepTime?: number; // 시간 단위
  preQna?: string;
}

// 회원 정보 조회 API
export const getMemberInfoApi = async (
  token: string,
  userId: string
): Promise<MemberInfo> => {
  const response = await axios.get<{
    isSuccess: boolean;
    code: string;
    message: string;
    result: MemberInfo;
  }>(`${API_BASE_URL}/user/${userId}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return response.data.result;
};

// ===== 참여자 정보 조회 =====

export interface HealthData {
  height?: number;
  weight?: number;
  steps?: number;
  sleepTime?: number; // 시간 단위
  activityLevel?: string;
  sleepQuality?: string;
  stressLevel?: string;
}

export interface ParticipantMemberInfo {
  nickname: string;
  gender: string;
  ageGroup: string;
  healthData: HealthData;
}

export interface ParticipantInfoResponse {
  memberInfo: ParticipantMemberInfo;
  aiSummary?: string;
}

/**
 * 코치가 상담 참여자 정보 조회 API (여러 참여자 정보 리스트 반환)
 * GET /api/v1/coaches/consultations/{consultationId}/participant-info
 * @param token - 인증 토큰
 * @param consultationId - 상담 ID
 */
export const getParticipantInfoApi = async (
  token: string,
  consultationId: number
): Promise<ParticipantInfoResponse[]> => {
  // 토큰 검증
  if (!token || token.trim() === "") {
    throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
  }

  const response = await axios.get<{
    isSuccess: boolean;
    code: string;
    message: string;
    result: ParticipantInfoResponse[];
  }>(
    `${API_BASE_URL}/coaches/consultations/${consultationId}/participant-info`,
    {
      headers: {
        Authorization: `Bearer ${token.trim()}`,
        "Content-Type": "application/json",
      },
    }
  );

  if (!response.data.isSuccess) {
    throw new Error(
      response.data.message || "참여자 정보 조회에 실패했습니다."
    );
  }

  return response.data.result;
};
