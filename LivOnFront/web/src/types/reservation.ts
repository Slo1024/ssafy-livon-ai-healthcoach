// 예약 관련 타입 정의
export interface Reservation {
  id: string;
  userId: string;
  coachId: string;
  classId?: string;
  type: ReservationType;
  title: string;
  description?: string;
  scheduledAt: Date;
  duration: number; // 분 단위
  status: ReservationStatus;
  price: number;
  paymentStatus: PaymentStatus;
  createdAt: Date;
  updatedAt: Date;
}

export type ReservationType = 
  | 'class'        // 클래스 예약
  | 'personal'     // 개인 상담
  | 'consultation'; // 컨설팅

export type ReservationStatus = 
  | 'pending'      // 대기 중
  | 'confirmed'     // 확정
  | 'in_progress'  // 진행 중
  | 'completed'    // 완료
  | 'cancelled'   // 취소
  | 'no_show';    // 노쇼

export type PaymentStatus = 
  | 'pending'      // 결제 대기
  | 'paid'         // 결제 완료
  | 'refunded'     // 환불 완료
  | 'failed';      // 결제 실패

export interface ReservationCreateRequest {
  coachId: string;
  classId?: string;
  type: ReservationType;
  title: string;
  description?: string;
  scheduledAt: Date;
  duration: number;
}

export interface ReservationUpdateRequest {
  title?: string;
  description?: string;
  scheduledAt?: Date;
  duration?: number;
  status?: ReservationStatus;
}

export interface ReservationListResponse {
  reservations: Reservation[];
  totalCount: number;
  page: number;
  pageSize: number;
  hasNext: boolean;
}

export interface ReservationDetailResponse {
  reservation: Reservation;
  user: {
    id: string;
    name: string;
    email: string;
    profileImage?: string;
  };
  coach: {
    id: string;
    name: string;
    profileImage?: string;
    rating: number;
  };
  class?: {
    id: string;
    title: string;
    description: string;
  };
}

export interface ReservationApprovalRequest {
  reservationId: string;
  status: 'confirmed' | 'cancelled';
  reason?: string;
}

export interface ReservationCancellationRequest {
  reservationId: string;
  reason: string;
  refundRequest?: boolean;
}

export interface ReservationSearchRequest {
  coachId?: string;
  userId?: string;
  status?: ReservationStatus;
  type?: ReservationType;
  startDate?: Date;
  endDate?: Date;
  page?: number;
  pageSize?: number;
}

export interface ReservationStatistics {
  totalReservations: number;
  pendingReservations: number;
  confirmedReservations: number;
  completedReservations: number;
  cancelledReservations: number;
  totalRevenue: number;
  averageRating: number;
}
