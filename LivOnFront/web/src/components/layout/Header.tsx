import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import styled from 'styled-components';
import { useAuth } from '../../hooks/useAuth';

const AuthButton = styled.button`
  appearance: none;
  background-color: #ffffff;
  border: 1px solid #4965f6;
  border-radius: 8px;
  box-sizing: border-box;
  color: #4965f6;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
  font-size: 14px;
  font-weight: 600;
  line-height: normal;
  width: 96px;
  height: 42px;
  outline: none;
  text-align: center;
  text-decoration: none;
  transition: all 300ms cubic-bezier(.23, 1, 0.32, 1);
  user-select: none;
  -webkit-user-select: none;
  touch-action: manipulation;
  will-change: transform;

  &:disabled {
    pointer-events: none;
    opacity: 0.6;
  }

  &:hover {
    color: #ffffff;
    background-color: #4965f6;
  }

  &:active {
    transform: none;
  }
`;

const MobileMenuWrapper = styled.div`
  display: none;

  @media (max-width: 1200px) {
    display: block;
  }

  .mobile-menu-input {
    display: none;
  }

  .mobile-menu-label {
    display: none;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    cursor: pointer;
    position: relative;
  }

  .hamburger-bar {
    width: 25px;
    height: 3px;
    background-color: #4965f6;
    border-radius: 2px;
    transition: all 0.3s ease;
    position: relative;
  }

  .hamburger-bar::before,
  .hamburger-bar::after {
    content: "";
    position: absolute;
    width: 25px;
    height: 3px;
    background-color: #4965f6;
    border-radius: 2px;
    transition: all 0.3s ease;
  }

  .hamburger-bar::before {
    top: -8px;
  }

  .hamburger-bar::after {
    top: 8px;
  }

  .mobile-menu-input:checked + .mobile-menu-label .hamburger-bar {
    background-color: transparent;
  }

  .mobile-menu-input:checked + .mobile-menu-label .hamburger-bar::before {
    transform: rotate(45deg);
    top: 0;
  }

  .mobile-menu-input:checked + .mobile-menu-label .hamburger-bar::after {
    transform: rotate(-45deg);
    top: 0;
  }

  .mobile-menu-container {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background-color: white;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    padding: 10px 0;
    transform: translateY(-10px);
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s ease;
    z-index: 1000;
  }

  .mobile-menu-input:checked ~ .mobile-menu-container {
    transform: translateY(0);
    opacity: 1;
    visibility: visible;
  }

  .mobile-menu-item {
    display: block;
    padding: 12px 20px;
    color: #374151;
    text-decoration: none;
    font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
      Roboto, sans-serif;
    font-weight: 500;
    font-size: 16px;
    transition: background-color 0.2s;
    outline: none;
    border: none;
  }

  .mobile-menu-item:hover {
    background-color: #f3f4f6;
    color: #4965f6;
  }

  .mobile-menu-item:focus {
    outline: none;
    border: none;
    box-shadow: none;
  }

  .mobile-menu-item:active {
    outline: none;
    border: none;
    box-shadow: none;
  }

  @media (max-width: 1200px) {
    .mobile-menu-label {
      display: flex;
    }
  }
`;

const DesktopNav = styled.nav`
  display: flex;
  flex-direction: row;
  gap: clamp(20px, 4vw, 70px);
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
  flex: 1 1 auto;

  @media (max-width: 1200px) {
    display: none;
  }
`;

const DesktopNavLink = styled(Link)`
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 500;
  font-size: 16px;
  color: #4a5568;
  text-decoration: none;
  white-space: nowrap;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.2s;
  outline: none;
  border: none;

  &:hover {
    color: #4965f6;
    background-color: transparent;
    text-decoration: none;
  }

  &:focus {
    outline: none;
    border: none;
    box-shadow: none;
    text-decoration: none;
  }

  &:active {
    outline: none;
    border: none;
    box-shadow: none;
    text-decoration: none;
  }

  &.active {
    font-weight: 800;
    font-size: 20px;
    color: #000000;
  }
`;

const WelcomeMessage = styled.div`
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: clamp(14px, 1.8vw, 20px);
  color: #000000;
  white-space: nowrap;
`;

