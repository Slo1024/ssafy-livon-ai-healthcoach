// 스토리지 관련 유틸리티 함수들
export const storage = {
  // Local Storage
  setLocalItem: (key: string, value: any): void => {
    try {
      const serializedValue = JSON.stringify(value);
      localStorage.setItem(key, serializedValue);
    } catch (error) {
      console.error('LocalStorage 저장 실패:', error);
    }
  },

  getLocalItem: <T>(key: string, defaultValue?: T): T | null => {
    try {
      const item = localStorage.getItem(key);
      if (item === null) {
        return defaultValue || null;
      }
      return JSON.parse(item);
    } catch (error) {
      console.error('LocalStorage 읽기 실패:', error);
      return defaultValue || null;
    }
  },

  removeLocalItem: (key: string): void => {
    try {
      localStorage.removeItem(key);
    } catch (error) {
      console.error('LocalStorage 삭제 실패:', error);
    }
  },

  clearLocalStorage: (): void => {
    try {
      localStorage.clear();
    } catch (error) {
      console.error('LocalStorage 전체 삭제 실패:', error);
    }
  },

  // Session Storage
  setSessionItem: (key: string, value: any): void => {
    try {
      const serializedValue = JSON.stringify(value);
      sessionStorage.setItem(key, serializedValue);
    } catch (error) {
      console.error('SessionStorage 저장 실패:', error);
    }
  },

  getSessionItem: <T>(key: string, defaultValue?: T): T | null => {
    try {
      const item = sessionStorage.getItem(key);
      if (item === null) {
        return defaultValue || null;
      }
      return JSON.parse(item);
    } catch (error) {
      console.error('SessionStorage 읽기 실패:', error);
      return defaultValue || null;
    }
  },

  removeSessionItem: (key: string): void => {
    try {
      sessionStorage.removeItem(key);
    } catch (error) {
      console.error('SessionStorage 삭제 실패:', error);
    }
  },

  clearSessionStorage: (): void => {
    try {
      sessionStorage.clear();
    } catch (error) {
      console.error('SessionStorage 전체 삭제 실패:', error);
    }
  },

  // Cookie
  setCookie: (name: string, value: string, days: number = 7): void => {
    try {
      const expires = new Date();
      expires.setTime(expires.getTime() + (days * 24 * 60 * 60 * 1000));
      document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/`;
    } catch (error) {
      console.error('Cookie 설정 실패:', error);
    }
  },

  getCookie: (name: string): string | null => {
    try {
      const nameEQ = name + '=';
      const ca = document.cookie.split(';');
      for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
      }
      return null;
    } catch (error) {
      console.error('Cookie 읽기 실패:', error);
      return null;
    }
  },

  removeCookie: (name: string): void => {
    try {
      document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;`;
    } catch (error) {
      console.error('Cookie 삭제 실패:', error);
    }
  },

  // IndexedDB (간단한 래퍼)
  setIndexedDBItem: async (dbName: string, storeName: string, key: string, value: any): Promise<void> => {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(dbName, 1);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        const db = request.result;
        const transaction = db.transaction([storeName], 'readwrite');
        const store = transaction.objectStore(storeName);
        const putRequest = store.put(value, key);
        
        putRequest.onsuccess = () => resolve();
        putRequest.onerror = () => reject(putRequest.error);
      };
      
      request.onupgradeneeded = () => {
        const db = request.result;
        if (!db.objectStoreNames.contains(storeName)) {
          db.createObjectStore(storeName);
        }
      };
    });
  },

  getIndexedDBItem: async <T>(dbName: string, storeName: string, key: string): Promise<T | null> => {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(dbName, 1);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        const db = request.result;
        const transaction = db.transaction([storeName], 'readonly');
        const store = transaction.objectStore(storeName);
        const getRequest = store.get(key);
        
        getRequest.onsuccess = () => resolve(getRequest.result || null);
        getRequest.onerror = () => reject(getRequest.error);
      };
      
      request.onupgradeneeded = () => {
        const db = request.result;
        if (!db.objectStoreNames.contains(storeName)) {
          db.createObjectStore(storeName);
        }
      };
    });
  },

  // 유틸리티 함수들
  isStorageAvailable: (type: 'localStorage' | 'sessionStorage'): boolean => {
    try {
      const storage = type === 'localStorage' ? localStorage : sessionStorage;
      const testKey = '__storage_test__';
      storage.setItem(testKey, 'test');
      storage.removeItem(testKey);
      return true;
    } catch (error) {
      return false;
    }
  },

  getStorageSize: (type: 'localStorage' | 'sessionStorage'): number => {
    try {
      const storage = type === 'localStorage' ? localStorage : sessionStorage;
      let total = 0;
      for (let key in storage) {
        if (storage.hasOwnProperty(key)) {
          total += storage[key].length + key.length;
        }
      }
      return total;
    } catch (error) {
      console.error('Storage 크기 계산 실패:', error);
      return 0;
    }
  },

  // 암호화된 저장 (간단한 Base64 인코딩)
  setEncryptedItem: (key: string, value: any, password: string): void => {
    try {
      const serializedValue = JSON.stringify(value);
      const encryptedValue = btoa(serializedValue + password);
      localStorage.setItem(key, encryptedValue);
    } catch (error) {
      console.error('암호화 저장 실패:', error);
    }
  },

  getEncryptedItem: <T>(key: string, password: string, defaultValue?: T): T | null => {
    try {
      const encryptedValue = localStorage.getItem(key);
      if (!encryptedValue) {
        return defaultValue || null;
      }
      
      const decryptedValue = atob(encryptedValue);
      const originalValue = decryptedValue.replace(password, '');
      return JSON.parse(originalValue);
    } catch (error) {
      console.error('암호화 해독 실패:', error);
      return defaultValue || null;
    }
  },
};

// 스토리지 키 상수
export const STORAGE_KEYS = {
  USER_INFO: 'user_info',
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
  THEME: 'theme',
  LANGUAGE: 'language',
  RECENT_SEARCHES: 'recent_searches',
  FAVORITES: 'favorites',
  SETTINGS: 'settings',
  CACHE: 'cache',
} as const;

// 스토리지 만료 시간 (밀리초)
export const STORAGE_EXPIRY = {
  ACCESS_TOKEN: 24 * 60 * 60 * 1000, // 24시간
  REFRESH_TOKEN: 7 * 24 * 60 * 60 * 1000, // 7일
  USER_INFO: 24 * 60 * 60 * 1000, // 24시간
  CACHE: 60 * 60 * 1000, // 1시간
} as const;
