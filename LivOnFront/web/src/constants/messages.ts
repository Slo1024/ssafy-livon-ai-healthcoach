// 안내문구 및 메시지 정의
export const MESSAGES = {
  // 공통 메시지
  LOADING: '로딩 중...',
  ERROR: '오류가 발생했습니다.',
  SUCCESS: '성공적으로 처리되었습니다.',
  CONFIRM: '확인',
  CANCEL: '취소',
  SAVE: '저장',
  DELETE: '삭제',
  EDIT: '수정',
  CLOSE: '닫기',
  
  // 인증 관련
  LOGIN_SUCCESS: '로그인되었습니다.',
  LOGOUT_SUCCESS: '로그아웃되었습니다.',
  SIGNUP_SUCCESS: '회원가입이 완료되었습니다.',
  EMAIL_VERIFICATION_SUCCESS: '이메일 인증이 완료되었습니다.',
  PASSWORD_RESET_SUCCESS: '비밀번호가 재설정되었습니다.',
  
  // 클래스 관련
  CLASS_CREATED: '클래스가 생성되었습니다.',
  CLASS_UPDATED: '클래스가 수정되었습니다.',
  CLASS_DELETED: '클래스가 삭제되었습니다.',
  
  // 예약 관련
  RESERVATION_APPROVED: '예약이 승인되었습니다.',
  RESERVATION_CANCELLED: '예약이 취소되었습니다.',
  
  // 스트리밍 관련
  STREAMING_STARTED: '스트리밍이 시작되었습니다.',
  STREAMING_ENDED: '스트리밍이 종료되었습니다.',
  PARTICIPANT_JOINED: '참가자가 입장했습니다.',
  PARTICIPANT_LEFT: '참가자가 퇴장했습니다.',
  
  // 에러 메시지
  NETWORK_ERROR: '네트워크 연결을 확인해주세요.',
  UNAUTHORIZED: '로그인이 필요합니다.',
  FORBIDDEN: '접근 권한이 없습니다.',
  NOT_FOUND: '요청한 페이지를 찾을 수 없습니다.',
  SERVER_ERROR: '서버 오류가 발생했습니다.',
  
  // 유효성 검사
  REQUIRED_FIELD: '필수 입력 항목입니다.',
  INVALID_EMAIL: '올바른 이메일 형식이 아닙니다.',
  INVALID_PASSWORD: '비밀번호는 8자 이상이어야 합니다.',
  PASSWORD_MISMATCH: '비밀번호가 일치하지 않습니다.',
  INVALID_PHONE: '올바른 전화번호 형식이 아닙니다.',
} as const;

// 메시지 타입
export type MessageKey = keyof typeof MESSAGES;
export type MessageValue = typeof MESSAGES[MessageKey];
