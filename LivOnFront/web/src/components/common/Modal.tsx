import React from 'react';
import styled from 'styled-components';

interface BaseModalProps {
  open: boolean;
  onClose: () => void;
  className?: string;
  style?: React.CSSProperties;
}

const Overlay = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
`;

const Card = styled.div`
  width: 820px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 24px;
  padding: 48px 40px 32px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  text-align: center;
`;

const Title = styled.h2`
  margin: 0 0 8px 0;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 800;
  font-size: 40px;
  color: #111827;
  line-height: 1.3;
  white-space: pre-line; /* 줄바꿈 지원 */
`;

const ConfirmButton = styled.button`
  margin-top: 24px;
  width: 100%;
  height: 64px;
  background-color: #5b77f6; /* 이미지 속 파랑과 유사 */
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 24px;
  font-weight: 700;
  cursor: pointer;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &:hover { background-color: #4965f6; }
`;

export interface ConfirmModalProps extends BaseModalProps {
  message: string; // 줄바꿈은 \n 으로 전달
  confirmText?: string;
}

/**
 * ConfirmModal
 * - 이미지의 "코치님의 정보가\n수정되었습니다." + 확인 버튼 스타일의 모달
 */
export const ConfirmModal: React.FC<ConfirmModalProps> = ({
  open,
  onClose,
  message,
  confirmText = '확인',
  className,
  style,
}) => {
  if (!open) return null;
  return (
    <Overlay onClick={onClose}>
      <Card className={className} style={style} onClick={(e) => e.stopPropagation()}>
        <Title>{message}</Title>
        <ConfirmButton onClick={onClose}>{confirmText}</ConfirmButton>
      </Card>
    </Overlay>
  );
};

export default ConfirmModal;

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: React.ReactNode;
  size?: 'small' | 'medium' | 'large';
}

export const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  title,
  children,
  size = 'medium',
}) => {
  if (!isOpen) return null;

  const sizeClasses = {
    small: 'max-w-md',
    medium: 'max-w-lg',
    large: 'max-w-2xl',
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black bg-opacity-50" 
        onClick={onClose}
      />
      
      {/* Modal */}
      <div className={`relative bg-white rounded-lg shadow-xl w-full mx-4 ${sizeClasses[size]}`}>
        {/* Header */}
        {title && (
          <div className="flex items-center justify-between p-6 border-b">
            <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        )}
        
        {/* Content */}
        <div className="p-6">
          {children}
        </div>
      </div>
    </div>
  );
};
