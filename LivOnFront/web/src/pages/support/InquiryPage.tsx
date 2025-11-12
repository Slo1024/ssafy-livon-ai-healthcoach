import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { InquirySuccessModal } from '../../components/common/Modal';

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

const Tabs = styled.div`
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
`;

const TabButton = styled.button<{ $active?: boolean }>`
  background: transparent;
  border: none;
  font-size: 18px;
  font-weight: ${p => (p.$active ? 700 : 500)};
  color: ${p => (p.$active ? '#111827' : '#9ca3af')};
  cursor: pointer;
  padding: 8px 0;
  position: relative;
  transition: color 0.2s ease;

  &:after {
    content: '';
    position: absolute;
    left: 0;
    bottom: -6px;
    width: 100%;
    height: 3px;
    background: ${p => (p.$active ? '#4965f6' : 'transparent')};
    border-radius: 999px;
    transition: background 0.2s ease;
  }
`;

const Form = styled.form`
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 28px;
`;

const FieldRow = styled.div`
  display: flex;
  align-items: center;
  gap: 24px;
`;

const FieldLabel = styled.label`
  width: 90px;
  flex-shrink: 0;
  font-size: 16px;
  color: #111827;
  font-weight: 600;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const Required = styled.span`
  color: #ef4444;
  margin-left: 6px;
`;

const LineInput = styled.input`
  flex: 1;
  height: 56px;
  border: none;
  border-bottom: 2px solid #6b75f6;
  outline: none;
  font-size: 16px;
  padding: 0 4px;
  background: transparent;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &::placeholder { color: #6b7280; }
`;

const Input = styled.input`
  height: 44px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
`;

const TextArea = styled.textarea`
  flex: 1;
  min-height: 160px;
  border: none;
  border-bottom: 2px solid #6b75f6;
  padding: 12px 4px 12px 4px;
  font-size: 16px;
  outline: none;
  resize: vertical;
  background: transparent;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const AgreeRow = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
  color: #374151;
  font-size: 14px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const SubmitWrap = styled.div`
  display: flex;
  justify-content: center;
  margin-top: 8px;
`;

const SubmitButton = styled.button`
  width: 520px;
  height: 64px;
  background-color: #5b77f6;
  color: #ffffff;
  border: none;
  border-radius: 16px;
  font-weight: 700;
  font-size: 22px;
  cursor: pointer;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &:hover { background-color: #4965f6; }
`;

export const InquiryPage: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'faq' | 'inquiry'>('inquiry');
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    content: '',
    agree: false,
  });

  const goFAQ = () => {
    setActiveTab('faq');
    navigate('/support/faq');
  };

  const handleInputChange = (field: string, value: string | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // 필수 입력사항 검증
    if (!formData.name.trim()) {
      alert('이름을 입력해주세요.');
      return;
    }

    if (!formData.email.trim()) {
      alert('이메일을 입력해주세요.');
      return;
    }

    if (!formData.content.trim()) {
      alert('문의 내용을 입력해주세요.');
      return;
    }

    if (!formData.agree) {
      alert('개인 정보 수집 및 이용에 동의해주세요.');
      return;
    }

    // 이메일 형식 검증 (간단한 검증)
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      alert('올바른 이메일 형식을 입력해주세요.');
      return;
    }

    // 모달 표시
    setShowSuccessModal(true);
  };

  const handleModalConfirm = () => {
    // 폼 초기화
    setFormData({
      name: '',
      email: '',
      phone: '',
      content: '',
      agree: false,
    });
    setShowSuccessModal(false);
  };

  return (
    <PageContainer>
      <ContentWrapper>
        <PageTitle>고객센터 - 고객 문의</PageTitle>

        <Tabs>
          <TabButton $active={activeTab === 'faq'} onClick={goFAQ}>자주 묻는 질문</TabButton>
          <TabButton $active={activeTab === 'inquiry'} onClick={() => setActiveTab('inquiry')}>고객 문의</TabButton>
        </Tabs>

        <Form onSubmit={handleSubmit}>
          <FieldRow>
            <FieldLabel>이름<Required>*</Required></FieldLabel>
            <LineInput 
              placeholder="이름" 
              value={formData.name}
              onChange={(e) => handleInputChange('name', e.target.value)}
            />
          </FieldRow>
          <FieldRow>
            <FieldLabel>이메일<Required>*</Required></FieldLabel>
            <LineInput 
              placeholder="이메일" 
              type="email"
              value={formData.email}
              onChange={(e) => handleInputChange('email', e.target.value)}
            />
          </FieldRow>
          <FieldRow>
            <FieldLabel>연락처</FieldLabel>
            <LineInput 
              placeholder="연락 받으실 연락처(선택사항)" 
              value={formData.phone}
              onChange={(e) => handleInputChange('phone', e.target.value)}
            />
          </FieldRow>
          <FieldRow>
            <FieldLabel>문의 내용<Required>*</Required></FieldLabel>
            <TextArea 
              placeholder="문의 내용을 입력해 주세요." 
              value={formData.content}
              onChange={(e) => handleInputChange('content', e.target.value)}
            />
          </FieldRow>

          <div>
            <FieldLabel style={{ width: 180 }}>개인 정보 수집 및 이용 동의<Required>*</Required></FieldLabel>
            <AgreeRow>
              <input 
                type="checkbox" 
                checked={formData.agree}
                onChange={(e) => handleInputChange('agree', e.target.checked)}
              />
              <span>개인 정보 수집 및 이용에 동의합니다.</span>
            </AgreeRow>
          </div>

          <SubmitWrap>
            <SubmitButton type="submit">보내기</SubmitButton>
          </SubmitWrap>
        </Form>

        {/* 문의 사항 전달 완료 모달 */}
        <InquirySuccessModal
          open={showSuccessModal}
          onClose={() => setShowSuccessModal(false)}
          onConfirm={handleModalConfirm}
        />
      </ContentWrapper>
    </PageContainer>
  );
};

export default InquiryPage;

export {};