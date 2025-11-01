import React, { useState, useEffect } from 'react';
import styled, { keyframes } from 'styled-components';
import mainpage1 from '../../assets/images/mainpage1.png';
import mainpage2 from '../../assets/images/mainpage2.png';

const fadeInUp = keyframes`
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
`;

const AnimatedText = styled.div<{ visible: boolean }>`
  opacity: 0;
  animation: ${props => props.visible ? fadeInUp : 'none'} 0.8s ease-out forwards;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 700;
  font-size: 32px;
  color: #1f2937;
  text-align: center;
  line-height: 1.5;
`;

const HighlightText = styled.span`
  color: #4965f6;
`;

export const HomePage: React.FC = () => {
  const [firstVisible, setFirstVisible] = useState(false);
  const [secondVisible, setSecondVisible] = useState(false);

  useEffect(() => {
    // 첫 번째 텍스트 즉시 표시
    setFirstVisible(true);
    
    // 1초 후 두 번째 텍스트 표시
    const timer = setTimeout(() => {
      setSecondVisible(true);
    }, 1000);

    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="min-h-screen flex flex-col bg-white" style={{ padding: 0, margin: 0 }}>
      {/* 메인 텍스트 영역 */}
      <div className="flex-1 flex items-center justify-center" style={{ paddingTop: '80px', paddingBottom: '40px' }}>
        <div className="text-center w-full" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <h1 className="mb-4" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 700, fontSize: '32px', color: '#1f2937', textAlign: 'center', display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <AnimatedText visible={firstVisible}>
              오늘도 당신 곁에서, 건강을 코칭합니다.
            </AnimatedText>
            <AnimatedText visible={secondVisible}>
              실시간 맞춤형 헬스 케어{' '}
              <HighlightText>리브온</HighlightText>
            </AnimatedText>
          </h1>
        </div>
      </div>
      
      {/* 이미지 영역 - 화면 하단을 꽉 채움 */}
      <div style={{ display: 'flex', flexDirection: 'row', width: '100%', marginTop: 'auto', paddingBottom: '79px', flexWrap: 'nowrap', alignItems: 'flex-end' }}>
        <div style={{ width: '50%', flexShrink: 0, display: 'flex', alignItems: 'flex-end', justifyContent: 'center', backgroundColor: 'white', overflow: 'hidden' }}>
          <img 
            src={mainpage1} 
            alt="헬스 코칭 서비스 일러스트 1" 
            style={{ width: '100%', height: 'auto', display: 'block', maxWidth: '100%' }}
          />
        </div>
        <div style={{ width: '50%', flexShrink: 0, display: 'flex', alignItems: 'flex-end', justifyContent: 'center', backgroundColor: 'white', overflow: 'hidden' }}>
          <img 
            src={mainpage2} 
            alt="헬스 코칭 서비스 일러스트 2" 
            style={{ width: '100%', height: 'auto', display: 'block', maxWidth: '100%' }}
          />
        </div>
      </div>
    </div>
  );
};