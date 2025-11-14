import React from "react";
import styled from "styled-components";
import { BaseModalProps, Overlay } from "./ModalStyles";

const InfoUpdateModalCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 48px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

const InfoUpdateModalTitle = styled.h2`
  margin: 0 0 32px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
  line-height: 1.5;
  white-space: pre-line;
`;

const HomeButton = styled.button`
  width: 100%;
  height: 48px;
  background-color: #667eea;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #5568d3;
  }
`;

export interface InfoUpdateSuccessModalProps extends BaseModalProps {
  message: string; // 줄바꿈은 \n 으로 전달
  homeButtonText?: string;
  onGoHome: () => void;
}

/**
 * InfoUpdateSuccessModal
 * - 정보 수정 완료 모달
 * - "코치님의 정보가\n수정되었습니다." + "홈 바로가기" 버튼
 */
export const InfoUpdateSuccessModal: React.FC<InfoUpdateSuccessModalProps> = ({
  open,
  onClose,
  message,
  homeButtonText = "홈 바로가기",
  onGoHome,
  className,
  style,
}) => {
  if (!open) return null;
  return (
    <Overlay onClick={onClose}>
      <InfoUpdateModalCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <InfoUpdateModalTitle>{message}</InfoUpdateModalTitle>
        <HomeButton onClick={onGoHome}>{homeButtonText}</HomeButton>
      </InfoUpdateModalCard>
    </Overlay>
  );
};

export default InfoUpdateSuccessModal;

