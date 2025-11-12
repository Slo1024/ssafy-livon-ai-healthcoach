import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
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
`;

const PageTitle = styled.h1`
  font-weight: 700;
  font-size: 40px;
  color: #000000;
  margin: 0 0 24px 0;
  text-align: left;

  @media (max-width: 1200px) {
    text-align: center;
    font-size: 34px;
  }

  @media (max-width: 900px) {
    font-size: 30px;
  }

  @media (max-width: 768px) {
    font-size: 26px;
  }

  @media (max-width: 480px) {
    font-size: 24px;
  }
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
  width: 100%;
  margin-top: 40px;
  flex-wrap: nowrap;
  align-items: flex-end;
  justify-content: center;
  gap: clamp(16px, 3vw, 32px);
`;

const ImageWrapper = styled.div`
  flex: 1 1 0;
  min-width: 0;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background-color: white;
  overflow: hidden;

  img {
    width: clamp(220px, 35vw, 420px);
    height: auto;
    display: block;
  }

  @media (max-width: 600px) {
    img {
      width: clamp(180px, 48vw, 320px);
    }
  }
`;

const ButtonRow = styled.div`
  display: flex;
  justify-content: center;
  margin-top: 40px;
`;

const ActionButton = styled.button`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 800;
  font-size: 20px;
  color: #ffffff;
  background-color: #4965f6;
  border: none;
  border-radius: 16px;
  width: clamp(180px, 20vw, 320px);
  height: clamp(48px, 7vw, 64px);
  cursor: pointer;
  transition: background-color 0.3s ease;

  &:hover {
    background-color: #3d54d4;
  }

  &:active {
    background-color: #2d3fa9;
  }

  @media (max-width: 768px) {
    width: clamp(150px, 40vw, 260px);
    height: clamp(44px, 10vw, 58px);
    font-size: 18px;
  }
`;

export const MyPageVerificationPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  // 회원가입 시 전달된 데이터 가져오기 (실제로는 API에서 가져와야 함)
  const nickname = location.state?.nickname;
  const jobField = location.state?.job || '운동/피트니스';

  return (
    <PageContainer>
      <ContentWrapper>
        <PageTitle>{nickname ? `${nickname} 코치님 마이페이지` : '코치님 마이페이지'}</PageTitle>
      
      <StatusMessage>
        코치님의 정보가<br />
        수정되었습니다.
      </StatusMessage>

      <ButtonRow>
        <ActionButton onClick={() => navigate('/')}>홈 바로가기</ActionButton>
      </ButtonRow>
      
      <ImageContainer>
        <ImageWrapper>
          <img src={coachverification1} alt="코치 인증 일러스트 1" />
        </ImageWrapper>
        <ImageWrapper>
          <img src={coachverification2} alt="코치 인증 일러스트 2" />
        </ImageWrapper>
      </ImageContainer>
      </ContentWrapper>
    </PageContainer>
  );
};