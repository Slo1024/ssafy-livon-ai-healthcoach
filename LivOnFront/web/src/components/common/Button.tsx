import React from 'react';
import styled from 'styled-components';

interface ButtonProps {
  children: React.ReactNode;
  onClick?: () => void;
  type?: 'button' | 'submit' | 'reset';
  variant?: 'primary' | 'secondary' | 'outline' | 'submit' | 'info-edit' | 'delete';
  size?: 'small' | 'medium' | 'large';
  disabled?: boolean;
  className?: string;
  style?: React.CSSProperties;
}

const StyledButton = styled.button<{ $variant?: string; $size?: string }>`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  outline: none;

  ${props => {
    switch (props.$variant) {
      case 'primary':
        return `
          background-color: #2d79f3;
          color: white;
          font-weight: 500;
          &:hover {
            background-color: #1a5fd9;
          }
        `;
      case 'secondary':
        return `
          background-color: #6c757d;
          color: white;
          font-weight: 500;
          &:hover {
            background-color: #5a6268;
          }
        `;
      case 'outline':
        return `
          background-color: transparent;
          border: 0.125em solid #4965f6;
          color: #4965f6;
          font-weight: 600;
          &:hover {
            color: #fff;
            background-color: #4965f6;
            box-shadow: rgba(73, 101, 246, 0.25) 0 8px 15px;
            transform: translateY(-2px);
          }
          &:active {
            box-shadow: none;
            transform: translateY(0);
          }
        `;
      case 'submit':
        return `
          width: 686px;
          background-color: #2d79f3;
          color: white;
          font-weight: 800;
          border-radius: 8px;
          &:hover {
            background-color: #1a5fd9;
          }
        `;
      case 'info-edit':
        return `
          width: 120px;
          height: 60px;
          background-color: transparent;
          border: 1px solid #4965f6;
          color: #4965f6;
          font-weight: 500;
          font-size: 12px;
          border-radius: 20px;
          transition: all 0.3s ease;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          &:hover {
            background-color: #4965f6;
            color: #ffffff;
          }
        `;
      case 'delete':
        return `
          width: 120px;
          height: 60px;
          background-color: transparent;
          border: 1px solid #ff0000;
          color: #ff0000;
          font-weight: 500;
          font-size: 14px;
          border-radius: 20px;
          transition: all 0.3s ease;
          &:hover {
            background-color: #ff0000;
            color: #ffffff;
          }
        `;
      default:
        return `
          background-color: #2d79f3;
          color: white;
          font-weight: 500;
          &:hover {
            background-color: #1a5fd9;
          }
        `;
    }
  }}

  ${props => {
    switch (props.$size) {
      case 'small':
        return `
          padding: 0 16px;
          height: 36px;
          font-size: 13px;
          border-radius: 6px;
        `;
      case 'medium':
        if (props.$variant === 'outline') {
          return `
            width: 87px;
            height: 42px;
            font-size: 14px;
            border-radius: 5px;
          `;
        }
        return `
          padding: 0 16px;
          height: 40px;
          font-size: 14px;
          border-radius: 6px;
        `;
      case 'large':
        return `
          padding: 0 24px;
          height: 48px;
          font-size: 16px;
          border-radius: 8px;
        `;
      default:
        return `
          padding: 0 16px;
          height: 40px;
          font-size: 14px;
          border-radius: 6px;
        `;
    }
  }}

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    pointer-events: none;
  }
`;

export const Button: React.FC<ButtonProps> = ({
  children,
  onClick,
  type = 'button',
  variant = 'primary',
  size = 'medium',
  disabled = false,
  className = '',
  style,
}) => {
  return (
    <StyledButton
      type={type}
      onClick={onClick}
      disabled={disabled}
      $variant={variant}
      $size={size}
      className={className}
      style={style}
    >
      {children}
    </StyledButton>
  );
};

// =====================
// SegmentedTabs (두 버튼 + 하단 가로줄)
// =====================

const SegmentedContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const TabsRow = styled.div`
  display: flex;
  gap: 0;

  button {
    border-radius: 6px;
  }
  button + button {
    margin-left: -1px; /* 경계선 겹치기 */
  }
`;

const TabButtonBase = styled.button<{ $active?: boolean; $width: number }>`
  height: 48px;
  width: ${p => p.$width}px;
  border: 1px solid #4965f6;
  background-color: ${p => (p.$active ? '#4965f6' : '#ffffff')};
  color: ${p => (p.$active ? '#ffffff' : '#4965f6')};
  font-weight: 500;
  font-size: 16px;
  cursor: pointer;
  border-radius: 6px;
  white-space: nowrap;
  transition: background-color 0.2s ease;
  outline: none;

  &:hover {
    background-color: ${p => (p.$active ? '#4965f6' : '#f7fafc')};
  }

  &:focus {
    outline: none;
  }
`;

const FullWidthDivider = styled.div`
  width: 100vw;
  height: 2px;
  background-color: #4965f6;
  margin: 0;
  position: relative;
  left: 50%;
  transform: translateX(-50%);
  margin-top: 0;
`;

export interface SegmentedTabsProps {
  leftLabel: string;
  rightLabel: string;
  active?: 'left' | 'right';
  onLeftClick?: () => void;
  onRightClick?: () => void;
  tabWidth?: number; // default 120
  showDivider?: boolean; // default true
  className?: string;
  style?: React.CSSProperties;
}

export const SegmentedTabs: React.FC<SegmentedTabsProps> = ({
  leftLabel,
  rightLabel,
  active = 'left',
  onLeftClick,
  onRightClick,
  tabWidth = 120,
  showDivider = true,
  className,
  style,
}) => {
  return (
    <>
      <SegmentedContainer className={className} style={style}>
        <TabsRow>
          <TabButtonBase $active={active === 'left'} $width={tabWidth} onClick={onLeftClick}>
            {leftLabel}
          </TabButtonBase>
          <TabButtonBase $active={active === 'right'} $width={tabWidth} onClick={onRightClick}>
            {rightLabel}
          </TabButtonBase>
        </TabsRow>
      </SegmentedContainer>
      {showDivider && <FullWidthDivider />}
    </>
  );
};
