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
  font-weight: 700;
  font-size: 40px;
  color: #000000;
  margin: 0 0 24px 0;
`;

const Tabs = styled.div`
  display: flex;
`;

const TabButton = styled.button<{ active: boolean }>`
  width: 100px;
  height: 48px;
  border: 1px solid #4965f6;
  background-color: ${p => (p.active ? '#4965f6' : '#ffffff')};
  color: ${p => (p.active ? '#ffffff' : '#4965f6')};
  font-weight: 500;
  font-size: 16px;
  cursor: pointer;
  border-radius: 0;
  &:first-child { border-radius: 6px 0 0 6px; }
  &:last-child { border-radius: 0 6px 6px 0; border-left: none; }
`;

const Divider = styled.div`
  width: 100%;
  height: 2px;
  background-color: #4965f6;
  margin: 0;
`;

const Form = styled.form`
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
`;

const Input = styled.input`
  height: 44px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
`;

const TextArea = styled.textarea`
  min-height: 160px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
`;

const SubmitButton = styled.button`
  width: 160px;
  height: 44px;
  background-color: #4965f6;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
`;

export const InquiryPage: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'faq' | 'inquiry'>('inquiry');

  const goFAQ = () => {
    setActiveTab('faq');
    navigate('/support/faq');
  };

  return (
    <PageContainer>
      <ContentWrapper>
        <PageTitle>고객센터 - 고객 문의</PageTitle>

        <Tabs>
          <TabButton active={activeTab === 'faq'} onClick={goFAQ}>자주 묻는 질문</TabButton>
          <TabButton active={activeTab === 'inquiry'} onClick={() => setActiveTab('inquiry')}>고객 문의</TabButton>
        </Tabs>

        <Divider />

        <Form onSubmit={(e) => e.preventDefault()}>
          <Input placeholder="제목" />
          <Input placeholder="이메일" />
          <TextArea placeholder="문의 내용을 입력해 주세요." />
          <SubmitButton type="submit">제출하기</SubmitButton>
        </Form>
      </ContentWrapper>
    </PageContainer>
  );
};

export default InquiryPage;

export {};