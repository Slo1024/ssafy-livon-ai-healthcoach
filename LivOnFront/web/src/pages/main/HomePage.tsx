import React from 'react';
import { Header } from '../../components/layout/Header';
import { Footer } from '../../components/layout/Footer';
import { Button } from '../../components/common/Button';

export const HomePage: React.FC = () => {
  return (
    <div className="min-h-screen flex flex-col" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif' }}>
      <Header />
      
      <div className="flex-1 flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">LIVON</h1>
          <p className="text-xl text-gray-600 mb-8">오늘도 당신 곁에서, 건강을 코칭합니다.</p>
          <p className="text-xl text-gray-600 mb-8">실시간 맞춤형 헬스 케어 리브온</p>
        </div>
      </div>
      
      <Footer />
    </div>
  );
};