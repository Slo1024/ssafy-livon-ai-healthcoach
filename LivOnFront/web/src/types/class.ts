// 클래스 관련 타입 정의
export interface Class {
  id: string;
  coachId: string;
  title: string;
  description: string;
  category: ClassCategory;
  type: ClassType;
  maxParticipants: number;
  currentParticipants: number;
  duration: number; // 분 단위
  price: number;
  isActive: boolean;
  schedule: ClassSchedule[];
  createdAt: Date;
  updatedAt: Date;
}

export type ClassCategory = 
  | 'fitness'      // 피트니스
  | 'nutrition'    // 영양
  | 'mental'       // 멘탈헬스
  | 'lifestyle'    // 라이프스타일
  | 'rehabilitation' // 재활
  | 'yoga'         // 요가
  | 'pilates'      // 필라테스
  | 'dance'        // 댄스
  | 'martial_arts' // 무술
  | 'swimming';    // 수영

export type ClassType = 
  | 'group'        // 그룹 클래스
  | 'personal'    // 개인 클래스
  | 'online'      // 온라인 클래스
  | 'offline';    // 오프라인 클래스

export interface ClassSchedule {
  id: string;
  classId: string;
  date: Date;
  startTime: string; // HH:mm 형식
  endTime: string; // HH:mm 형식
  isRecurring: boolean;
  recurringPattern?: RecurringPattern;
  maxParticipants: number;
  currentParticipants: number;
  isActive: boolean;
}

export interface RecurringPattern {
  type: 'daily' | 'weekly' | 'monthly';
  interval: number; // 반복 간격
  daysOfWeek?: number[]; // 0-6 (일-토)
  endDate?: Date;
}

export interface ClassCreateRequest {
  title: string;
  description: string;
  category: ClassCategory;
  type: ClassType;
  maxParticipants: number;
  duration: number;
  price: number;
  schedule: Omit<ClassSchedule, 'id' | 'classId'>[];
}

export interface ClassUpdateRequest {
  title?: string;
  description?: string;
  category?: ClassCategory;
  type?: ClassType;
  maxParticipants?: number;
  duration?: number;
  price?: number;
  isActive?: boolean;
}

export interface ClassListResponse {
  classes: Class[];
  totalCount: number;
  page: number;
  pageSize: number;
  hasNext: boolean;
}

export interface ClassDetailResponse {
  class: Class;
  coach: {
    id: string;
    name: string;
    profileImage?: string;
    rating: number;
    reviewCount: number;
  };
  participants: ClassParticipant[];
}

export interface ClassParticipant {
  id: string;
  userId: string;
  name: string;
  profileImage?: string;
  joinedAt: Date;
  status: 'active' | 'completed' | 'cancelled';
}
