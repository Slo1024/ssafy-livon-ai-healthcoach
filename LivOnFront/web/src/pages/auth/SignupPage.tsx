import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import styled from 'styled-components';
import visibilityIcon from '../../assets/images/visibility.png';
import visibilityOffIcon from '../../assets/images/visibility_off.png';

const SignupContainer = styled.div`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  padding: 20px;
`;

const SignupTitle = styled.div`
  text-align: center;
  margin-bottom: 40px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 700;
  font-size: 32px;
  color: #000000;
  line-height: 1.4;
`;

const FormContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 10px;
  background-color: #ffffff;
  padding: 30px;
  width: 450px;
  border-radius: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  font-family: Pretendard, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const InputForm = styled.div`
  border: 1.5px solid #ecedec;
  border-radius: 10px;
  height: 50px;
  display: flex;
  align-items: center;
  padding-left: 10px;
  transition: 0.2s ease-in-out;

  &:focus-within {
    border: 1.5px solid #2d79f3;
  }
`;

const Input = styled.input`
  margin-left: 10px;
  border-radius: 10px;
  border: none;
  width: 85%;
  height: 100%;
  font-family: Pretendard, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &:focus {
    outline: none;
  }

  &::placeholder {
    font-family: Pretendard, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  }
`;

const PasswordToggle = styled.button`
  background: none;
  border: none;
  cursor: pointer;
  padding: 5px;
  margin-right: 10px;
`;

const FlexRow = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  justify-content: space-between;
  margin: 10px 0;
`;

const CheckboxLabel = styled.label`
  font-size: 14px;
  color: black;
  font-weight: 400;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
`;

const Checkbox = styled.input`
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid #ecedec;
  cursor: pointer;
`;

const ButtonSubmit = styled.button`
  margin: 20px 0 10px 0;
  background-color: #77a3f3;
  border: none;
  color: white;
  font-size: 15px;
  font-weight: 500;
  border-radius: 10px;
  height: 50px;
  width: 100%;
  cursor: pointer;
  font-family: Pretendard, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &:hover {
    background-color: #4965f6;
  }
`;

const SocialButton = styled.button`
  margin-top: 10px;
  width: 100%;
  height: 50px;
  border-radius: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: 500;
  gap: 10px;
  border: 1px solid #ededef;
  background-color: white;
  cursor: pointer;
  transition: 0.2s ease-in-out;
  font-family: Pretendard, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &:hover {
    border: 1px solid #2d79f3;
  }
`;

const GoogleButton = styled(SocialButton)`
  background-color: white;
  color: black;
`;

const KakaoButton = styled(SocialButton)`
  background-color: #FEE500;
  color: black;
`;

const NaverButton = styled(SocialButton)`
  background-color: #03C75A;
  color: white;
`;

const LoginLink = styled.p`
  text-align: center;
  color: black;
  font-size: 14px;
  margin: 20px 0 5px 0;
  font-family: Pretendard, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const LoginSpan = styled.span`
  color: #878787;
  font-weight: 500;
  cursor: pointer;
`;

export const SignupPage: React.FC = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [agreeTerms, setAgreeTerms] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log('Signup attempt:', formData);
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <SignupContainer>
      <SignupTitle>
        코치님, 기다리고 있을 회원들을 위해<br />
        회원가입을 해주세요.
      </SignupTitle>

      <FormContainer>
        <form onSubmit={handleSubmit}>
          <InputForm>
            <Input
              type="text"
              placeholder="이름을 입력하세요"
              value={formData.name}
              onChange={(e) => handleInputChange('name', e.target.value)}
              required
            />
          </InputForm>

          <InputForm>
            <Input
              type="email"
              placeholder="이메일을 입력하세요"
              value={formData.email}
              onChange={(e) => handleInputChange('email', e.target.value)}
              required
            />
          </InputForm>

          <InputForm>
            <Input
              type={showPassword ? 'text' : 'password'}
              placeholder="비밀번호를 입력하세요"
              value={formData.password}
              onChange={(e) => handleInputChange('password', e.target.value)}
              required
            />
            <PasswordToggle
              type="button"
              onClick={() => setShowPassword(!showPassword)}
            >
              <img 
                src={showPassword ? visibilityIcon : visibilityOffIcon} 
                alt={showPassword ? "비밀번호 숨기기" : "비밀번호 보기"} 
                width="20" 
                height="20" 
              />
            </PasswordToggle>
          </InputForm>

          <InputForm>
            <Input
              type={showConfirmPassword ? 'text' : 'password'}
              placeholder="비밀번호를 다시 입력하세요"
              value={formData.confirmPassword}
              onChange={(e) => handleInputChange('confirmPassword', e.target.value)}
              required
            />
            <PasswordToggle
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            >
              <img 
                src={showConfirmPassword ? visibilityIcon : visibilityOffIcon} 
                alt={showConfirmPassword ? "비밀번호 숨기기" : "비밀번호 보기"} 
                width="20" 
                height="20" 
              />
            </PasswordToggle>
          </InputForm>

          <FlexRow>
            <CheckboxLabel>
              <Checkbox
                type="checkbox"
                checked={agreeTerms}
                onChange={(e) => setAgreeTerms(e.target.checked)}
              />
              이용약관 및 개인정보처리방침에 동의합니다
            </CheckboxLabel>
          </FlexRow>

          <ButtonSubmit type="submit">회원가입</ButtonSubmit>
        </form>

        <LoginLink>
          이미 계정이 있으신가요? <Link to="/auth/login"><LoginSpan>로그인</LoginSpan></Link>
        </LoginLink>
      </FormContainer>
    </SignupContainer>
  );
};
