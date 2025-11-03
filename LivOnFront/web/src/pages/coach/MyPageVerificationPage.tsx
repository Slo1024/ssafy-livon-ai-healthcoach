import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ROUTES } from '../../constants/routes';
import styled from 'styled-components';
import { SegmentedTabs } from '../../components/common/Button';
import coachverification1 from '../../assets/images/coachverification1.png';
import coachverification2 from '../../assets/images/coachverification2.png';

const PageContainer = styled.div`
  width: 100%;
  max-width: 1080px;
  margin: 0 auto;
  padding: 30px 20px 60px;
  box-sizing: border-box;
`;

const PageTitle = styled.h1`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 700;
  font-size: 40px;
  color: #000;
  margin: 0 0 16px 0;
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
  flex-direction: row;
  width: 100%;
  margin-top: 40px;
  flex-wrap: nowrap;
  align-items: flex-end;
  justify-content: space-between;
`;

const ImageWrapper = styled.div`
  flex-shrink: 0;
  display: flex;
  align-items: flex-end;
  justify-content: flex-start;
  background-color: white;
  overflow: hidden;

  &:first-child {
    justify-content: flex-start;
  }

  &:last-child {
    justify-content: flex-end;
  }

  img {
    width: auto;
    height: auto;
    display: block;
    max-height: 400px;
  }

  @media (max-width: 768px) {
    img {
      max-height: 300px;
    }
  }
`;

export const MyPageVerificationPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  // 회원가입 시 전달된 데이터 가져오기 (실제로는 API에서 가져와야 함)
  const nickname = location.state?.nickname;
  const jobField = location.state?.job || '운동/피트니스';

  const handleInfoClick = () => {
    navigate(ROUTES.COACH_MYPAGE_INFO);
  };

  const handleVerificationClick = () => {
    // 이미 verification 페이지에 있으므로 아무 동작 안함
  };

  return (
    <PageContainer>
      <PageTitle>{nickname ? `${nickname} 코치님 마이페이지` : '코치님 마이페이지'}</PageTitle>
      
      <SegmentedTabs
        leftLabel="코치님 정보"
        rightLabel="코치 인증 여부"
        active="right"
        onLeftClick={handleInfoClick}
        onRightClick={handleVerificationClick}
        tabWidth={120}
      />
      
      <StatusMessage>
        코치님의 전문가 인증이<br />
        성공적으로 완료되었습니다!
      </StatusMessage>
      <DescriptionMessage>
        이제 코치님은 리브온의 검증된 {jobField} 분야 전문가로서<br />
        회원들을 위한 편리한 건강 코칭을 시작할 수 있습니다.
      </DescriptionMessage>
      
      <ImageContainer>
        <ImageWrapper>
          <img src={coachverification1} alt="코치 인증 일러스트 1" />
        </ImageWrapper>
        <ImageWrapper>
          <img src={coachverification2} alt="코치 인증 일러스트 2" />
        </ImageWrapper>
      </ImageContainer>
    </PageContainer>
  );
};