import React from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../../components/common/Button';
import { ROUTES } from '../../constants/routes';

export const NotFoundPage: React.FC = () => {
  return (
    <div className="flex items-center justify-center bg-gray-50 min-h-full">
      <div className="max-w-md w-full text-center">
        <div className="mb-8">
          <h1 className="text-9xl font-bold text-blue-600 mb-4">404</h1>
          <h2 className="text-2xl font-semibold text-gray-900 mb-2">
            페이지를 찾을 수 없습니다
          </h2>
          <p className="text-gray-600 mb-8">
            요청하신 페이지가 존재하지 않거나 이동되었을 수 있습니다.
          </p>
        </div>
        
        <div className="space-y-4">
          <Button
            variant="primary"
            size="large"
            onClick={() => window.history.back()}
            className="w-full"
          >
            이전 페이지로 돌아가기
          </Button>
          
          <Link to={ROUTES.HOME}>
            <Button
              variant="outline"
              size="large"
              className="w-full"
            >
              홈으로 이동
            </Button>
          </Link>
        </div>
        
        <div className="mt-8 text-sm text-gray-500">
          <p>문제가 지속되면 고객센터로 문의해주세요.</p>
          <Link to={ROUTES.INQUIRY} className="text-blue-600 hover:text-blue-500">
            고객센터 문의하기
          </Link>
        </div>
      </div>
    </div>
  );
};
