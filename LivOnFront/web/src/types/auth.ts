// 인증 관련 타입 정의
export interface User {
  id: string;
  email: string;
  name: string;
  role: 'coach' | 'member';
  profileImage?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResponse {
  user: User;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
  role: 'coach' | 'member';
  profileImage?: string;
}

export interface SignupResponse {
  user: User;
  message: string;
}

export interface EmailVerificationRequest {
  email: string;
  code: string;
}

export interface EmailVerificationResponse {
  success: boolean;
  message: string;
}

export interface PasswordResetRequest {
  email: string;
}

export interface PasswordResetResponse {
  success: boolean;
  message: string;
}

export interface SocialLoginRequest {
  provider: 'kakao' | 'naver' | 'google';
  token: string;
}

export interface SocialLoginResponse {
  user: User;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  expiresIn: number;
}
