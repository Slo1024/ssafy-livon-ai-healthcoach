import React from 'react';
import styled from 'styled-components';

interface DropdownOption {
  value: string;
  label: string;
}

interface DropdownProps {
  options: DropdownOption[];
  value: string;
  onChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
  placeholder?: string;
  error?: string;
  disabled?: boolean;
  className?: string;
  style?: React.CSSProperties;
}

const StyledDropdown = styled.select<{ isPlaceholder?: boolean }>`
  height: 48px;
  line-height: 48px;
  border: 1px solid #ecedec;
  border-radius: 12px;
  padding: 0 12px;
  font-size: 13px;
  color: ${props => (props.isPlaceholder ? '#999999' : '#000000')};
  background-color: white;
  /* custom caret */
  background-image: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='10' height='6' viewBox='0 0 10 6'><path fill='%23999999' d='M1 0l4 4 4-4 1 1-5 5-5-5z'/></svg>");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 10px 6px;
  padding-right: 36px; /* space for caret */
  cursor: pointer;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  flex: 1;
  min-width: 0;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;

  &:focus {
    outline: none;
    border-color: #2d79f3;
  }

  &::placeholder {
    color: #999999;
  }

  option {
    color: #000000;
  }
  option[disabled] {
    color: #999999;
  }

  &:disabled {
    background-color: #f5f5f5;
    cursor: not-allowed;
  }
`;

export const Dropdown: React.FC<DropdownProps> = ({
  options,
  value,
  onChange,
  placeholder = undefined,
  error,
  disabled = false,
  className = '',
  style,
}) => {
  const isPlaceholder = value === '';

  return (
    <>
      <StyledDropdown
        value={value}
        onChange={onChange}
        disabled={disabled}
        className={className}
        style={style}
        isPlaceholder={isPlaceholder}
      >
        {placeholder && (
          <option value="" disabled>
            {placeholder}
          </option>
        )}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </StyledDropdown>
      {error && (
        <div style={{ fontSize: '12px', color: '#ff0000', marginTop: '5px' }}>
          {error}
        </div>
      )}
    </>
  );
};
