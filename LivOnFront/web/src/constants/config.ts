// 환경 설정값
export const CONFIG = {
  // API 설정
  API_BASE_URL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080',
  API_TIMEOUT: 10000,
  
  // 소켓 설정
  SOCKET_URL: process.env.REACT_APP_SOCKET_URL || 'ws://localhost:8080',
  
  // 파일 업로드 설정
  MAX_FILE_SIZE: 10 * 1024 * 1024, // 10MB
  ALLOWED_FILE_TYPES: ['image/jpeg', 'image/png', 'image/gif'],
  
  // 스트리밍 설정
  STREAMING_CONFIG: {
    VIDEO_WIDTH: 1280,
    VIDEO_HEIGHT: 720,
    VIDEO_FRAME_RATE: 30,
    AUDIO_SAMPLE_RATE: 44100,
  },
  
  // LiveKit 설정 (OpenVidu 3.4.1)
  LIVEKIT: {
    SERVER_URL: process.env.REACT_APP_LIVEKIT_URL || 'ws://localhost:7880',
  },
  
  // 페이지네이션 설정
  PAGINATION: {
    DEFAULT_PAGE_SIZE: 10,
    MAX_PAGE_SIZE: 100,
  },
  
  // 토큰 설정
  TOKEN: {
    ACCESS_TOKEN_KEY: 'access_token',
    REFRESH_TOKEN_KEY: 'refresh_token',
    EXPIRES_IN: 3600, // 1시간
  },
  
  // 로컬 스토리지 키
  STORAGE_KEYS: {
    USER_INFO: 'user_info',
    THEME: 'theme',
    LANGUAGE: 'language',
  },
  
  // 테마 설정
  THEME: {
    DEFAULT: 'light',
    AVAILABLE_THEMES: ['light', 'dark'],
  },
  
  // 언어 설정
  LANGUAGE: {
    DEFAULT: 'ko',
    AVAILABLE_LANGUAGES: ['ko', 'en'],
  },
} as const;

// 설정 타입
export type ConfigKey = keyof typeof CONFIG;
export type ConfigValue = typeof CONFIG[ConfigKey];
