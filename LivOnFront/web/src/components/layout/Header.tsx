import React from 'react';
import { Link } from 'react-router-dom';

interface HeaderProps {
  isAuthenticated?: boolean;
  userRole?: 'coach' | 'member';
  onLogout?: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  isAuthenticated = false,
  userRole,
  onLogout,
}) => {
  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <div className="flex-shrink-0">
            <Link to="/" className="text-xl font-bold text-blue-600">
              LivOn
            </Link>
          </div>
          
          {/* Navigation */}
          <nav className="hidden md:flex space-x-8">
            <Link to="/" className="text-gray-700 hover:text-blue-600 px-3 py-2 text-sm font-medium">
              홈
            </Link>
            <Link to="/about" className="text-gray-700 hover:text-blue-600 px-3 py-2 text-sm font-medium">
              서비스 소개
            </Link>
            <Link to="/download" className="text-gray-700 hover:text-blue-600 px-3 py-2 text-sm font-medium">
              앱 다운로드
            </Link>
          </nav>
          
          {/* User Menu */}
          <div className="flex items-center space-x-4">
            {isAuthenticated ? (
              <>
                {userRole === 'coach' && (
                  <Link to="/coach/dashboard" className="text-gray-700 hover:text-blue-600 px-3 py-2 text-sm font-medium">
                    코치 대시보드
                  </Link>
                )}
                <button
                  onClick={onLogout}
                  className="text-gray-700 hover:text-blue-600 px-3 py-2 text-sm font-medium"
                >
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link to="/auth/login" className="text-gray-700 hover:text-blue-600 px-3 py-2 text-sm font-medium">
                  로그인
                </Link>
                <Link to="/auth/signup" className="bg-blue-600 text-white hover:bg-blue-700 px-4 py-2 rounded-lg text-sm font-medium">
                  회원가입
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
