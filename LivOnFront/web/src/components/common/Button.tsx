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
  transition: color 0.2s ease;
  background: transparent;
  outline: none;
  border: none;
  padding: 0;
  font-weight: 600;
  font-size: 16px;
  color: #4965f6;

  &:hover {
    color: #2d79f3;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    pointer-events: none;
  }

  ${props => props.$variant === 'info-edit' && `
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    width: 85px;
    height: 35px;
    padding: 0 16px;
    border: 1px solid #4965f6;
    border-radius: 8px;
    background: #ffffff;
    color: #4965f6;
    font-size: 14px;
    font-weight: 400;
    text-align: center;
    white-space: nowrap;
    line-height: 1;

    &:hover {
      background: #f7fafc;
      color: #325ad6;
    }
  `}

  ${props => props.$variant === 'delete' && `
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    width: 85px;
    height: 35px;
    padding: 0 16px;
    border: 1px solid #ff0000;
    border-radius: 8px;
    background: #ffffff;
    color: #ff0000;
    font-size: 14px;
    font-weight: 500;
    text-align: center;
    white-space: nowrap;
    line-height: 1;

    &:hover {
      background: #fef2f2;
      color: #d90000;
    }
  `}
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

// SegmentedTabs (두 버튼)

const SegmentedContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const TabsRow = styled.div`
  display: flex;
  gap: 24px;
  padding: 4px 0;
`;

const TabButtonBase = styled.button<{ $active?: boolean; $width: number }>`
  min-width: ${p => p.$width}px;
  background: transparent;
  border: none;
  color: ${p => (p.$active ? '#1f2937' : '#6b7280')};
  font-weight: ${p => (p.$active ? 700 : 500)};
  font-size: 16px;
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.2s ease;
  padding: 6px 0;
  position: relative;

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

  &:hover {
    color: #4965f6;
  }

  &:focus {
    outline: none;
  }
`;

export interface SegmentedTabsProps {
  leftLabel: string;
  rightLabel: string;
  active?: 'left' | 'right';
  onLeftClick?: () => void;
  onRightClick?: () => void;
  tabWidth?: number; // default 120
  showDivider?: boolean; // default false
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
  showDivider: _showDivider = false,
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
    </>
  );
};