interface HeaderProps {
  isAuthenticated?: boolean;
  userRole?: "coach" | "member";
  onLogout?: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  isAuthenticated: propIsAuthenticated,
  userRole: propUserRole,
  onLogout: propOnLogout,
}) => {
  const location = useLocation();
  const { isAuthenticated, user, logout } = useAuth();

  // Props가 제공되면 props 우선 사용, 없으면 useAuth hook 사용
  const finalIsAuthenticated =
    propIsAuthenticated !== undefined ? propIsAuthenticated : isAuthenticated;
  const finalUserRole = propUserRole || user?.role;
  const handleLogout = propOnLogout || logout;

  const getLinkStyle = (path: string) => {
    const isActive = location.pathname === path;
    return {
      fontFamily:
        'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      fontWeight: isActive ? 800 : 500,
      fontSize: isActive ? "20px" : "16px",
      color: isActive ? "#000000" : "#4a5568",
      textDecoration: "none",
    };
  };

  const navItems = [
    { path: "/about", label: "서비스 소개", activePaths: ["/about"] },
    {
      path: "/reservations",
      label: "예약 현황",
      activePaths: ["/reservations", "/coach/past-reservation"],
    },
    {
      path: "/classes",
      label: "나의 클래스",
      activePaths: ["/classes", "/coach/class-setup"],
    },
    {
      path: "/support/faq",
      label: "고객센터",
      activePaths: ["/support/faq", "/support/inquiry"],
    },
    { path: "/download", label: "앱 다운로드", activePaths: ["/download"] },
    {
      path: "/coach/mypage/info",
      label: "마이페이지",
      activePaths: ["/coach/mypage/info"],
    },
  ];

  return (
    <header
      className="bg-white shadow-sm border-b"
      style={{
        width: "100%",
        height: "79px",
        margin: "0 auto",
        boxSizing: "border-box",
        position: "relative",
      }}
    >
      <div
        style={{
          width: "100%",
          maxWidth: "100%",
          height: "100%",
          display: "flex",
          flexDirection: "row",
          alignItems: "center",
          justifyContent: "space-between",
          padding: "0 20px",
          boxSizing: "border-box",
          overflow: "hidden",
        }}
      >
        {/* Logo */}
        <div style={{ flexShrink: 0 }}>
          <Link
            to="/"
            style={{
              fontFamily:
                'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
              fontWeight: 600,
              fontSize: "40px",
              color: "#4965f6",
              textDecoration: "none",
              whiteSpace: "nowrap",
              outline: "none",
              border: "none",
            }}
            onFocus={(e) => (e.target.style.outline = "none")}
            onBlur={(e) => (e.target.style.outline = "none")}
          >
            LIVON
          </Link>
        </div>

        {/* Desktop Navigation */}
        <DesktopNav>
          {navItems.map((item) => {
            const isActive =
              item.activePaths?.includes(location.pathname) ||
              location.pathname === item.path;
            return (
              <DesktopNavLink
                key={item.path}
                to={item.path}
                className={isActive ? "active" : ""}
              >
                {item.label}
              </DesktopNavLink>
            );
          })}
        </DesktopNav>

        {/* Right Section - Mobile Menu + User Menu */}
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: "15px",
            flexShrink: 0,
          }}
        >
          {/* Mobile Menu */}
          <MobileMenuWrapper>
            <input
              type="checkbox"
              id="mobile-menu"
              className="mobile-menu-input"
            />
            <label htmlFor="mobile-menu" className="mobile-menu-label">
              <div className="hamburger-bar"></div>
            </label>
            <div className="mobile-menu-container">
              {navItems.map((item) => (
                <Link
                  key={item.path}
                  to={item.path}
                  className="mobile-menu-item"
                  onClick={() => {
                    const checkbox = document.getElementById(
                      "mobile-menu"
                    ) as HTMLInputElement;
                    if (checkbox) checkbox.checked = false;
                  }}
                >
                  {item.label}
                </Link>
              ))}
            </div>
          </MobileMenuWrapper>

          {/* User Menu */}
          {finalIsAuthenticated ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              {finalUserRole === 'coach' && user?.nickname && (
                <WelcomeMessage>
                  {user.nickname} 코치님, 환영합니다.
                </WelcomeMessage>
              )}
              {finalUserRole === 'coach' && !user?.nickname && (
                <Link
                  to="/coach/dashboard"
                  className="text-gray-700 hover:text-blue-600 px-3 py-2 text-sm font-medium"
                >
                  코치 대시보드
                </Link>
              )}
              <AuthButton type="button" onClick={handleLogout}>
                로그아웃
              </AuthButton>
            </div>
          ) : (
            <Link to="/auth/login" style={{ textDecoration: 'none' }}>
              <AuthButton type="button">로그인</AuthButton>
            </Link>
          )}
        </div>
      </div>
    </header>
  );
};
