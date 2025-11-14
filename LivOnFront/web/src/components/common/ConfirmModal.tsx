import React from "react";
import { BaseModalProps, Overlay, Card, Title, ConfirmButton } from "./ModalStyles";

export interface ConfirmModalProps extends BaseModalProps {
  message: string; // 줄바꿈은 \n 으로 전달
  confirmText?: string;
}

/**
 * ConfirmModal
 * - 확인 버튼이 있는 간단한 모달
 */
export const ConfirmModal: React.FC<ConfirmModalProps> = ({
  open,
  onClose,
  message,
  confirmText = "확인",
  className,
  style,
}) => {
  if (!open) return null;
  return (
    <Overlay onClick={onClose}>
      <Card
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <Title>{message}</Title>
        <ConfirmButton onClick={onClose}>{confirmText}</ConfirmButton>
      </Card>
    </Overlay>
  );
};

export default ConfirmModal;

