import React from 'react';
import mainpage1 from '../../assets/images/mainpage1.png';
import mainpage2 from '../../assets/images/mainpage2.png';

export const HomePage: React.FC = () => {
  return (
    <div className="min-h-screen flex flex-col bg-white" style={{ padding: 0, margin: 0 }}>
      {/* 메인 텍스트 영역 */}
      <div className="flex-1 flex items-center justify-center" style={{ paddingTop: '80px', paddingBottom: '40px' }}>
        <div className="text-center w-full" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <h1 className="mb-4" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 700, fontSize: '32px', color: '#1f2937', textAlign: 'center' }}>
            오늘도 당신 곁에서, 건강을 코칭합니다. <br />
            실시간 맞춤형 헬스 케어{' '}
            <span style={{ color: '#4965f6' }}>리브온</span>
          </h1>
        </div>
      </div>
      
      {/* 이미지 영역 - 화면 하단을 꽉 채움 */}
      <div style={{ display: 'flex', flexDirection: 'row', width: '100%', marginTop: 'auto', paddingBottom: '79px', flexWrap: 'nowrap', alignItems: 'flex-end' }}>
        <div style={{ width: '50%', flexShrink: 0, display: 'flex', alignItems: 'flex-end', justifyContent: 'center', backgroundColor: 'white', overflow: 'hidden' }}>
          <img 
            src={mainpage1} 
            alt="헬스 코칭 서비스 일러스트 1" 
            style={{ width: '100%', height: 'auto', display: 'block', maxWidth: '100%' }}
          />
        </div>
        <div style={{ width: '50%', flexShrink: 0, display: 'flex', alignItems: 'flex-end', justifyContent: 'center', backgroundColor: 'white', overflow: 'hidden' }}>
          <img 
            src={mainpage2} 
            alt="헬스 코칭 서비스 일러스트 2" 
            style={{ width: '100%', height: 'auto', display: 'block', maxWidth: '100%' }}
          />
        </div>
      </div>
    </div>
  );
};