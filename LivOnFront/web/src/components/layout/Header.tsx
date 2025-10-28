import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import styled from 'styled-components';

const StyledLoginButton = styled.div`
  button {
    appearance: none;
    background-color: transparent;
    border: 0.125em solid #4965f6;
    border-radius: 5px;
    box-sizing: border-box;
    color: #4965f6;
    cursor: pointer;
    display: inline-block;
    font-family: Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
    font-size: 14px;
    font-weight: 600;
    line-height: normal;
    margin: 0;
    width: 87px;
    height: 42px;
    outline: none;
    text-align: center;
    text-decoration: none;
    transition: all 300ms cubic-bezier(.23, 1, 0.32, 1);
    user-select: none;
    -webkit-user-select: none;
    touch-action: manipulation;
    will-change: transform;
  }

  button:disabled {
    pointer-events: none;
  }

  button:hover {
    color: #fff;
    background-color: #4965f6;
    box-shadow: rgba(73, 101, 246, 0.25) 0 8px 15px;
    transform: translateY(-2px);
  }

  button:active {
    box-shadow: none;
    transform: translateY(0);
  }
`;

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
  const location = useLocation();
  
  const getLinkStyle = (path: string) => {
    const isActive = location.pathname === path;
    return {
      fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      fontWeight: isActive ? 800 : 500,
      fontSize: isActive ? '20px' : '16px',
      color: isActive ? '#000000' : '#4a5568',
      textDecoration: 'none'
    };
  };

  return (
    <header className="bg-white shadow-sm border-b" style={{ width: '1312px', height: '79px', margin: '0 auto' }}>
      <div style={{ width: '100%', height: '100%', display: 'flex', flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', padding: '0 20px' }}>
        {/* Logo */}
        <div style={{ flexShrink: 0 }}>
          <Link to="/" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 600, fontSize: '40px', color: '#4965f6', textDecoration: 'none' }}>
            LIVON
          </Link>
        </div>
        
        {/* Navigation */}
        <nav style={{ display: 'flex', flexDirection: 'row', gap: '70px', alignItems: 'center' }}>
            <Link 
              to="/about" 
              style={getLinkStyle('/about')}
              className="hover:text-blue-600 px-3 py-2 transition-all duration-200"
            >
              서비스 소개
            </Link>
            <Link 
              to="/reservations" 
              style={getLinkStyle('/reservations')}
              className="hover:text-blue-600 px-3 py-2 transition-all duration-200"
            >
              예약 현황
            </Link>
            <Link 
              to="/classes" 
              style={getLinkStyle('/classes')}
              className="hover:text-blue-600 px-3 py-2 transition-all duration-200"
            >
              나의 클래스
            </Link>
            <Link 
              to="/support" 
              style={getLinkStyle('/support')}
              className="hover:text-blue-600 px-3 py-2 transition-all duration-200"
            >
              고객센터
            </Link>
            <Link 
              to="/download" 
              style={getLinkStyle('/download')}
              className="hover:text-blue-600 px-3 py-2 transition-all duration-200"
            >
              앱 다운로드
            </Link>
            <Link 
              to="/mypage" 
              style={getLinkStyle('/mypage')}
              className="hover:text-blue-600 px-3 py-2 transition-all duration-200"
            >
              마이페이지
            </Link>
        </nav>
        
        {/* User Menu */}
        <div style={{ display: 'flex', alignItems: 'center', flexShrink: 0 }}>
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
              <StyledLoginButton>
                <Link to="/auth/login">
                  <button>로그인</button>
                </Link>
              </StyledLoginButton>
            )}
        </div>
      </div>
    </header>
  );
};
