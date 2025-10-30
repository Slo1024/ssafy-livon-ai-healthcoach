import React from 'react';
import { Link } from 'react-router-dom';
import styled from 'styled-components';

const StyledLink = styled(Link)`
  text-decoration: none;
  color: #000000;
  outline: none;
  
  &:focus {
    outline: none;
    border: none;
    box-shadow: none;
  }
  
  &:focus-visible {
    outline: none;
    border: none;
    box-shadow: none;
  }
`;

export const Footer: React.FC = () => {
  return (
    <footer className="bg-gray-800 text-white" style={{ width: '100%', minHeight: '79px', margin: '0 auto', boxSizing: 'border-box' }}>
      <div style={{ width: '100%', maxWidth: '100%', height: '100%', minHeight: '79px', display: 'flex', flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', padding: '10px 20px', boxSizing: 'border-box', flexWrap: 'wrap', gap: '10px' }}>
        {/* Left Section - Company Info */}
        <div style={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: 'clamp(10px, 2vw, 20px)', flexWrap: 'wrap', flex: '1 1 auto' }}>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 500, fontSize: 'clamp(12px, 1.8vw, 15px)', whiteSpace: 'nowrap' }}>
            리브온 | 헬스케어
          </span>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 500, fontSize: 'clamp(12px, 1.8vw, 15px)', whiteSpace: 'nowrap' }}>
            법인명: 리브온(주)
          </span>
          <StyledLink to="/terms" className="text-gray-300 hover:text-white transition-colors">
            <span style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 500, fontSize: 'clamp(12px, 1.8vw, 15px)', whiteSpace: 'nowrap', color: '#000000' }}>
              이용약관
            </span>
          </StyledLink>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 500, fontSize: 'clamp(12px, 1.8vw, 15px)', whiteSpace: 'nowrap' }}>
            문의
          </span>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 400, fontSize: 'clamp(11px, 1.5vw, 14px)', whiteSpace: 'nowrap' }}>
            이메일: support@livon.com
          </span>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 400, fontSize: 'clamp(11px, 1.5vw, 14px)', whiteSpace: 'nowrap' }}>
            주소: 46733 부산광역시 강서구 녹산산업중로 333
          </span>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 400, fontSize: 'clamp(11px, 1.5vw, 14px)', whiteSpace: 'nowrap' }}>
            사업자 등록번호: 000-00-0000
          </span>
        </div>
        
        {/* Right Section - Copyright */}
        <div style={{ display: 'flex', alignItems: 'center', flexShrink: 0, marginLeft: 'auto' }}>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 400, fontSize: 'clamp(12px, 1.5vw, 14px)', whiteSpace: 'nowrap' }}>
            Copyright © 2025 LIVON. All rights reserved.
          </span>
        </div>
      </div>
    </footer>
  );
};
