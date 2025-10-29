// 코치 관련 타입 정의
export interface Coach {
  id: string;
  userId: string;
  name: string;
  email: string;
  profileImage?: string;
  introduction?: string;
  specialties: string[];
  certifications: Certification[];
  experience: number; // 경력 (년)
  rating: number; // 평점
  reviewCount: number; // 리뷰 수
  isVerified: boolean; // 인증 여부
  isAvailable: boolean; // 예약 가능 여부
  createdAt: Date;
  updatedAt: Date;
}

export interface Certification {
  id: string;
  name: string;
  issuer: string;
  issueDate: Date;
  expiryDate?: Date;
  certificateImage?: string;
}

export interface CoachProfile {
  coach: Coach;
  statistics: CoachStatistics;
  availability: CoachAvailability[];
}

export interface CoachStatistics {
  totalSessions: number;
  totalClients: number;
  averageRating: number;
  completionRate: number; // 완료율
  responseTime: number; // 평균 응답 시간 (분)
}

export interface CoachAvailability {
  dayOfWeek: number; // 0-6 (일-토)
  startTime: string; // HH:mm 형식
  endTime: string; // HH:mm 형식
  isAvailable: boolean;
}

export interface CoachUpdateRequest {
  name?: string;
  introduction?: string;
  specialties?: string[];
  certifications?: Certification[];
  experience?: number;
  profileImage?: string;
}

export interface CoachVerificationRequest {
  certifications: Certification[];
  experience: number;
  introduction: string;
  profileImage?: string;
}

export interface CoachVerificationResponse {
  success: boolean;
  message: string;
  verificationStatus: 'pending' | 'approved' | 'rejected';
}
