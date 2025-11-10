import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { signInApi } from '../api/authApi';
import { CONFIG } from '../constants/config';

interface User {
  id: string;
  email: string;
  name: string;
  nickname?: string;
  role: 'coach' | 'member';
  profileImage?: string;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export const useAuth = () => {
  const [authState, setAuthState] = useState<AuthState>({
    user: null,
    isAuthenticated: false,
    isLoading: true,
  });
  
  const navigate = useNavigate();

  // 로그인
  const login = useCallback(async (email: string, password: string) => {
    try {
      setAuthState(prev => ({ ...prev, isLoading: true }));
      
      // 새로운 로그인 API 호출
      const response = await signInApi(email, password);
      
      if (response.isSuccess) {
        const { accessToken, refreshToken, role } = response.result;
        
        // 토큰 저장
        localStorage.setItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY, accessToken);
        localStorage.setItem(CONFIG.TOKEN.REFRESH_TOKEN_KEY, refreshToken);
        
        // 역할 정보 저장
        localStorage.setItem('user_role', JSON.stringify(role));
        
        // 사용자 정보는 이메일로 임시 설정 (실제로는 사용자 정보 조회 API 필요)
        const user: User = {
          id: email, // 임시 ID
          email: email,
          name: email.split('@')[0], // 임시 이름
          role: role.includes('COACH') ? 'coach' : 'member',
        };
        
        localStorage.setItem(CONFIG.STORAGE_KEYS.USER_INFO, JSON.stringify(user));
        
        setAuthState({
          user,
          isAuthenticated: true,
          isLoading: false,
        });
        
        console.log('✅ 로그인 성공:', response.message);
        return { success: true, message: response.message };
      } else {
        throw new Error(response.message || '로그인 실패');
      }
    } catch (error) {
      setAuthState(prev => ({ ...prev, isLoading: false }));
      const errorMessage = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.';
      console.error('❌ 로그인 오류:', errorMessage);
      return { success: false, error: errorMessage };
    }
  }, []);

  // 로그아웃
  const logout = useCallback(() => {
    setAuthState({
      user: null,
      isAuthenticated: false,
      isLoading: false,
    });
    
    // 토큰 제거
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user_info');
    
    navigate('/');
  }, [navigate]);

  // 회원가입
  const signup = useCallback(async (userData: any) => {
    try {
      setAuthState(prev => ({ ...prev, isLoading: true }));
      
      const response = await fetch('/api/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData),
      });
      
      if (response.ok) {
        setAuthState(prev => ({ ...prev, isLoading: false }));
        return { success: true };
      } else {
        throw new Error('회원가입 실패');
      }
    } catch (error) {
      setAuthState(prev => ({ ...prev, isLoading: false }));
      return { success: false, error: error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.' };
    }
  }, []);

  // 인증 상태 확인
  useEffect(() => {
    const checkAuthStatus = () => {
      try {
        const token = localStorage.getItem('access_token');
        const userInfo = localStorage.getItem('user_info');
        
        if (token && userInfo) {
          const user = JSON.parse(userInfo);
          setAuthState({
            user,
            isAuthenticated: true,
            isLoading: false,
          });
        } else {
          setAuthState({
            user: null,
            isAuthenticated: false,
            isLoading: false,
          });
        }
      } catch (error) {
        setAuthState({
          user: null,
          isAuthenticated: false,
          isLoading: false,
        });
      }
    };

    checkAuthStatus();
  }, []);

  return {
    ...authState,
    login,
    logout,
    signup,
  };
};
