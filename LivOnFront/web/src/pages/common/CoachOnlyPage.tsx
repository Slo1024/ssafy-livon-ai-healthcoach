import React from "react";
import styled from "styled-components";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "../../constants/routes";

const PageContainer = styled.div`
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  padding: 40px 20px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
`;

const Card = styled.div`
  width: 100%;
  max-width: 520px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
`;

const Title = styled.h1`
  margin: 0 0 12px 0;
  font-weight: 800;
  font-size: 24px;
  color: #111827;
`;

const Message = styled.p`
  margin: 0 0 24px 0;
  font-size: 16px;
  color: #6b7280;
  line-height: 1.6;
`;

const HomeButton = styled.button`
  width: 100%;
  height: 48px;
  background-color: #4965f6;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;

  &:hover {
    background-color: #3b5dd8;
  }
`;

const CoachOnlyPage: React.FC = () => {
  const navigate = useNavigate();
  return (
    <PageContainer>
      <Card>
        <Title>코치 전용 서비스입니다</Title>
        <Message>
          현재 페이지는 코치 권한이 필요한 페이지입니다.
          <br />
          홈으로 돌아가 다른 서비스를 이용해 주세요.
        </Message>
        <HomeButton onClick={() => navigate(ROUTES.HOME)}>홈으로</HomeButton>
      </Card>
    </PageContainer>
  );
};

export default CoachOnlyPage;
