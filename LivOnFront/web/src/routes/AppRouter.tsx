import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ROUTES } from '../constants/routes';
import { Layout } from '../components/layout/Layout';

// 페이지 컴포넌트들
import { HomePage } from '../pages/main/HomePage';
import { AboutPage } from '../pages/main/AboutPage';
import { DownloadPage } from '../pages/main/DownloadPage';
import { LoginPage } from '../pages/auth/LoginPage';
import { SignupPage } from '../pages/auth/SignupPage';
import { TermsPage } from '../pages/main/TermsPage';
import { SignupCompletePage } from '../pages/auth/SignupCompletePage';
import { MyPageVerificationPage } from '../pages/coach/MyPageVerificationPage';
import { MyPageInfoPage } from '../pages/coach/MyPageInfoPage';
import { ClassListPage } from '../pages/coach/ClassListPage';
import { ClassSetupPage } from '../pages/coach/ClassSetupPage';
import { ReservationListPage } from '../pages/coach/ReservationListPage';
import { PastReservationPage } from '../pages/coach/PastReservationPage';
import { StreamingPage } from '../pages/coach/StreamingPage';
import { NotFoundPage } from '../pages/notfound/NotFoundPage';
import { FAQPage } from '../pages/support/FAQPage';
import { InquiryPage } from '../pages/support/InquiryPage';

// 보호된 라우트 컴포넌트
import { ProtectedRoute } from './ProtectedRoute';

export const AppRouter: React.FC = () => {
  return (
    <Router>
      <Layout>
      <Routes>
        {/* 공개 라우트 */}
        <Route path={ROUTES.HOME} element={<HomePage />} />
        <Route path={ROUTES.ABOUT} element={<AboutPage />} />
        <Route path={ROUTES.DOWNLOAD} element={<DownloadPage />} />
        
        {/* 인증 라우트 */}
        <Route path={ROUTES.LOGIN} element={<LoginPage />} />
        <Route path={ROUTES.SIGNUP} element={<SignupPage />} />
        <Route path={ROUTES.TERMS} element={<TermsPage />} />
        {/* 호환용(기존 App.tsx 경로) */}
        <Route path="/terms" element={<TermsPage />} />
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
          path={ROUTES.CLASS_LIST}
          element={
            <ProtectedRoute requiredRole="coach">
              <ClassListPage />
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.CLASS_SETUP}
          element={
            <ProtectedRoute requiredRole="coach">
              <ClassSetupPage />
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
              <PastReservationPage />
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.STREAMING}
          element={
            <ProtectedRoute requiredRole="coach">
              <StreamingPage />
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
        <Route
          path={ROUTES.COACH_MYPAGE_INFO}
          element={
            <ProtectedRoute requiredRole="coach">
              <MyPageInfoPage />
            </ProtectedRoute>
          }
        />
        
        {/* 예약 현황 라우트 */}
        <Route
          path={ROUTES.RESERVATION_LIST}
          element={
            <ProtectedRoute requiredRole="coach">
              <ReservationListPage />
            </ProtectedRoute>
          }
        />
        
        {/* 고객센터 라우트 */}
        <Route path="/support" element={<Navigate to={ROUTES.FAQ} replace />} />
        <Route path={ROUTES.FAQ} element={<FAQPage />} />
        <Route path={ROUTES.INQUIRY} element={<InquiryPage />} />
        
        {/* 예외 처리: /dashboard 경로는 404로 리다이렉트하지 않음 */}
        <Route path="/dashboard" element={null} />
        
        {/* 404 페이지 */}
        <Route path={ROUTES.NOT_FOUND} element={<NotFoundPage />} />
        
        {/* 기본 리다이렉트 */}
        <Route path="*" element={<Navigate to={ROUTES.NOT_FOUND} replace />} />
      </Routes>
      </Layout>
    </Router>
  );
};
