import React from 'react';
import { Link } from 'react-router-dom';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-gray-800 text-white" style={{ width: '1312px', height: '79px', margin: '0 auto' }}>
      <div style={{ width: '100%', height: '100%', display: 'flex', flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', padding: '0 20px', overflow: 'hidden' }}>
        {/* Left Section - Company Info */}
        <div style={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: '20px', flexWrap: 'wrap' }}>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 500, fontSize: '15px', whiteSpace: 'nowrap' }}>
            리브온 | 헬스케어
          </span>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 500, fontSize: '15px', whiteSpace: 'nowrap' }}>
            법인명: 리브온(주)
          </span>
          <Link to="/terms" className="text-gray-300 hover:text-white transition-colors" style={{ textDecoration: 'none' }}>
            <span style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 500, fontSize: '15px', whiteSpace: 'nowrap' }}>
              이용약관
            </span>
          </Link>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 500, fontSize: '15px', whiteSpace: 'nowrap' }}>
            문의
          </span>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 400, fontSize: '14px', whiteSpace: 'nowrap' }}>
            이메일: support@livon.com
          </span>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 400, fontSize: '14px', whiteSpace: 'nowrap' }}>
            주소: 46733 부산광역시 강서구 녹산산업중로 333
          </span>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 400, fontSize: '14px', whiteSpace: 'nowrap' }}>
            사업자 등록번호: 000-00-0000
          </span>
        </div>
        
        {/* Right Section - Copyright */}
        <div style={{ display: 'flex', alignItems: 'center', flexShrink: 0 }}>
          <span className="text-gray-300" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 400, fontSize: '14px', whiteSpace: 'nowrap' }}>
            Copyright © 2025 LIVON. All rights reserved.
          </span>
        </div>
      </div>
    </footer>
  );
};
