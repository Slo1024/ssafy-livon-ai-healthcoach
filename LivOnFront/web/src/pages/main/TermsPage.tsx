import React from 'react';

export const TermsPage: React.FC = () => {
  return (
    <div className="max-w-4xl mx-auto px-4 py-16" style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif' }}>
      <div className="bg-white rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-8 text-center">
          이용약관
        </h1>
        
        <div className="prose max-w-none">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">제1조 (목적)</h2>
          <p className="text-gray-600 mb-6">
            이 약관은 리브온(주)(이하 "회사")이 제공하는 헬스케어 서비스의 이용과 관련하여 회사와 이용자 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.
          </p>
          
          <h2 className="text-xl font-semibold text-gray-800 mb-4">제2조 (정의)</h2>
          <p className="text-gray-600 mb-6">
            이 약관에서 사용하는 용어의 정의는 다음과 같습니다.
          </p>
          <ul className="list-disc list-inside text-gray-600 mb-6 space-y-2">
            <li>"서비스"란 회사가 제공하는 헬스케어 관련 모든 서비스를 의미합니다.</li>
            <li>"이용자"란 서비스에 접속하여 이 약관에 따라 서비스를 이용하는 회원 및 비회원을 의미합니다.</li>
            <li>"회원"이란 서비스에 개인정보를 제공하여 회원등록을 한 자로서, 서비스의 정보를 지속적으로 제공받으며 서비스를 계속적으로 이용할 수 있는 자를 의미합니다.</li>
          </ul>
          
          <h2 className="text-xl font-semibold text-gray-800 mb-4">제3조 (약관의 효력 및 변경)</h2>
          <p className="text-gray-600 mb-6">
            이 약관은 이용자가 동의함으로써 효력을 발생하며, 회사는 필요에 따라 이 약관을 변경할 수 있습니다. 변경된 약관은 서비스 내 공지사항을 통해 공지됩니다.
          </p>
          
          <h2 className="text-xl font-semibold text-gray-800 mb-4">제4조 (서비스의 제공)</h2>
          <p className="text-gray-600 mb-6">
            회사는 다음과 같은 서비스를 제공합니다.
          </p>
          <ul className="list-disc list-inside text-gray-600 mb-6 space-y-2">
            <li>실시간 헬스케어 코칭 서비스</li>
            <li>개인 맞춤형 건강 관리 프로그램</li>
            <li>전문 코치와의 1:1 상담 서비스</li>
            <li>기타 회사가 정하는 서비스</li>
          </ul>
          
          <h2 className="text-xl font-semibold text-gray-800 mb-4">제5조 (이용자의 의무)</h2>
          <p className="text-gray-600 mb-6">
            이용자는 다음 행위를 하여서는 안 됩니다.
          </p>
          <ul className="list-disc list-inside text-gray-600 mb-6 space-y-2">
            <li>신청 또는 변경 시 허위 내용의 등록</li>
            <li>타인의 정보 도용</li>
            <li>회사가 게시한 정보의 변경</li>
            <li>회사가 정한 정보 이외의 정보(컴퓨터 프로그램 등) 등의 송신 또는 게시</li>
            <li>회사 기타 제3자의 저작권 등 지적재산권에 대한 침해</li>
            <li>회사 기타 제3자의 명예를 손상시키거나 업무를 방해하는 행위</li>
            <li>외설 또는 폭력적인 메시지, 화상, 음성, 기타 공서양속에 반하는 정보를 서비스에 공개 또는 게시하는 행위</li>
          </ul>
          
          <h2 className="text-xl font-semibold text-gray-800 mb-4">제6조 (개인정보보호)</h2>
          <p className="text-gray-600 mb-6">
            회사는 이용자의 개인정보 수집 시 필요한 최소한의 정보를 수집하며, 개인정보보호법에 따라 이용자의 개인정보를 보호하기 위해 노력합니다.
          </p>
          
          <h2 className="text-xl font-semibold text-gray-800 mb-4">제7조 (면책조항)</h2>
          <p className="text-gray-600 mb-6">
            회사는 천재지변 또는 이에 준하는 불가항력으로 인하여 서비스를 제공할 수 없는 경우에는 서비스 제공에 관한 책임이 면제됩니다.
          </p>
          
          <h2 className="text-xl font-semibold text-gray-800 mb-4">제8조 (준거법 및 관할법원)</h2>
          <p className="text-gray-600 mb-6">
            이 약관은 대한민국 법률에 따라 규율되고 해석되며, 서비스 이용과 관련하여 회사와 이용자 간에 발생한 분쟁에 대해서는 민사소송법상의 관할법원에 제소합니다.
          </p>
          
          <div className="mt-8 pt-6 border-t border-gray-200">
            <p className="text-sm text-gray-500 text-center">
              이 약관은 2025년 1월 1일부터 시행됩니다.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
