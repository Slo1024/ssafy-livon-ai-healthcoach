import React from 'react';
import { useLocation } from 'react-router-dom';
import { Header } from './Header';
import { Footer } from './Footer';
import { ROUTES } from '../../constants/routes';

interface LayoutProps {
  children: React.ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  const location = useLocation();
  const isStreamingPage = location.pathname === ROUTES.STREAMING;
  const isCallbackPage = location.pathname.includes('/auth/callback');

  if (isStreamingPage || isCallbackPage) {
    return <>{children}</>;
  }

  return (
    <div className="min-h-screen flex flex-col" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif' }}>
      <Header />
      <main className="flex-1">
        {children}
      </main>
      <Footer />
    </div>
  );
};
