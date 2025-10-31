import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';

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
  font-weight: 700; /* Pretendard Bold */
  font-size: 40px;
  color: #000000;
  margin: 0 0 24px 0;
`;

const Tabs = styled.div`
  display: flex;
  gap: 0;
  
  /* 두 버튼이 맞닿도록 인접 모서리 처리 */
  button {
    border-radius: 6px;
  }
  button:last-child {
    margin-left: -1px; /* 테두리 겹치기 */
  }
`;

const PrimaryTabButton = styled.button`
  width: 120px;
  height: 48px;
  border: 1px solid #4965f6;
  background-color: #4965f6;
  color: #ffffff;
  font-weight: 500; /* Pretendard Medium */
  font-size: 16px;
  cursor: pointer;
  border-radius: 6px;
  white-space: nowrap;
`;

const OutlineTabButton = styled.button`
  width: 120px;
  height: 48px;
  border: 1px solid #4965f6;
  background-color: #ffffff;
  color: #4965f6;
  font-weight: 500; /* Pretendard Medium */
  font-size: 16px;
  cursor: pointer;
  border-radius: 6px;
  white-space: nowrap;
`;

const Divider = styled.div`
  width: 100vw; /* 화면 전체 폭 */
  height: 2px;
  background-color: #4965f6;
  margin: 0;
  position: relative;
  left: 50%;
  transform: translateX(-50%); /* 중앙 기준으로 좌우 끝까지 */
`;

const FAQList = styled.div`
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
`;

const FAQItem = styled.button`
  width: 100%;
  text-align: left;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: background-color 0.2s ease;

  &:hover { background-color: #f9fafb; }
`;

export const FAQPage: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'faq' | 'inquiry'>('faq');

  const goInquiry = () => {
    setActiveTab('inquiry');
    navigate('/support/inquiry');
  };

  return (
    <PageContainer>
      <ContentWrapper>
        <PageTitle>고객센터 - 자주 묻는 질문</PageTitle>

        <Tabs>
          <PrimaryTabButton onClick={() => setActiveTab('faq')}>자주 묻는 질문</PrimaryTabButton>
          <OutlineTabButton onClick={goInquiry}>고객 문의</OutlineTabButton>
        </Tabs>

        <Divider />

        <FAQList>
          {[
            '코치 회원가입 시 인증 절차는 어떻게 진행되나요?',
            '프로필 사진과 소개글은 어디서 수정하나요?',
            '코치 승인은 얼마나 걸리나요?',
            '코칭 예약은 어떻게 관리하나요?',
            '상담 기록이나 상담 요약본은 자동으로 저장되나요?',
            '고객센터에 직접 문의하려면 어떤 방법을 이용하나요?',
          ].map((q) => (
            <FAQItem key={q}>
              <span style={{ fontWeight: 700, color: '#111827', marginRight: 12 }}>Q.</span>
              <span style={{ flex: 1, color: '#111827' }}>{q}</span>
              <span style={{ color: '#6b7280' }}>›</span>
            </FAQItem>
          ))}
        </FAQList>
      </ContentWrapper>
    </PageContainer>
  );
};

export default FAQPage;

export {};