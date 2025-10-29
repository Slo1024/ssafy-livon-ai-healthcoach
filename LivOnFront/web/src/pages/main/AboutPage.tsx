import React from 'react';
import mainpage1 from '../../assets/images/mainpage1.png';

export const AboutPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-white px-4 py-16" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif' }}>
      <div className="max-w-6xl mx-auto">
        <div className="flex flex-col lg:flex-row items-center gap-12">
          {/* 텍스트 영역 */}
          <div className="flex-1 lg:pr-12">
            <h1 className="text-4xl font-bold text-gray-900 mb-8">
              더 새롭고, 더 스마트한 건강 코칭{' '}
              <span style={{ color: '#4965f6' }}>리브온</span>
            </h1>
            
            <div className="space-y-6 text-lg text-gray-700 leading-relaxed">
              <p>
                실시간 스트리밍으로 코치와 참여자가 연결되어, 개인의 건강 데이터를 기반으로 맞춤형 상담과 코칭을 제공합니다.
              </p>
              
              <p>
                운동, 영양, 통증, 멘탈케어까지, 당신에게 전문 코칭이 필요한 순간, 언제 어디서나 리브온이 함께합니다.
              </p>
            </div>
          </div>
          
          {/* 이미지 영역 */}
          <div className="flex-shrink-0">
            <img 
              src={mainpage1} 
              alt="헬스 코칭 서비스 일러스트" 
              width="500"
              height="500"
              className="w-auto h-auto"
            />
          </div>
        </div>
      </div>
    </div>
  );
};