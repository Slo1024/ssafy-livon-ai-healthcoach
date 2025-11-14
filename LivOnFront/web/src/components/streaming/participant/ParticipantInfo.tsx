import React from "react";
import styled from "styled-components";
import { ParticipantInfoResponse } from "../../../api/reservationApi";

// API 응답 2개를 조합한 데이터 타입
export interface ParticipantModalData {
  participantInfo: ParticipantInfoResponse; // getParticipantInfoApi 응답
  preQna?: string; // getCoachConsultationsApi 항목에서 추출
  aiSummary?: string; // getCoachConsultationsApi 항목에서 추출
}

// 기존 ParticipantDetail은 하위 호환성을 위해 유지
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
  data?: ParticipantModalData; // participant -> data로 변경
  isLoading?: boolean; // 로딩 상태
  error?: string | null; // 에러 메시지
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
  max-height: calc(100vh - 48px);
  background: linear-gradient(180deg, #ffffff 0%, #f8f9fd 100%);
  border-radius: 32px;
  box-shadow: 0 32px 80px rgba(15, 23, 42, 0.25);
  padding: 24px 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  overflow: hidden;

  @media (max-width: 640px) {
    padding: 20px 16px;
    gap: 10px;
  }
`;

const Header = styled.div`
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
`;

const Name = styled.h2`
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #111827;
  line-height: 1.2;
`;

const Subtext = styled.p`
  margin: 0;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.4;
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
  gap: 10px;
  flex: 1;
  min-height: 0;
`;

const PhysicalSection = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 12px;
  border-radius: 24px;
  background: transparent;
  border: none;
  flex-shrink: 0;
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
  gap: 6px;

  li {
    font-size: 14px;
    color: #1f2937;
    font-weight: 600;
    line-height: 1.4;
  }
`;

const Divider = styled.hr`
  margin: 8px 0 20px;
  border: none;
  border-top: 1px solid #e2e8f0;
`;

const SectionTitle = styled.h3`
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.3;
`;

const Questions = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
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
  padding: 8px 10px;
  border-radius: 14px;
  background: transparent;
  color: #1f2937;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  line-height: 1.4;

  &:before {
    content: "Q.";
    color: #1f2937;
    font-weight: 700;
  }
`;

const AnalysisBox = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  border-radius: 18px;
  background: transparent;
  border: none;
  flex-shrink: 0;
`;

const AnalysisSummary = styled.p`
  margin: 0;
  padding: 10px;
  border-radius: 14px;
  background: transparent;
  color: #1f2937;
  font-size: 14px;
  line-height: 1.5;
`;

const Footer = styled.div`
  display: flex;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 4px;
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

const LoadingMessage = styled.div`
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
  font-size: 16px;
`;

const ErrorMessage = styled.div`
  text-align: center;
  padding: 40px 20px;
  color: #ef4444;
  font-size: 16px;
`;

export const ParticipantInfo: React.FC<ParticipantInfoProps> = ({
  open,
  data,
  isLoading = false,
  error = null,
  onClose,
}) => {
  if (!open) {
    return null;
  }

  // 로딩 중
  if (isLoading) {
    return (
      <Overlay onClick={onClose}>
        <Card onClick={(e) => e.stopPropagation()}>
          <LoadingMessage>정보를 불러오는 중...</LoadingMessage>
        </Card>
      </Overlay>
    );
  }

  // 에러 발생
  if (error) {
    return (
      <Overlay onClick={onClose}>
        <Card onClick={(e) => e.stopPropagation()}>
          <ErrorMessage>{error}</ErrorMessage>
          <Footer>
            <ConfirmButton type="button" onClick={onClose}>
              확인
            </ConfirmButton>
          </Footer>
        </Card>
      </Overlay>
    );
  }

  // 데이터가 없음
  if (!data) {
    return (
      <Overlay onClick={onClose}>
        <Card onClick={(e) => e.stopPropagation()}>
          <ErrorMessage>참여자 정보를 불러올 수 없습니다.</ErrorMessage>
          <Footer>
            <ConfirmButton type="button" onClick={onClose}>
              확인
            </ConfirmButton>
          </Footer>
        </Card>
      </Overlay>
    );
  }

  // API 데이터를 변수로 풀어서 쓰기
  const { participantInfo, preQna, aiSummary } = data;
  const { memberInfo } = participantInfo;
  const { healthData, nickname } = memberInfo;

  return (
    <Overlay onClick={onClose}>
      <Card onClick={(e) => e.stopPropagation()}>
        <Header>
          <Name>{nickname} 회원님 정보</Name>
          <Subtext>
            회원님의 신체 데이터를 AI로 분석한 결과를 확인할 수 있습니다.
          </Subtext>
        </Header>

        <Body>
          <PhysicalSection>
            <div>
              <SectionTitle>신체 정보</SectionTitle>
              <PhysicalList>
                <li>
                  신장: {healthData.height ? `${healthData.height} cm` : "정보 없음"}
                </li>
                <li>
                  체중: {healthData.weight ? `${healthData.weight} kg` : "정보 없음"}
                </li>
                <li>
                  수면 시간: {healthData.sleepTime !== undefined && healthData.sleepTime !== null
                    ? `${healthData.sleepTime}시간`
                    : "정보 없음"}
                </li>
                <li>
                  일일 걸음 수: {healthData.steps !== undefined && healthData.steps !== null
                    ? `${healthData.steps}걸음`
                    : "정보 없음"}
                </li>
              </PhysicalList>
            </div>
          </PhysicalSection>

          {(healthData.activityLevel || healthData.sleepQuality || healthData.stressLevel) && (
            <div style={{ paddingLeft: '12px' }}>
              <SectionTitle>건강 설문</SectionTitle>
              <PhysicalList>
                {healthData.activityLevel && (
                  <li>활동 수준: {healthData.activityLevel}</li>
                )}
                {healthData.sleepQuality && (
                  <li>수면의 질: {healthData.sleepQuality}</li>
                )}
                {healthData.stressLevel && (
                  <li>스트레스 수준: {healthData.stressLevel}</li>
                )}
              </PhysicalList>
            </div>
          )}

          <AnalysisBox>
            <SectionTitle>AI 분석 결과</SectionTitle>
            <AnalysisSummary>
              {aiSummary || "AI 분석 결과가 없습니다."}
            </AnalysisSummary>
          </AnalysisBox>

          <div style={{ paddingLeft: '12px' }}>
            <SectionTitle>Q&A</SectionTitle>
            <Questions>
              {preQna ? (
                <QuestionItem>{preQna}</QuestionItem>
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
