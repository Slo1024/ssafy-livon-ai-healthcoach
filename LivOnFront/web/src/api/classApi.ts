// 클래스 관련 API 함수들
import axios from "axios";
import { CONFIG } from "../constants/config";
import { ApiResponse } from "./authApi";

const API_BASE_URL =
  CONFIG.API_BASE_URL ||
  process.env.REACT_APP_API_BASE_URL ||
  "http://localhost:8081";

// 타입 정의
interface ClassData {
  title: string;
  description: string;
  category: string;
  type: string;
  maxParticipants: number;
  duration: number;
  price: number;
}

interface ClassResponse {
  id: string;
  title: string;
  description: string;
  category: string;
  type: string;
  maxParticipants: number;
  duration: number;
  price: number;
  createdAt: string;
  updatedAt: string;
}

interface ClassListResponse {
  classes: ClassResponse[];
  totalCount: number;
  page: number;
  pageSize: number;
  hasNext: boolean;
}

// 클래스 개설 API
export const createClassApi = async (
  classData: ClassData
): Promise<ClassResponse> => {
  const response = await axios.post<ClassResponse>(
    `${API_BASE_URL}/class/create`,
    classData
  );
  return response.data;
};

// 클래스 목록 조회 API
export const getClassListApi = async (
  coachId: string
): Promise<ClassListResponse> => {
  const response = await axios.get<ClassListResponse>(
    `${API_BASE_URL}/class/list/${coachId}`
  );
  return response.data;
};

// 클래스 상세 조회 API
export const getClassDetailApi = async (
  classId: string
): Promise<ClassResponse> => {
  const response = await axios.get<ClassResponse>(
    `${API_BASE_URL}/class/${classId}`
  );
  return response.data;
};

// 클래스 수정 API
export const updateClassApi = async (
  classId: string,
  classData: Partial<ClassData>
): Promise<ClassResponse> => {
  const response = await axios.put<ClassResponse>(
    `${API_BASE_URL}/class/${classId}`,
    classData
  );
  return response.data;
};

// 클래스 삭제 API
export const deleteClassApi = async (
  classId: string
): Promise<{ success: boolean; message: string }> => {
  const response = await axios.delete<{ success: boolean; message: string }>(
    `${API_BASE_URL}/class/${classId}`
  );
  return response.data;
};

// 새로운 클래스 생성 API 타입 정의
export interface CreateGroupConsultationRequest {
  title: string; // 클래스 이름
  description: string; // 클래스 설명
  startAt: string; // 클래스 시작 시간 (ISO 8601 형식: "2025-10-30T09:00:00")
  endAt: string; // 클래스 종료 시간 (ISO 8601 형식: "2025-10-30T10:00:00")
  capacity: number; // 클래스 최대 수용 인원 설정
}

export type CreateGroupConsultationResponse = ApiResponse<number>; // consultation id 반환

// 새로운 클래스 생성 API (POST /api/v1/group-consultations)
// form-data 형식으로 전송: data (JSON 문자열), classImage (파일, 선택사항)
export const createGroupConsultationApi = async (
  classData: CreateGroupConsultationRequest,
  classImage?: File
): Promise<CreateGroupConsultationResponse> => {
  // 토큰 가져오기
  const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);

  if (!token) {
    throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
  }

  // FormData 생성
  const formData = new FormData();

  // data 필드: JSON 문자열로 변환하여 추가
  formData.append("data", JSON.stringify(classData));

  // classImage 필드: 파일이 있으면 추가 (선택사항)
  if (classImage) {
    formData.append("classImage", classImage);
  }

  // API 호출
  const response = await axios.post<CreateGroupConsultationResponse>(
    `${API_BASE_URL}/api/v1/group-consultations`,
    formData,
    {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "multipart/form-data", // form-data 형식
      },
    }
  );

  return response.data;
};
