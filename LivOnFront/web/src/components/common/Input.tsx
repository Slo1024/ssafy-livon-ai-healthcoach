import React from 'react';
import styled from 'styled-components';

interface InputProps {
  type?: 'text' | 'email' | 'password' | 'number' | 'tel';
  placeholder?: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  error?: string;
  disabled?: boolean;
  required?: boolean;
  className?: string;
  style?: React.CSSProperties;
  maxLength?: number;
}

const StyledInput = styled.input`
  flex: 1;
  height: 48px;
  border: 1px solid #ecedec;
  border-radius: 12px;
  padding: 0 12px;
  font-size: 13px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  min-width: 0;
  line-height: 48px;
  box-sizing: border-box;

  &:focus {
    outline: none;
    border-color: #2d79f3;
  }

  &::placeholder {
    color: #999999;
    font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  }

  &:disabled {
    background-color: #f5f5f5;
    cursor: not-allowed;
  }
`;

export const Input: React.FC<InputProps> = ({
  type = 'text',
  placeholder,
  value,
  onChange,
  error,
  disabled = false,
  required = false,
  className = '',
  style,
  maxLength,
}) => {
  return (
    <>
      <StyledInput
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        disabled={disabled}
        required={required}
        className={className}
        style={style}
        maxLength={maxLength}
      />
      {error && (
        <div style={{ fontSize: '12px', color: '#ff0000', marginTop: '5px' }}>
          {error}
        </div>
      )}
    </>
  );
};
