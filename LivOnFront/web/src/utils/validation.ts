// 유효성 검사 관련 유틸리티 함수들
export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const validatePassword = (password: string): { isValid: boolean; errors: string[] } => {
  const errors: string[] = [];
  
  if (password.length < 8) {
    errors.push('비밀번호는 8자 이상이어야 합니다.');
  }
  
  if (!/[A-Z]/.test(password)) {
    errors.push('비밀번호는 대문자를 포함해야 합니다.');
  }
  
  if (!/[a-z]/.test(password)) {
    errors.push('비밀번호는 소문자를 포함해야 합니다.');
  }
  
  if (!/\d/.test(password)) {
    errors.push('비밀번호는 숫자를 포함해야 합니다.');
  }
  
  if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    errors.push('비밀번호는 특수문자를 포함해야 합니다.');
  }
  
  return {
    isValid: errors.length === 0,
    errors,
  };
};

export const validatePhoneNumber = (phoneNumber: string): boolean => {
  const phoneRegex = /^01[016789]-\d{3,4}-\d{4}$/;
  return phoneRegex.test(phoneNumber);
};

export const validateKoreanPhoneNumber = (phoneNumber: string): boolean => {
  const cleaned = phoneNumber.replace(/\D/g, '');
  return cleaned.length === 11 && cleaned.startsWith('01');
};

export const validateSocialSecurityNumber = (ssn: string): boolean => {
  const cleaned = ssn.replace(/\D/g, '');
  if (cleaned.length !== 13) return false;
  
  // 주민등록번호 체크섬 검증
  const weights = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5];
  let sum = 0;
  
  for (let i = 0; i < 12; i++) {
    sum += parseInt(cleaned[i]) * weights[i];
  }
  
  const remainder = sum % 11;
  const checkDigit = remainder < 2 ? remainder : 11 - remainder;
  
  return checkDigit === parseInt(cleaned[12]);
};

export const validateBusinessNumber = (businessNumber: string): boolean => {
  const cleaned = businessNumber.replace(/\D/g, '');
  if (cleaned.length !== 10) return false;
  
  // 사업자등록번호 체크섬 검증
  const weights = [1, 3, 7, 1, 3, 7, 1, 3, 5];
  let sum = 0;
  
  for (let i = 0; i < 9; i++) {
    sum += parseInt(cleaned[i]) * weights[i];
  }
  
  const remainder = sum % 10;
  const checkDigit = remainder === 0 ? 0 : 10 - remainder;
  
  return checkDigit === parseInt(cleaned[9]);
};

export const validateUrl = (url: string): boolean => {
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

export const validateKoreanName = (name: string): boolean => {
  const koreanRegex = /^[가-힣]{2,10}$/;
  return koreanRegex.test(name);
};

export const validateEnglishName = (name: string): boolean => {
  const englishRegex = /^[a-zA-Z\s]{2,50}$/;
  return englishRegex.test(name);
};

export const validateNickname = (nickname: string): boolean => {
  const nicknameRegex = /^[a-zA-Z0-9가-힣_]{2,20}$/;
  return nicknameRegex.test(nickname);
};

export const validateAge = (birthDate: Date | string): boolean => {
  const birth = typeof birthDate === 'string' ? new Date(birthDate) : birthDate;
  const today = new Date();
  const age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    return age - 1 >= 14 && age - 1 <= 100;
  }
  
  return age >= 14 && age <= 100;
};

export const validateFileSize = (file: File, maxSizeInMB: number): boolean => {
  const maxSizeInBytes = maxSizeInMB * 1024 * 1024;
  return file.size <= maxSizeInBytes;
};

export const validateFileType = (file: File, allowedTypes: string[]): boolean => {
  return allowedTypes.includes(file.type);
};

export const validateImageFile = (file: File): boolean => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
  return validateFileType(file, allowedTypes);
};

export const validateRequired = (value: any): boolean => {
  if (typeof value === 'string') {
    return value.trim().length > 0;
  }
  if (Array.isArray(value)) {
    return value.length > 0;
  }
  return value !== null && value !== undefined;
};

export const validateMinLength = (value: string, minLength: number): boolean => {
  return value.length >= minLength;
};

export const validateMaxLength = (value: string, maxLength: number): boolean => {
  return value.length <= maxLength;
};

export const validateRange = (value: number, min: number, max: number): boolean => {
  return value >= min && value <= max;
};

export const validatePositiveNumber = (value: number): boolean => {
  return value > 0;
};

export const validateNonNegativeNumber = (value: number): boolean => {
  return value >= 0;
};

export const validateInteger = (value: number): boolean => {
  return Number.isInteger(value);
};

export const validateDecimal = (value: number, decimalPlaces: number): boolean => {
  const regex = new RegExp(`^\\d+\\.\\d{1,${decimalPlaces}}$`);
  return regex.test(value.toString());
};

export const validateCreditCard = (cardNumber: string): boolean => {
  const cleaned = cardNumber.replace(/\D/g, '');
  if (cleaned.length < 13 || cleaned.length > 19) return false;
  
  // Luhn 알고리즘 검증
  let sum = 0;
  let isEven = false;
  
  for (let i = cleaned.length - 1; i >= 0; i--) {
    let digit = parseInt(cleaned[i]);
    
    if (isEven) {
      digit *= 2;
      if (digit > 9) {
        digit -= 9;
      }
    }
    
    sum += digit;
    isEven = !isEven;
  }
  
  return sum % 10 === 0;
};

export const validatePostalCode = (postalCode: string): boolean => {
  const koreanPostalRegex = /^\d{5}$/;
  return koreanPostalRegex.test(postalCode);
};
