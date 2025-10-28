import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';

interface User {
  id: string;
  email: string;
  name: string;
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
      
      // API 호출 (실제 구현 시 authApi 사용)
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });
      
      if (response.ok) {
        const data = await response.json();
        const user: User = data.user;
        
        setAuthState({
          user,
          isAuthenticated: true,
          isLoading: false,
        });
        
        // 토큰 저장
        localStorage.setItem('access_token', data.accessToken);
        localStorage.setItem('user_info', JSON.stringify(user));
        
        return { success: true };
      } else {
        throw new Error('로그인 실패');
      }
    } catch (error) {
      setAuthState(prev => ({ ...prev, isLoading: false }));
      return { success: false, error: error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.' };
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
