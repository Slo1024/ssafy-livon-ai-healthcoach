import React from 'react';
import appdownload from '../../assets/images/appdownload.png';

export const DownloadPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-white px-4 py-16" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif' }}>
      <div className="max-w-6xl mx-auto">
        <div className="flex flex-col lg:flex-row items-center gap-12">
          {/* 텍스트 영역 */}
          <div className="flex-1 lg:pr-12">
            <h1 className="text-4xl font-bold text-blue-600 mb-8">
              리브온 다운로드
            </h1>
            
            <div className="space-y-6 text-lg text-gray-700 leading-relaxed mb-8">
              <p>
                모바일로도 코치님의 회원들에게
              </p>
              <p>
                실시간 코칭을 제공할 수 있어요.
              </p>
            </div>
            
            {/* 다운로드 버튼 */}
            <button className="bg-purple-600 hover:bg-purple-700 text-white font-semibold py-4 px-8 rounded-lg text-lg transition-colors">
              리브온 다운로드 바로가기
            </button>
          </div>
          
          {/* 이미지 영역 */}
          <div className="flex-shrink-0">
            <img 
              src={appdownload} 
              alt="앱 다운로드 일러스트" 
              width="500"
              height="500"
              className="w-auto h-auto"
            />
          </div>
        </div>
      </div>
    </div>
  );
};