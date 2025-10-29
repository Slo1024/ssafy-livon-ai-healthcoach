// 인증 관련 API 함수들
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

// 타입 정의
interface LoginRequest {
  email: string;
  password: string;
}

interface LoginResponse {
  user: {
    id: string;
    email: string;
    name: string;
    role: 'coach' | 'member';
  };
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

interface SignupRequest {
  email: string;
  password: string;
  name: string;
  role: 'coach' | 'member';
  profileImage?: string;
}

interface SignupResponse {
  user: {
    id: string;
    email: string;
    name: string;
    role: 'coach' | 'member';
  };
  message: string;
}

interface EmailVerificationRequest {
  email: string;
  code: string;
}

interface EmailVerificationResponse {
  success: boolean;
  message: string;
}

interface SocialLoginRequest {
  provider: 'kakao' | 'naver' | 'google';
  token: string;
}

interface SocialLoginResponse {
  user: {
    id: string;
    email: string;
    name: string;
    role: 'coach' | 'member';
  };
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

// 로그인 API
export const loginApi = async (email: string, password: string): Promise<LoginResponse> => {
  const response = await axios.post<LoginResponse>(`${API_BASE_URL}/auth/login`, {
    email,
    password,
  });
  return response.data;
};

// 회원가입 API
export const signupApi = async (userData: SignupRequest): Promise<SignupResponse> => {
  const response = await axios.post<SignupResponse>(`${API_BASE_URL}/auth/signup`, userData);
  return response.data;
};

// 이메일 인증 API
export const verifyEmailApi = async (email: string, code: string): Promise<EmailVerificationResponse> => {
  const response = await axios.post<EmailVerificationResponse>(`${API_BASE_URL}/auth/verify-email`, {
    email,
    code,
  });
  return response.data;
};

// 소셜 로그인 API
export const socialLoginApi = async (provider: string, token: string): Promise<SocialLoginResponse> => {
  const response = await axios.post<SocialLoginResponse>(`${API_BASE_URL}/auth/social-login`, {
    provider,
    token,
  });
  return response.data;
};
