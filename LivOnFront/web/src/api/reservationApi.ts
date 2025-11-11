// 예약 관련 API 함수들
import axios from "axios";

const API_BASE_URL =
  process.env.REACT_APP_API_BASE_URL || "http://localhost:8081";

// 타입 정의
interface ReservationData {
  coachId: string;
  classId?: string;
  type: "class" | "personal" | "consultation";
  title: string;
  description?: string;
  scheduledAt: string;
  duration: number;
}

interface ReservationResponse {
  id: string;
  userId: string;
  coachId: string;
  classId?: string;
  type: string;
  title: string;
  description?: string;
  scheduledAt: string;
  duration: number;
  status: string;
  price: number;
  paymentStatus: string;
  createdAt: string;
  updatedAt: string;
}

interface ReservationListResponse {
  reservations: ReservationResponse[];
  totalCount: number;
  page: number;
  pageSize: number;
  hasNext: boolean;
}

// 예약 생성 API
export const createReservationApi = async (
  reservationData: ReservationData
): Promise<ReservationResponse> => {
  const response = await axios.post<ReservationResponse>(
    `${API_BASE_URL}/reservation/create`,
    reservationData
  );
  return response.data;
};

// 현재 예약 목록 조회 API
export const getCurrentReservationsApi = async (
  coachId: string
): Promise<ReservationListResponse> => {
  const response = await axios.get<ReservationListResponse>(
    `${API_BASE_URL}/reservation/current/${coachId}`
  );
  return response.data;
};

// 지난 예약 목록 조회 API
export const getPastReservationsApi = async (
  coachId: string
): Promise<ReservationListResponse> => {
  const response = await axios.get<ReservationListResponse>(
    `${API_BASE_URL}/reservation/past/${coachId}`
  );
  return response.data;
};

// 예약 승인 API
export const approveReservationApi = async (
  reservationId: string
): Promise<{ success: boolean; message: string }> => {
  const response = await axios.put<{ success: boolean; message: string }>(
    `${API_BASE_URL}/reservation/${reservationId}/approve`
  );
  return response.data;
};

// 예약 취소 API
export const cancelReservationApi = async (
  reservationId: string
): Promise<{ success: boolean; message: string }> => {
  const response = await axios.put<{ success: boolean; message: string }>(
    `${API_BASE_URL}/reservation/${reservationId}/cancel`
  );
  return response.data;
};
