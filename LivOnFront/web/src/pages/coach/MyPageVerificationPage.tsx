import React, { useState } from 'react';
import { useLocation } from 'react-router-dom';
import styled from 'styled-components';
import coachverification1 from '../../assets/images/coachverification1.png';
import coachverification2 from '../../assets/images/coachverification2.png';

const PageContainer = styled.div`
  min-height: 100vh;
  background-color: #ffffff;
  padding: 40px 20px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const ContentWrapper = styled.div`
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 40px;

  @media (min-width: 768px) {
    flex-direction: row;
    align-items: flex-start;
  }
`;

const MainContent = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 30px;
`;

const PageTitle = styled.h1`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 700;
  font-size: 40px;
  color: #000000;
  margin: 0;
`;

const TabContainer = styled.div`
  display: flex;
  gap: 0;
  margin-bottom: 0;
`;

const TabButton = styled.button<{ active: boolean }>`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 500;
  font-size: 16px;
  width: 100px;
  height: 48px;
  border: 1px solid #4965f6;
  background-color: ${props => props.active ? '#4965f6' : '#ffffff'};
  color: ${props => props.active ? '#ffffff' : '#4965f6'};
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 0;
  
  &:first-child {
    border-radius: 6px 0 0 6px;
  }
  
  &:last-child {
    border-radius: 0 6px 6px 0;
    border-left: none;
  }

  &:hover {
    background-color: ${props => props.active ? '#4965f6' : '#f7fafc'};
  }
`;

const DividerLine = styled.div`
  width: 100%;
  height: 2px;
  background-color: #4965f6;
  margin: 0;
`;

const StatusMessage = styled.h2`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 700;
  font-size: 32px;
  color: #000000;
  margin: 40px 0 20px 0;
  text-align: center;
  line-height: 1.4;
`;

const DescriptionMessage = styled.p`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 300;
  font-size: 30px;
  color: #000000;
  margin: 0 0 40px 0;
  text-align: center;
  line-height: 1.5;
`;

const ImageContainer = styled.div`
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-top: 40px;
  flex-wrap: wrap;

  @media (max-width: 768px) {
    gap: 20px;
  }
`;

const ImageWrapper = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
  min-width: 150px;

  img {
    max-width: 200px;
    height: auto;
    width: 100%;
  }

  @media (max-width: 768px) {
    flex: 0 0 auto;
    min-width: 120px;
  }
`;

export const MyPageVerificationPage: React.FC = () => {
  const location = useLocation();
  const [activeTab, setActiveTab] = useState<'info' | 'verification'>('verification');
  
  // 회원가입 시 전달된 데이터 가져오기 (실제로는 API에서 가져와야 함)
  const nickname = location.state?.nickname || '코치';
  const jobField = location.state?.job || '운동/피트니스';
  const isVerified = location.state?.isVerified || false; // 실제로는 API에서 가져와야 함

  const handleTabClick = (tab: 'info' | 'verification') => {
    setActiveTab(tab);
    if (tab === 'info') {
      // MyPageInfoPage로 이동
      window.location.href = '/mypage/coach-info';
    }
  };

  return (
    <PageContainer>
      <MainContent>
        <PageTitle>{nickname} 코치님 마이페이지</PageTitle>
        
        <TabContainer>
          <TabButton 
            active={activeTab === 'info'} 
            onClick={() => handleTabClick('info')}
          >
            코치님 정보
          </TabButton>
          <TabButton 
            active={activeTab === 'verification'} 
            onClick={() => handleTabClick('verification')}
          >
            코치 인증 여부
          </TabButton>
        </TabContainer>
        
        <DividerLine />
        
        {isVerified ? (
          <>
            <StatusMessage>
              코치님의 전문가 인증이<br />
              성공적으로 완료되었습니다!
            </StatusMessage>
            <DescriptionMessage>
              이제 코치님은 리브온의 검증된 {jobField} 분야 전문가로서<br />
              회원들을 위한 편리한 건강 코칭을 시작할 수 있습니다.
            </DescriptionMessage>
          </>
        ) : (
          <>
            <StatusMessage>
              코치님의 전문가 인증이<br />
              아직 진행 중입니다.
            </StatusMessage>
            <DescriptionMessage>
              리브온의 전문가 인증을 받기까지<br />
              조금만 더 기다려 주세요.
            </DescriptionMessage>
          </>
        )}
        
        <ImageContainer>
          <ImageWrapper>
            <img src={coachverification1} alt="코치 인증 일러스트 1" />
          </ImageWrapper>
          <ImageWrapper>
            <img src={coachverification2} alt="코치 인증 일러스트 2" />
          </ImageWrapper>
        </ImageContainer>
      </MainContent>
    </PageContainer>
  );
};