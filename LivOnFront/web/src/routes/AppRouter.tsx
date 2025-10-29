import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ROUTES } from '../constants/routes';

// 페이지 컴포넌트들
import { HomePage } from '../pages/main/HomePage';
import { AboutPage } from '../pages/main/AboutPage';
import { DownloadPage } from '../pages/main/DownloadPage';
import { LoginPage } from '../pages/auth/LoginPage';
import { SignupCompletePage } from '../pages/auth/SignupCompletePage';
import { MyPageVerificationPage } from '../pages/coach/MyPageVerificationPage';
import { NotFoundPage } from '../pages/notfound/NotFoundPage';
import { FAQPage } from '../pages/support/FAQPage';
import { InquiryPage } from '../pages/support/InquiryPage';

// 보호된 라우트 컴포넌트
import { ProtectedRoute } from './ProtectedRoute';

export const AppRouter: React.FC = () => {
  return (
    <Router>
      <Routes>
        {/* 공개 라우트 */}
        <Route path={ROUTES.HOME} element={<HomePage />} />
        <Route path={ROUTES.ABOUT} element={<AboutPage />} />
        <Route path={ROUTES.DOWNLOAD} element={<DownloadPage />} />
        
        {/* 인증 라우트 */}
        <Route path={ROUTES.LOGIN} element={<LoginPage />} />
        <Route path={ROUTES.SIGNUP} element={<div>회원가입 페이지</div>} />
        <Route path={ROUTES.TERMS} element={<div>이용약관 페이지</div>} />
        <Route path={ROUTES.EMAIL_VERIFICATION} element={<div>이메일 인증 페이지</div>} />
        <Route path={ROUTES.PROFILE_SETUP} element={<div>프로필 설정 페이지</div>} />
        <Route path={ROUTES.SIGNUP_COMPLETE} element={<SignupCompletePage />} />
        
        {/* 코치 보호된 라우트 */}
        <Route
          path={ROUTES.COACH_DASHBOARD}
          element={
            <ProtectedRoute requiredRole="coach">
              <div>코치 대시보드</div>
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.CLASS_SETUP}
          element={
            <ProtectedRoute requiredRole="coach">
              <div>클래스 개설 페이지</div>
            </ProtectedRoute>
          }
        />
        <Route
          path="/mypage/coach-verification"
          element={
            <ProtectedRoute requiredRole="coach">
              <MyPageVerificationPage />
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.RESERVATION_MANAGE}
          element={
            <ProtectedRoute requiredRole="coach">
              <div>예약 관리 페이지</div>
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.PAST_RESERVATION}
          element={
            <ProtectedRoute requiredRole="coach">
              <div>지난 예약 페이지</div>
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.STREAMING}
          element={
            <ProtectedRoute requiredRole="coach">
              <div>스트리밍 페이지</div>
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.COACH_MYPAGE}
          element={
            <ProtectedRoute requiredRole="coach">
              <div>코치 마이페이지</div>
            </ProtectedRoute>
          }
        />
        
        {/* 고객센터 라우트 */}
        <Route path="/support" element={<Navigate to={ROUTES.FAQ} replace />} />
        <Route path={ROUTES.FAQ} element={<FAQPage />} />
        <Route path={ROUTES.INQUIRY} element={<InquiryPage />} />
        
        {/* 404 페이지 */}
        <Route path={ROUTES.NOT_FOUND} element={<NotFoundPage />} />
        
        {/* 기본 리다이렉트 */}
        <Route path="*" element={<Navigate to={ROUTES.NOT_FOUND} replace />} />
      </Routes>
    </Router>
  );
};
