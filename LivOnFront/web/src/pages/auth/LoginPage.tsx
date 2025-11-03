import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import googleIcon from '../../assets/images/google-icon.png';
import kakaoIcon from '../../assets/images/Kakao.png';
import naverIcon from '../../assets/images/Naver logo.png';
import visibilityIcon from '../../assets/images/visibility.png';
import visibilityOffIcon from '../../assets/images/visibility_off.png';

const LoginContainer = styled.div`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  padding: 20px;
`;

const LoginTitle = styled.div`
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
  align-items: center;
  gap: 0;
  background-color: #ffffff;
  padding: 30px;
  width: 422px;
  height: 516px;
  border-radius: 32px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  font-family: Pretendard, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  form {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0;
    width: 100%;
  }
`;

const InputForm = styled.div`
  border: 1.5px solid #ecedec;
  border-radius: 6px;
  height: 48px;
  width: 342px;
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
  width: 342px;
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
  border-radius: 6px;
  border: 2px solid #ecedec;
  cursor: pointer;
`;

const ForgotPassword = styled.span`
  font-size: 14px;
  color: #4965f6;
  font-weight: 500;
  cursor: pointer;
`;

const ButtonSubmit = styled.button`
  margin: 20px 0 8px 0;
  background-color: #77a3f3;
  border: none;
  color: white;
  font-size: 15px;
  font-weight: 500;
  border-radius: 6px;
  height: 48px;
  width: 342px;
  cursor: pointer;
  font-family: Pretendard, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &:hover {
    background-color: #4965f6;
  }
`;

const SocialButton = styled.button`
  margin-top: 8px;
  width: 342px;
  height: 48px;
  border-radius: 6px;
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

const SignUpLink = styled.p`
  text-align: center;
  color: black;
  font-size: 14px;
  margin: 20px 0 5px 0;
  font-family: Pretendard, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const SignUpSpan = styled.span`
  color: #878787;
  font-weight: 500;
  cursor: pointer;
`;

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [keepLoggedIn, setKeepLoggedIn] = useState(false);


  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log('Login attempt:', { email, password, keepLoggedIn });
  };

  const handleGoogleLogin = () => {
    // 보류 중
  };

  const handleKakaoLogin = () => {
    // 보류 중
  };

  const handleNaverLogin = () => {
    // 보류 중
  };

  return (
    <LoginContainer>
      <LoginTitle>
        코치님, 기다리고 있을 회원들을 위해<br />
        로그인을 해주세요.
      </LoginTitle>

      <FormContainer>
        <form onSubmit={handleSubmit}>
          <InputForm style={{ marginBottom: '20px' }}>
            <Input
              type="email"
              placeholder="이메일을 입력하세요"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </InputForm>

          <InputForm>
            <Input
              type={showPassword ? 'text' : 'password'}
              placeholder="비밀번호를 입력하세요"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
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

          <FlexRow>
            <CheckboxLabel>
              <Checkbox
                type="checkbox"
                checked={keepLoggedIn}
                onChange={(e) => setKeepLoggedIn(e.target.checked)}
              />
              로그인 유지
            </CheckboxLabel>
            <ForgotPassword>비밀번호 찾기</ForgotPassword>
          </FlexRow>

          <ButtonSubmit type="submit">로그인</ButtonSubmit>
        </form>

        <GoogleButton type="button" onClick={handleGoogleLogin}>
          <img src={googleIcon} alt="Google" width="20" height="20" />
          구글 로그인
        </GoogleButton>

        <KakaoButton type="button" onClick={handleKakaoLogin}>
          <img src={kakaoIcon} alt="Kakao" width="20" height="20" />
          카카오 로그인
        </KakaoButton>

        <NaverButton type="button" onClick={handleNaverLogin}>
          <img src={naverIcon} alt="Naver" width="20" height="20" />
          네이버 로그인
        </NaverButton>

        <SignUpLink>
          <Link to="/auth/signup">
            <SignUpSpan>회원가입</SignUpSpan>
          </Link>
        </SignUpLink>
      </FormContainer>
    </LoginContainer>
  );
};
