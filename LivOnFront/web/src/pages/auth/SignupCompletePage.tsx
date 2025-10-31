import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import styled from 'styled-components';

const PageContainer = styled.div`
  min-height: 100vh;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const ContentWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 800px;
  width: 100%;
`;

const CompletionMessage = styled.h1`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 400; /* Regular */
  font-size: 24px;
  color: #000000;
  margin: 0 0 20px 0;
`;

const WelcomeMessage = styled.h2`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 700; /* Bold */
  font-size: 32px;
  color: #000000;
  margin: 0 0 60px 0;
`;

const InstructionText = styled.p`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 300; /* Light */
  font-size: 25px;
  color: #000000;
  margin: 0 0 80px 0;
  line-height: 1.4;
`;

const HighlightText = styled.span`
  font-weight: 600; /* SemiBold */
`;

const ButtonContainer = styled.div`
  display: flex;
  gap: 32px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;

  @media (max-width: 768px) {
    flex-direction: column;
    gap: 20px;
  }
`;

const ActionButton = styled.button`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 800; /* ExtraBold */
  font-size: 25px;
  color: #ffffff;
  background-color: #4965f6;
  border: none;
  border-radius: 20px;
  width: 500px;
  height: 81px;
  cursor: pointer;
  transition: background-color 0.3s ease;

  &:hover {
    background-color: #3d54d4;
  }

  &:active {
    background-color: #2d3fa9;
  }

  @media (max-width: 768px) {
    width: 100%;
    max-width: 400px;
  }
`;

export const SignupCompletePage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  // 회원가입 시 전달된 닉네임 가져오기
  const nickname = location.state?.nickname || '코치';

  const handleLoginClick = () => {
    navigate('/auth/login');
  };

  const handleVerificationClick = () => {
    navigate('/mypage/coach-verification');
  };

  return (
    <PageContainer>
      <ContentWrapper>
        <CompletionMessage>
          회원가입이 완료되었습니다.
        </CompletionMessage>
        
        <WelcomeMessage>
          {nickname} 코치님, 환영합니다!
        </WelcomeMessage>
        
        <InstructionText>
          코치 인증 여부는 <HighlightText>'마이페이지 - 코치 인증 여부'</HighlightText>를 통해 확인하시기 바랍니다.
        </InstructionText>
        
        <ButtonContainer>
          <ActionButton onClick={handleLoginClick}>
            로그인 바로가기
          </ActionButton>
          <ActionButton onClick={handleVerificationClick}>
            코치 인증 여부 확인하기
          </ActionButton>
        </ButtonContainer>
      </ContentWrapper>
    </PageContainer>
  );
};