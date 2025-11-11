// 인증 관련 API 함수들
import axios from "axios";
import { CONFIG } from "../constants/config";

const API_BASE_URL =
  CONFIG.API_BASE_URL ||
  process.env.REACT_APP_API_BASE_URL ||
  "http://localhost:8080";

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
    role: "coach" | "member";
  };
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

interface SignupRequest {
  email: string;
  password: string;
  name: string;
  role: "coach" | "member";
  profileImage?: string;
}

interface SignupResponse {
  user: {
    id: string;
    email: string;
    name: string;
    role: "coach" | "member";
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
  provider: "kakao" | "naver" | "google";
  token: string;
}

interface SocialLoginResponse {
  user: {
    id: string;
    email: string;
    name: string;
    role: "coach" | "member";
  };
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

// 로그인 API
export const loginApi = async (
  email: string,
  password: string
): Promise<LoginResponse> => {
  const response = await axios.post<LoginResponse>(
    `${API_BASE_URL}/auth/login`,
    {
      email,
      password,
    }
  );
  return response.data;
};

// 회원가입 API
export const signupApi = async (
  userData: SignupRequest
): Promise<SignupResponse> => {
  const response = await axios.post<SignupResponse>(
    `${API_BASE_URL}/auth/signup`,
    userData
  );
  return response.data;
};

// 이메일 인증 API
export const verifyEmailApi = async (
  email: string,
  code: string
): Promise<EmailVerificationResponse> => {
  const response = await axios.post<EmailVerificationResponse>(
    `${API_BASE_URL}/auth/verify-email`,
    {
      email,
      code,
    }
  );
  return response.data;
};

// 소셜 로그인 API
export const socialLoginApi = async (
  provider: string,
  token: string
): Promise<SocialLoginResponse> => {
  const response = await axios.post<SocialLoginResponse>(
    `${API_BASE_URL}/auth/social-login`,
    {
      provider,
      token,
    }
  );
  return response.data;
};

// 새로운 로그인 API 타입 정의
export interface SignInRequest {
  email: string;
  password: string;
}

export interface SignInResult {
  grantType: string;
  accessToken: string;
  refreshToken: string;
  refreshTokenExpirationTime: number;
  role: string[];
}

export interface ApiResponse<T> {
  isSuccess: boolean;
  code: string;
  message: string;
  result: T;
}

export type SignInResponse = ApiResponse<SignInResult>;

export interface UserProfile {
  userId: string;
  email: string;
  nickname?: string;
  profileImage?: string;
  phoneNumber?: string;
  roles?: string[];
  role?: string | string[];
}

// 새로운 로그인 API (POST /api/v1/user/sign-in)
export const signInApi = async (
  email: string,
  password: string
): Promise<SignInResponse> => {
  const response = await axios.post<SignInResponse>(
    `${API_BASE_URL}/user/sign-in`,
    {
      email,
      password,
    },
    {
      headers: {
        "Content-Type": "application/json",
      },
    }
  );
  return response.data;
};

// 사용자 정보 조회 API (GET /api/v1/user/me)
export const getMyProfileApi = async (
  accessToken: string
): Promise<ApiResponse<UserProfile>> => {
  const response = await axios.get<ApiResponse<UserProfile>>(
    `${API_BASE_URL}/api/v1/user/my-info`,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    }
  );
  return response.data;
};
