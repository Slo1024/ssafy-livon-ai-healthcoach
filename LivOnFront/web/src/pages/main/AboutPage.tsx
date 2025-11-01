import React, { useState, useEffect } from 'react';
import styled, { keyframes } from 'styled-components';
import mainpage1 from '../../assets/images/mainpage1.png';

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

const AnimatedText = styled.p<{ visible: boolean }>`
  opacity: 0;
  animation: ${props => props.visible ? fadeInUp : 'none'} 0.8s ease-out forwards;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 40px;
  color: #374151;
  line-height: 1.5;
  margin: 0;
`;

const PageContainer = styled.div`
  min-height: 100vh;
  background-color: #ffffff;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  position: relative;
  padding: 40px 20px;
  padding-bottom: 40px;
  
  @media (min-width: 768px) {
    padding-bottom: 100px;
  }
`;

const ContentWrapper = styled.div`
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  
  @media (min-width: 768px) {
    flex-direction: row;
    align-items: flex-start;
  }
`;

const TextSection = styled.div`
  max-width: 800px;
  width: 100%;
  margin-bottom: 40px;
  
  @media (min-width: 768px) {
    margin-bottom: 0;
  }
`;

const ImageContainer = styled.div`
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
  
  @media (min-width: 768px) {
    position: absolute;
    bottom: 0;
    right: 5%;
    width: auto;
    margin-top: 0;
    pointer-events: none;
  }
  
  img {
    width: 100%;
    max-width: 500px;
    height: auto;
    
    @media (min-width: 768px) {
      width: auto;
      max-width: 500px;
      max-height: 500px;
    }
  }
`;

export const AboutPage: React.FC = () => {
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
    <PageContainer>
      <ContentWrapper>
        <TextSection>
          <h1 className="text-4xl font-bold text-gray-900 mb-8">
            더 새롭고, 더 스마트한 건강 코칭{' '}
            <span style={{ color: '#4965f6' }}>리브온</span>
          </h1>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            <AnimatedText visible={firstVisible} style={{ fontWeight: 400 }}>
              실시간 스트리밍으로 코치와 참여자가 연결되어,<br />
              개인의 건강 데이터를 기반으로<br />
              맞춤형 상담과 코칭을 제공합니다.
            </AnimatedText>
            
            <AnimatedText visible={secondVisible} style={{ fontWeight: 500 }}>
              운동, 영양, 통증, 멘탈케어까지,<br />
              당신에게 전문 코칭이 필요한 순간,<br />
              언제 어디서나 리브온이 함께합니다.
            </AnimatedText>
          </div>
        </TextSection>
        
        <ImageContainer>
          <img 
            src={mainpage1} 
            alt="헬스 코칭 서비스 일러스트" 
          />
        </ImageContainer>
      </ContentWrapper>
    </PageContainer>
  );
};