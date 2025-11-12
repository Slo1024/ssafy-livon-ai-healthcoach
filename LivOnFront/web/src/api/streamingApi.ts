// 스트리밍 관련 API 함수들
import axios from "axios";

const API_BASE_URL =
  process.env.REACT_APP_API_BASE_URL || "http://localhost:8081";

// 타입 정의
interface StreamingSessionData {
  coachId: string;
  classId?: string;
  reservationId?: string;
  title: string;
  description?: string;
  startTime: string;
  maxParticipants: number;
}

interface StreamingSessionResponse {
  id: string;
  coachId: string;
  classId?: string;
  reservationId?: string;
  title: string;
  description?: string;
  startTime: string;
  endTime?: string;
  status: string;
  maxParticipants: number;
  currentParticipants: number;
  createdAt: string;
  updatedAt: string;
}

interface StreamingParticipantsResponse {
  participants: Array<{
    id: string;
    userId: string;
    sessionId: string;
    name: string;
    profileImage?: string;
    role: "coach" | "member";
    isVideoEnabled: boolean;
    isAudioEnabled: boolean;
    joinedAt: string;
    leftAt?: string;
    connectionStatus: string;
  }>;
}

interface StreamingAnalyticsResponse {
  sessionId: string;
  totalDuration: number;
  peakParticipants: number;
  averageParticipants: number;
  totalMessages: number;
  engagementScore: number;
  technicalIssues: number;
  qualityMetrics: {
    averageBitrate: number;
    averageLatency: number;
    packetLoss: number;
  };
}

// 스트리밍 세션 생성 API
export const createStreamingSessionApi = async (
  sessionData: StreamingSessionData
): Promise<StreamingSessionResponse> => {
  const response = await axios.post<StreamingSessionResponse>(
    `${API_BASE_URL}/streaming/create-session`,
    sessionData
  );
  return response.data;
};

// 스트리밍 세션 종료 API
export const endStreamingSessionApi = async (
  sessionId: string
): Promise<{ success: boolean; message: string }> => {
  const response = await axios.put<{ success: boolean; message: string }>(
    `${API_BASE_URL}/streaming/${sessionId}/end`
  );
  return response.data;
};

// 스트리밍 참가자 목록 조회 API
export const getStreamingParticipantsApi = async (
  sessionId: string
): Promise<StreamingParticipantsResponse> => {
  const response = await axios.get<StreamingParticipantsResponse>(
    `${API_BASE_URL}/streaming/${sessionId}/participants`
  );
  return response.data;
};

// 스트리밍 분석 데이터 조회 API
export const getStreamingAnalyticsApi = async (
  sessionId: string
): Promise<StreamingAnalyticsResponse> => {
  const response = await axios.get<StreamingAnalyticsResponse>(
    `${API_BASE_URL}/streaming/${sessionId}/analytics`
  );
  return response.data;
};
