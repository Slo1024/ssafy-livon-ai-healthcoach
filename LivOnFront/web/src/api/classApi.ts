// 클래스 관련 API 함수들
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

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
export const createClassApi = async (classData: ClassData): Promise<ClassResponse> => {
  const response = await axios.post<ClassResponse>(`${API_BASE_URL}/class/create`, classData);
  return response.data;
};

// 클래스 목록 조회 API
export const getClassListApi = async (coachId: string): Promise<ClassListResponse> => {
  const response = await axios.get<ClassListResponse>(`${API_BASE_URL}/class/list/${coachId}`);
  return response.data;
};

// 클래스 상세 조회 API
export const getClassDetailApi = async (classId: string): Promise<ClassResponse> => {
  const response = await axios.get<ClassResponse>(`${API_BASE_URL}/class/${classId}`);
  return response.data;
};

// 클래스 수정 API
export const updateClassApi = async (classId: string, classData: Partial<ClassData>): Promise<ClassResponse> => {
  const response = await axios.put<ClassResponse>(`${API_BASE_URL}/class/${classId}`, classData);
  return response.data;
};

// 클래스 삭제 API
export const deleteClassApi = async (classId: string): Promise<{ success: boolean; message: string }> => {
  const response = await axios.delete<{ success: boolean; message: string }>(`${API_BASE_URL}/class/${classId}`);
  return response.data;
};
