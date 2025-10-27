// 라우트 경로 정의
export const ROUTES = {
  // 메인 페이지
  HOME: '/',
  ABOUT: '/about',
  DOWNLOAD: '/download',
  
  // 인증 관련
  LOGIN: '/auth/login',
  SIGNUP: '/auth/signup',
  TERMS: '/auth/terms',
  EMAIL_VERIFICATION: '/auth/email-verification',
  PROFILE_SETUP: '/auth/profile-setup',
  SIGNUP_COMPLETE: '/auth/signup-complete',
  
  // 코치 관련
  COACH_DASHBOARD: '/coach/dashboard',
  CLASS_SETUP: '/coach/class-setup',
  RESERVATION_MANAGE: '/coach/reservation-manage',
  PAST_RESERVATION: '/coach/past-reservation',
  STREAMING: '/coach/streaming',
  COACH_MYPAGE: '/coach/mypage',
  
  // 고객센터
  FAQ: '/support/faq',
  INQUIRY: '/support/inquiry',
  
  // 기타
  NOT_FOUND: '/404',
} as const;

// 라우트 타입
export type RouteKey = keyof typeof ROUTES;
export type RoutePath = typeof ROUTES[RouteKey];
