import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyProfileApi, signInApi } from '../api/authApi';
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
      
      if (!response.isSuccess) {
        throw new Error(response.message || '로그인 실패');
      }

      const { accessToken, refreshToken, role: loginRoles } = response.result;

      // 토큰 저장 (먼저 저장하여 이후 API 호출에서 활용)
      localStorage.setItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY, accessToken);
      localStorage.setItem(CONFIG.TOKEN.REFRESH_TOKEN_KEY, refreshToken);

      // 사용자 정보 조회
      const profileResponse = await getMyProfileApi(accessToken);

      if (!profileResponse.isSuccess) {
        throw new Error(profileResponse.message || '사용자 정보 조회 실패');
      }

      const profile = profileResponse.result;

      const roleList = Array.isArray(profile.role)
        ? profile.role
        : Array.isArray(loginRoles)
          ? loginRoles
          : Array.isArray(profile.roles)
            ? profile.roles
            : profile.role
              ? [profile.role]
              : [];

      const resolvedRole: User['role'] = roleList.includes('COACH') ? 'coach' : 'member';

      const userEmail = profile.email || email;

      const user: User = {
        id: profile.userId ? String(profile.userId) : userEmail,
        email: userEmail,
        name: profile.nickname || (userEmail ? userEmail.split('@')[0] : email.split('@')[0]),
        nickname: profile.nickname,
        role: resolvedRole,
        profileImage: profile.profileImage,
      };

      localStorage.setItem('user_role', JSON.stringify(roleList));
      localStorage.setItem(CONFIG.STORAGE_KEYS.USER_INFO, JSON.stringify(user));
      
      setAuthState({
        user,
        isAuthenticated: true,
        isLoading: false,
      });
      
      console.log('✅ 로그인 성공:', response.message);
      return { success: true, message: response.message };
    } catch (error) {
      localStorage.removeItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);
      localStorage.removeItem(CONFIG.TOKEN.REFRESH_TOKEN_KEY);
      localStorage.removeItem(CONFIG.STORAGE_KEYS.USER_INFO);
      localStorage.removeItem('user_role');

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
    localStorage.removeItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);
    localStorage.removeItem(CONFIG.TOKEN.REFRESH_TOKEN_KEY);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.USER_INFO);
    localStorage.removeItem('user_role');
    localStorage.removeItem('keepLoggedIn');

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
        const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);
        const userInfo = localStorage.getItem(CONFIG.STORAGE_KEYS.USER_INFO);
        
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
