import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { FAQAnswerModal } from '../../components/common/Modal';

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

interface FAQItem {
  question: string;
  answer: string;
}

export const FAQPage: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'faq' | 'inquiry'>('faq');
  const [showFAQModal, setShowFAQModal] = useState(false);
  const [selectedFAQ, setSelectedFAQ] = useState<FAQItem | null>(null);

  const goInquiry = () => {
    setActiveTab('inquiry');
    navigate('/support/inquiry');
  };

  const faqList: FAQItem[] = [
    {
      question: '코치 회원가입 시 인증 절차는 어떻게 진행되나요?',
      answer: '코치 회원가입 시 다음과 같은 인증 절차를 진행합니다:\n\n1. 기본 정보 입력 (이름, 이메일, 비밀번호 등)\n2. 전문 분야 선택 및 자격증/경력 정보 입력\n3. 프로필 사진 및 소개글 작성\n4. 약관 동의\n5. 관리자 승인 대기\n\n관리자 승인 후 서비스 이용이 가능합니다.'
    },
    {
      question: '프로필 사진과 소개글은 어디서 수정하나요?',
      answer: '프로필 사진과 소개글은 마이페이지에서 수정할 수 있습니다:\n\n1. 상단 메뉴에서 "마이페이지" 클릭\n2. "코치님 마이페이지" 화면에서 프로필 사진 영역 클릭 (사진 변경)\n3. 소개글은 해당 화면의 텍스트 필드에서 직접 수정 가능\n4. 수정 후 저장 버튼 클릭하여 변경사항 반영'
    },
    {
      question: '코치 승인은 얼마나 걸리나요?',
      answer: '코치 승인은 보통 1-3영업일 내에 완료됩니다.\n\n제출하신 자격증 및 경력 정보를 검토한 후 승인 여부가 결정되며, 승인 완료 시 이메일로 알림을 드립니다.\n\n승인 대기 중이거나 추가 정보가 필요한 경우 고객센터로 문의해주시기 바랍니다.'
    },
    {
      question: '코칭 예약은 어떻게 관리하나요?',
      answer: '코칭 예약은 "예약 현황" 메뉴에서 관리할 수 있습니다:\n\n1. "예약 현황" 메뉴 클릭\n2. "현재 예약" 탭에서 다가오는 예약 확인\n3. 각 예약별로 "상담 시작", "신청 회원", "예약 취소" 등의 기능 이용 가능\n4. "지난 예약" 탭에서 이전 예약 내역 및 상담 요약본 확인 가능'
    },
    {
      question: '상담 기록이나 상담 요약본은 자동으로 저장되나요?',
      answer: '네, 상담 기록과 요약본은 자동으로 저장됩니다.\n\n스트리밍으로 진행된 상담 내용은 다음과 같이 관리됩니다:\n- 상담 영상은 서버에 자동 저장\n- AI가 생성한 상담 요약본은 "지난 예약" 탭에서 확인 가능\n- 각 예약별로 "상담 요약본" 버튼을 통해 상세 내용 확인 가능'
    },
    {
      question: '고객센터에 직접 문의하려면 어떤 방법을 이용하나요?',
      answer: '고객센터 문의는 다음과 같은 방법으로 가능합니다:\n\n1. 고객센터 메뉴에서 "고객 문의" 탭 클릭\n2. 문의 유형 선택 및 내용 작성\n3. 문의 제출\n\n또는 이메일 support@livon.com으로 직접 문의하실 수 있으며, 평일 09:00-18:00에 응답해드립니다.'
    },
  ];

  const handleFAQClick = (faq: FAQItem) => {
    setSelectedFAQ(faq);
    setShowFAQModal(true);
  };

  return (
    <PageContainer>
      <ContentWrapper>
        <PageTitle>고객센터 - 자주 묻는 질문</PageTitle>

        <Tabs>
          <TabButton $active={activeTab === 'faq'} onClick={() => setActiveTab('faq')}>자주 묻는 질문</TabButton>
          <TabButton $active={activeTab === 'inquiry'} onClick={goInquiry}>고객 문의</TabButton>
        </Tabs>

        <FAQList>
          {faqList.map((faq) => (
            <FAQItem key={faq.question} onClick={() => handleFAQClick(faq)}>
              <span style={{ fontWeight: 700, color: '#111827', marginRight: 12 }}>Q.</span>
              <span style={{ flex: 1, color: '#111827' }}>{faq.question}</span>
              <span style={{ color: '#6b7280' }}>›</span>
            </FAQItem>
          ))}
        </FAQList>

        {/* FAQ 답변 모달 */}
        {selectedFAQ && (
          <FAQAnswerModal
            open={showFAQModal}
            onClose={() => {
              setShowFAQModal(false);
              setSelectedFAQ(null);
            }}
            question={selectedFAQ.question}
            answer={selectedFAQ.answer}
          />
        )}
      </ContentWrapper>
    </PageContainer>
  );
};

export default FAQPage;

export {};