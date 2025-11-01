import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { ROUTES } from '../constants/routes';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: 'coach' | 'member';
  redirectTo?: string;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredRole,
  redirectTo = ROUTES.LOGIN,
}) => {
  // 개발 단계에서는 인증을 우회하여 모든 화면 작업을 진행할 수 있도록 허용합니다.
  // 실제 배포 시에는 아래 플래그를 false 로 변경하거나 제거하세요.
  const BYPASS_AUTH_FOR_SCREEN_BUILD = true;
  if (BYPASS_AUTH_FOR_SCREEN_BUILD) {
    return <>{children}</>;
  }

  const { isAuthenticated, user, isLoading } = useAuth();
  const location = useLocation();

  // 로딩 중일 때
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  // 인증되지 않은 경우
  if (!isAuthenticated) {
    return <Navigate to={redirectTo} state={{ from: location }} replace />;
  }

  // 역할이 필요한 경우
  if (requiredRole && user?.role !== requiredRole) {
    // 권한이 없는 경우 홈으로 리다이렉트
    return <Navigate to={ROUTES.HOME} replace />;
  }

  return <>{children}</>;
};
