import React from "react";
import styled from "styled-components";

export interface ParticipantDetail {
  name: string;
  badges: string[];
  notes: string;
  questions: string[];
  analysis: {
    generatedAt: string;
    type: string;
    summary: string;
    tip: string;
  };
}

interface ParticipantInfoProps {
  open: boolean;
  participant?: ParticipantDetail;
  onClose: () => void;
}

const Overlay = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(20, 22, 34, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  padding: 24px 16px;
`;

const Card = styled.div`
  width: min(640px, 100%);
  max-height: 100vh;
  background: linear-gradient(180deg, #ffffff 0%, #f8f9fd 100%);
  border-radius: 32px;
  box-shadow: 0 32px 80px rgba(15, 23, 42, 0.25);
  padding: 32px 40px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: relative;
  overflow-y: auto;

  @media (max-width: 640px) {
    padding: 24px 20px;
    gap: 16px;
  }
`;

const Header = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

const Name = styled.h2`
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #111827;
`;

const Subtext = styled.p`
  margin: 0;
  font-size: 15px;
  color: #6b7280;
`;

const CloseButton = styled.button`
  position: absolute;
  top: 24px;
  right: 24px;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: #eff2fa;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4b5563;
  font-size: 20px;
  transition: background 0.2s;

  &:hover {
    background: #e2e8f0;
  }
`;

const Body = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
`;

const PhysicalSection = styled.div`
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px;
  border-radius: 24px;
  background: transparent;
  border: none;

  @media (max-width: 520px) {
    flex-direction: column;
    align-items: flex-start;
  }
`;

const Avatar = styled.div`
  width: 100px;
  height: 120px;
  border-radius: 20px;
  background: rgba(148, 163, 184, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  svg {
    width: 48px;
    height: 48px;
    color: #94a3b8;
  }
`;

const PhysicalList = styled.ul`
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;

  li {
    font-size: 16px;
    color: #1f2937;
    font-weight: 600;
  }
`;

const Divider = styled.hr`
  margin: 8px 0 20px;
  border: none;
  border-top: 1px solid #e2e8f0;
`;

const SectionTitle = styled.h3`
  margin: 0 0 12px 0;
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
`;

const Questions = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

const MemoText = styled.p`
  margin: 0;
  padding: 16px;
  border-radius: 14px;
  background: #f8fafc;
  color: #1f2937;
  line-height: 1.6;
`;

const QuestionItem = styled.div`
  padding: 10px 12px;
  border-radius: 14px;
  background: transparent;
  color: #1f2937;
  font-size: 15px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  line-height: 1.4;

  &:before {
    content: "Q.";
    color: #4c6ef5;
    font-weight: 700;
  }
`;

const AnalysisBox = styled.div`
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 20px;
  border-radius: 18px;
  background: transparent;
  border: none;
`;

const AnalysisMeta = styled.div`
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
  color: #4b5563;
`;

const AnalysisSummary = styled.p`
  margin: 0;
  padding: 14px;
  border-radius: 14px;
  background: transparent;
  color: #1f2937;
  line-height: 1.6;
`;

const AnalysisTip = styled.div`
  padding: 14px;
  border-radius: 14px;
  background: transparent;
  color: #92400e;
  font-weight: 600;
  line-height: 1.5;
`;

const Footer = styled.div`
  display: flex;
  justify-content: center;
`;

const ConfirmButton = styled.button`
  width: 220px;
  height: 48px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;

  &:hover {
    /* 호버 시 변화 없음 */
  }
`;

export const ParticipantInfo: React.FC<ParticipantInfoProps> = ({
  open,
  participant,
  onClose,
}) => {
  if (!open) {
    return null;
  }

  // participant가 없으면 기본값 사용
  const participantData = participant || {
    name: "참가자",
    badges: [],
    notes: "",
    questions: [],
    analysis: {
      generatedAt: "",
      type: "",
      summary: "",
      tip: "",
    },
  };

  return (
    <Overlay onClick={onClose}>
      <Card onClick={(e) => e.stopPropagation()}>
        <Header>
          <Name>{participantData.name} 회원님 정보</Name>
          <Subtext>
            회원님의 신체 데이터를 AI로 분석한 결과를 확인할 수 있습니다.
          </Subtext>
        </Header>

        <Body>
          <PhysicalSection>
            <Avatar>
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.5"
              >
                <circle cx="12" cy="8" r="4" />
                <path d="M4 20c0-3.314 3.134-6 7-6h2c3.866 0 7 2.686 7 6" />
              </svg>
            </Avatar>
            <div>
              <SectionTitle>신체 정보</SectionTitle>
              {participantData.notes ? (
                <MemoText>{participantData.notes}</MemoText>
              ) : (
                <PhysicalList>
                  <li>신체 정보 없음</li>
                </PhysicalList>
              )}
            </div>
          </PhysicalSection>

          {participantData.badges.length > 0 && (
            <div>
              <SectionTitle>건강 상태</SectionTitle>
              <PhysicalList>
                {participantData.badges.map((badge, index) => (
                  <li key={`${participantData.name}-badge-${index}`}>
                    {badge}
                  </li>
                ))}
              </PhysicalList>
            </div>
          )}

          <AnalysisBox>
            <SectionTitle>AI 분석 결과</SectionTitle>
            <AnalysisSummary>
              {participantData.analysis.summary || ""}
            </AnalysisSummary>
            <AnalysisTip>{participantData.analysis.tip || ""}</AnalysisTip>
          </AnalysisBox>

          <div>
            <SectionTitle>Q&A</SectionTitle>
            <Questions>
              {participantData.questions.length > 0 ? (
                participantData.questions.map((question, index) => (
                  <QuestionItem
                    key={`${participantData.name}-question-${index}`}
                  >
                    {question}
                  </QuestionItem>
                ))
              ) : (
                <QuestionItem>질문이 없습니다.</QuestionItem>
              )}
            </Questions>
          </div>
        </Body>

        <Footer>
          <ConfirmButton type="button" onClick={onClose}>
            확인
          </ConfirmButton>
        </Footer>
      </Card>
    </Overlay>
  );
};
