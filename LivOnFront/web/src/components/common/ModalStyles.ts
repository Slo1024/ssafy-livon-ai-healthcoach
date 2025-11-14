import styled from "styled-components";

export interface BaseModalProps {
  open: boolean;
  onClose: () => void;
  className?: string;
  style?: React.CSSProperties;
}

export const Overlay = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
`;

export const Card = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

export const Title = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
  line-height: 1.3;
  white-space: pre-line; /* 줄바꿈 지원 */
`;

export const ConfirmButton = styled.button`
  margin-top: 0;
  width: 100%;
  height: 48px;
  background-color: #4965f6;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:hover {
    background-color: #3b5dd8;
  }
`;

