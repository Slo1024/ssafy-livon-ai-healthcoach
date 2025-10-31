import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import visibilityIcon from '../../assets/images/visibility.png';
import visibilityOffIcon from '../../assets/images/visibility_off.png';
import profilePictureIcon from '../../assets/images/profile_picture.png';

const SignupContainer = styled.div`
  min-height: 100vh;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 40px 20px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  overflow-x: auto;
  width: 100%;
`;

const SignupContentWrapper = styled.div`
  width: 810px;
  min-width: 810px;
  background-color: #ffffff;
  padding: 30px 40px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const WelcomeMessage = styled.div`
  text-align: center;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 32px;
  font-weight: 700;
  color: #000000;
  margin-bottom: 40px;
  line-height: 1.5;
`;

const SignupTitle = styled.div`
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 25px;
`;

const SignupTitleMain = styled.span`
  font-size: 32px;
  font-weight: 800;
  color: #000000;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const SignupTitleSub = styled.span`
  font-size: 20px;
  font-weight: 200;
  color: #000000;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const ProfileSection = styled.div`
  display: flex;
  gap: 20px;
  margin-bottom: 25px;
  align-items: flex-start;
`;

const ProfileImageContainer = styled.div`
  width: 197px;
  height: 263px;
  border-radius: 8px;
  background-color: #ffffff;
  border: 1px solid #dee2e6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
`;

const ProfileImagePlaceholder = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  color: #adb5bd;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const ProfileImage = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;

const ProfileImageInput = styled.input`
  display: none;
`;

const ProfileInfo = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
`;

const ProfileLabel = styled.h3`
  font-size: 10px;
  font-weight: 800;
  color: #000000;
  margin: 0;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const FileNameLabel = styled.label`
  font-size: 13px;
  font-weight: 500;
  color: #000000;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  margin-right: 10px;
  white-space: nowrap;
`;

const FileNameContainer = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
`;

const FileNameInput = styled.input`
  flex: 1;
  height: 36px;
  border: 1px solid #ecedec;
  border-radius: 12px;
  padding: 0 10px;
  font-size: 13px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &:focus {
    outline: none;
    border-color: #2d79f3;
  }
`;

const FileButton = styled.button<{ variant?: 'primary' | 'danger' }>`
  height: 36px;
  padding: 0 16px;
  border: ${props => props.variant === 'danger' ? '1px solid #ff0000' : 'none'};
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background-color: ${props => props.variant === 'danger' ? '#ffffff' : '#2d79f3'};
  color: ${props => props.variant === 'danger' ? '#ff0000' : 'white'};
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &:hover {
    background-color: ${props => props.variant === 'danger' ? '#ff0000' : '#1a5fd9'};
    color: ${props => props.variant === 'danger' ? '#ffffff' : 'white'};
  }
`;

const ProfileDescription = styled.div`
  font-size: 11px;
  color: #666666;
  line-height: 1.5;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const FormGrid = styled.div`
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px; /* reduce gap to pull right column slightly left */
  margin-bottom: 25px;
`;

const FormColumn = styled.div`
  display: flex;
  flex-direction: column;
  gap: 15px;
`;

const FormField = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 3px; /* tighter label-input spacing */
  width: 100%;
`;

const FormLabel = styled.label`
  font-size: 13px;
  font-weight: 500;
  color: #000000;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  width: 120px;
  flex-shrink: 0;
  white-space: nowrap;
  margin-left: 20px;
`;

const InputWithButton = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  flex: 1;
  width: 100%;
`;

const StyledInput = styled.input`
  flex: 1;
  height: 48px;
  border: 1px solid #ecedec;
  border-radius: 12px;
  padding: 0 12px;
  font-size: 13px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  min-width: 0;
  line-height: 48px;

  &:focus {
    outline: none;
    border-color: #2d79f3;
  }

  &::placeholder {
    color: #999999;
    font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  }
`;

const SmallButton = styled.button`
  height: 40px;
  padding: 0 16px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background-color: #2d79f3;
  color: white;
  white-space: nowrap;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &:hover {
    background-color: #1a5fd9;
  }
`;

const PasswordInputContainer = styled.div`
  position: relative;
  display: flex;
  align-items: center;
  flex: 1;
`;

const PasswordToggleButton = styled.button`
  position: absolute;
  right: 12px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const PasswordMatchIndicator = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #28a745;
  margin-top: 5px;
  margin-left: 135px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  white-space: nowrap;
  width: 100%;
`;

const PasswordConfirmWrapper = styled.div`
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 5px;
`;

const RadioGroup = styled.div`
  display: flex;
  gap: 20px;
  align-items: center;
  flex: 1;
`;

const RadioLabel = styled.label`
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const RadioInput = styled.input`
  width: 18px;
  height: 18px;
  cursor: pointer;
`;

const QualificationInput = styled.div`
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
`;

const QualificationList = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

const QualificationItem = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const AddButton = styled.button`
  align-self: flex-end;
  margin-top: 0;
  margin-bottom: 12px; /* add space before 소개 섹션 */
  border: none;
  background: transparent;
  padding: 0;
  font-size: 13px;
  font-weight: 500; /* Pretendard Medium */
  color: #000000;
  cursor: pointer;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const TextArea = styled.textarea`
  width: 100%;
  height: 48px;
  min-height: 48px;
  border: 1px solid #ecedec;
  border-radius: 12px;
  padding: 0 12px;
  font-size: 13px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  line-height: 48px;
  resize: none;
  overflow: hidden;
  -ms-overflow-style: none; /* IE and Edge */
  scrollbar-width: none; /* Firefox */
  &::-webkit-scrollbar { /* Chrome, Safari, Opera */
    display: none;
  }

  &:focus {
    outline: none;
    border-color: #2d79f3;
  }

  &::placeholder {
    color: #999999;
    font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  }
`;

const CharacterCounter = styled.div`
  text-align: right;
  font-size: 12px;
  color: #999999;
  margin-top: 2px;
  margin-bottom: 16px; /* add more space above divider */
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const EmailInputContainer = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  flex: 1;
  width: 100%;
`;

const EmailSeparator = styled.span`
  font-size: 16px;
  color: #000000;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const StyledDropdown = styled.select<{ isPlaceholder?: boolean }>`
  height: 48px;
  line-height: 48px;
  border: 1px solid #ecedec;
  border-radius: 12px;
  padding: 0 12px;
  font-size: 13px;
  color: ${props => (props.isPlaceholder ? '#999999' : '#000000')};
  background-color: white;
  cursor: pointer;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  flex: 1;
  min-width: 0;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;

  &:focus {
    outline: none;
    border-color: #2d79f3;
  }

  &::placeholder {
    color: #999999;
  }

  option {
    color: #000000;
  }
  option[disabled] {
    color: #999999;
  }
`;

const VerificationButton = styled.button`
  height: 40px;
  padding: 0 16px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background-color: #e9ecef;
  color: #495057;
  white-space: nowrap;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &:hover {
    background-color: #dee2e6;
  }
`;

const ConsentSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 25px;
  padding-top: 15px;
  border-top: 1px solid #ecedec;
`;

const ConsentItem = styled.label`
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 14px;
  color: #000000;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const ConsentCheckbox = styled.input`
  width: 18px;
  height: 18px;
  cursor: pointer;
`;

const ArrowIcon = styled.span`
  margin-left: auto;
  color: #999999;
  font-size: 14px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  cursor: pointer;
`;

// Modal for consent details
const ModalOverlay = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
`;

const ModalContent = styled.div`
  width: 640px;
  max-width: 90vw;
  max-height: 80vh;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  overflow: hidden;
  display: flex;
  flex-direction: column;
`;

const ModalHeader = styled.div`
  padding: 16px 20px;
  border-bottom: 1px solid #ecedec;
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const ModalTitle = styled.h3`
  margin: 0;
  font-size: 18px;
  font-weight: 700;
`;

const ModalClose = styled.button`
  border: none;
  background: transparent;
  font-size: 18px;
  cursor: pointer;
`;

const ModalBody = styled.div`
  padding: 16px 20px;
  overflow-y: auto;
  line-height: 1.6;
  color: #333333;
  font-size: 14px;
`;

const SubmitButton = styled.button`
  width: 686px;
  height: 80px;
  border: none;
  border-radius: 8px;
  font-size: 25px;
  font-weight: 800;
  cursor: pointer;
  background-color: #2d79f3;
  color: white;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &:hover {
    background-color: #1a5fd9;
  }
`;

const CheckIcon = styled.span`
  color: #28a745;
  font-size: 16px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

export const SignupPage: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    userId: '',
    name: '',
    nickname: '',
    password: '',
    confirmPassword: '',
    gender: 'male',
    birthDate: '',
    qualifications: [] as string[],
    qualificationInput: '',
    introduction: '',
    contact: '',
    emailId: '',
    emailDomain: '',
    verificationCode: '',
    affiliation: '',
    job: '',
    profileImage: null as File | null,
    profileFileName: '',
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [passwordMatch, setPasswordMatch] = useState(false);
  const [agreePersonalInfo, setAgreePersonalInfo] = useState(false);
  const [agreeThirdParty, setAgreeThirdParty] = useState(false);
  const [agreeAll, setAgreeAll] = useState(false);
  const [emailDomains] = useState(['gmail.com', 'naver.com', 'daum.net', 'kakao.com']);
  const [showPersonalModal, setShowPersonalModal] = useState(false);
  const [showThirdPartyModal, setShowThirdPartyModal] = useState(false);

  // Keep '필수 약관에 모두 동의' in sync with the two required consents
  useEffect(() => {
    setAgreeAll(agreePersonalInfo && agreeThirdParty);
  }, [agreePersonalInfo, agreeThirdParty]);
  const [qualificationFields, setQualificationFields] = useState<string[]>(['']);

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    
    if (field === 'password' || field === 'confirmPassword') {
      const password = field === 'password' ? value : formData.password;
      const confirm = field === 'confirmPassword' ? value : formData.confirmPassword;
      setPasswordMatch(password !== '' && confirm !== '' && password === confirm);
    }
  };

  const handleProfileImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) {
        alert('파일 용량은 2MB 이하만 가능합니다.');
        return;
      }
      const validTypes = ['image/jpeg', 'image/jpg', 'image/png'];
      if (!validTypes.includes(file.type)) {
        alert('파일 형식은 JPG, JPEG 또는 PNG만 지원합니다.');
        return;
      }
      setFormData(prev => ({ ...prev, profileImage: file, profileFileName: file.name }));
    }
  };

  const handleDeleteProfile = () => {
    setFormData(prev => ({ ...prev, profileImage: null, profileFileName: '' }));
  };

  const handleAddQualification = () => {
    setQualificationFields(prev => [...prev, '']);
  };

  const handleRemoveQualification = (index: number) => {
    setQualificationFields(prev => prev.filter((_, i) => i !== index));
  };

  const handleAgreeAll = (checked: boolean) => {
    setAgreeAll(checked);
    setAgreePersonalInfo(checked);
    setAgreeThirdParty(checked);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log('Signup attempt:', formData);
    
    // 회원가입 완료 후 SignupCompletePage로 이동 (닉네임 전달)
    navigate('/auth/signup-complete', {
      state: {
        nickname: formData.nickname
      }
    });
  };

  const characterCount = formData.introduction.length;

  return (
    <SignupContainer>
      <WelcomeMessage>
        코치님, 기다리고 있을 회원들을 위해<br />
        회원가입을 해주세요.
      </WelcomeMessage>
      <SignupContentWrapper>
        <SignupTitle>
          <SignupTitleMain>회원가입</SignupTitleMain>
          <SignupTitleSub>Signup</SignupTitleSub>
        </SignupTitle>

        <form onSubmit={handleSubmit}>
        <ProfileSection>
          <ProfileImageContainer>
            {formData.profileImage ? (
              <ProfileImage
                src={URL.createObjectURL(formData.profileImage)}
                alt="Profile"
              />
            ) : (
              <ProfileImagePlaceholder>
                <img src={profilePictureIcon} alt="Profile Placeholder" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              </ProfileImagePlaceholder>
            )}
          </ProfileImageContainer>
          <ProfileInfo>
            <ProfileLabel>프로필 사진</ProfileLabel>
            <FileNameContainer>
              <FileNameLabel>파일명</FileNameLabel>
              <FileNameInput
                type="text"
                value={formData.profileFileName}
                readOnly
              />
              <FileButton
                type="button"
                onClick={() => document.getElementById('profile-input')?.click()}
              >
                파일 등록
              </FileButton>
              <FileButton
                type="button"
                variant="danger"
                onClick={handleDeleteProfile}
                disabled={!formData.profileImage}
              >
                파일 삭제
              </FileButton>
            </FileNameContainer>
            <ProfileDescription>
              프로필 사진은 300x400px 사이즈를 권장합니다.<br />
              파일 형식은 JPGE(.jpg, .jpeg) 또는 PNG(.png)만 지원합니다.<br />
              업로드 파일 용량은 2MB 이하만 가능합니다.
            </ProfileDescription>
            <ProfileImageInput
              id="profile-input"
              type="file"
              accept="image/jpeg,image/jpg,image/png"
              onChange={handleProfileImageChange}
            />
          </ProfileInfo>
        </ProfileSection>

        <FormGrid>
          <FormColumn>
            <FormField>
              <FormLabel>아이디</FormLabel>
              <InputWithButton>
                <StyledInput
                  type="text"
                  placeholder="아이디를 입력하세요"
                  value={formData.userId}
                  onChange={(e) => handleInputChange('userId', e.target.value)}
                />
                <SmallButton type="button">중복확인</SmallButton>
              </InputWithButton>
            </FormField>

            <FormField>
              <FormLabel>이름</FormLabel>
              <StyledInput
              type="text"
                placeholder="이름을 입력해주세요."
              value={formData.name}
              onChange={(e) => handleInputChange('name', e.target.value)}
              />
            </FormField>

            <FormField>
              <FormLabel>닉네임</FormLabel>
              <InputWithButton>
                <StyledInput
                  type="text"
                  placeholder="닉네임을 입력하세요"
                  value={formData.nickname}
                  onChange={(e) => handleInputChange('nickname', e.target.value)}
                />
                <SmallButton type="button">중복확인</SmallButton>
              </InputWithButton>
            </FormField>

            <FormField>
              <FormLabel>비밀번호</FormLabel>
              <PasswordInputContainer>
                <StyledInput
              type={showPassword ? 'text' : 'password'}
                  placeholder="비밀번호를 입력해주세요."
              value={formData.password}
              onChange={(e) => handleInputChange('password', e.target.value)}
                  style={{ paddingRight: '40px' }}
            />
                <PasswordToggleButton
              type="button"
              onClick={() => setShowPassword(!showPassword)}
            >
              <img 
                src={showPassword ? visibilityIcon : visibilityOffIcon} 
                    alt={showPassword ? '비밀번호 숨기기' : '비밀번호 보기'}
                width="20" 
                height="20" 
              />
                </PasswordToggleButton>
              </PasswordInputContainer>
            </FormField>

            <FormField style={{ alignItems: 'flex-start' }}>
              <FormLabel style={{ marginTop: '12px' }}>비밀번호 확인</FormLabel>
              <PasswordConfirmWrapper>
                <PasswordInputContainer>
                  <StyledInput
                    type={showConfirmPassword ? 'text' : 'password'}
                    placeholder="비밀번호를 한 번 더 입력해주세요."
                    value={formData.confirmPassword}
                    onChange={(e) => handleInputChange('confirmPassword', e.target.value)}
                    style={{ paddingRight: '40px' }}
                  />
                  <PasswordToggleButton
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  >
                    <img 
                      src={showConfirmPassword ? visibilityIcon : visibilityOffIcon} 
                      alt={showConfirmPassword ? '비밀번호 숨기기' : '비밀번호 보기'}
                      width="20" 
                      height="20" 
                    />
                  </PasswordToggleButton>
                </PasswordInputContainer>
                {passwordMatch && (
                  <PasswordMatchIndicator style={{ marginLeft: '0' }}>
                    <CheckIcon>✓</CheckIcon>
                    입력한 비밀번호와 일치합니다.
                  </PasswordMatchIndicator>
                )}
              </PasswordConfirmWrapper>
            </FormField>

            <FormField>
              <FormLabel>성별</FormLabel>
              <RadioGroup>
                <RadioLabel>
                  <RadioInput
                    type="radio"
                    name="gender"
                    value="male"
                    checked={formData.gender === 'male'}
                    onChange={(e) => handleInputChange('gender', e.target.value)}
                  />
                  남
                </RadioLabel>
                <RadioLabel>
                  <RadioInput
                    type="radio"
                    name="gender"
                    value="female"
                    checked={formData.gender === 'female'}
                    onChange={(e) => handleInputChange('gender', e.target.value)}
                  />
                  여
                </RadioLabel>
              </RadioGroup>
            </FormField>

            <FormField>
              <FormLabel>생년월일</FormLabel>
              <StyledInput
                type="text"
                placeholder="YYYY.MM.DD"
                value={formData.birthDate}
                onChange={(e) => handleInputChange('birthDate', e.target.value)}
              />
            </FormField>

            
          </FormColumn>

          <FormColumn>
            <FormField style={{ gap: '0px' }}>
              <FormLabel style={{ width: '90px' }}>연락처</FormLabel>
              <StyledInput
                type="tel"
                placeholder="연락처를 입력해주세요."
                value={formData.contact}
                onChange={(e) => handleInputChange('contact', e.target.value)}
              />
            </FormField>

            <FormField style={{ alignItems: 'flex-start', gap: '0px' }}>
              <FormLabel style={{ marginTop: '17px', width: '90px' }}>이메일</FormLabel>
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '10px', width: '100%' }}>
                <EmailInputContainer style={{ width: '100%' }}>
                  <StyledInput
                    type="text"
                    placeholder="이메일을 입력하세요."
                    value={formData.emailId}
                    onChange={(e) => handleInputChange('emailId', e.target.value)}
                    style={{ flex: 1 }}
                  />
                  <EmailSeparator style={{ marginLeft: '6px' }}>@</EmailSeparator>
                </EmailInputContainer>
                <StyledDropdown
                  value={formData.emailDomain}
                  onChange={(e) => handleInputChange('emailDomain', e.target.value)}
                  style={{ width: '100%', height: '48px' }}
                  isPlaceholder={formData.emailDomain === ''}
                >
                  <option value="" disabled>이메일 주소를 입력하세요.</option>
                  {emailDomains.map((domain) => (
                    <option key={domain} value={domain}>
                      {domain}
                    </option>
                  ))}
                </StyledDropdown>
                <VerificationButton type="button">인증번호 보내기</VerificationButton>
              </div>
            </FormField>

            <FormField style={{ alignItems: 'flex-start', gap: '0px' }}>
              <FormLabel style={{ marginTop: '17px', width: '90px' }}>인증하기</FormLabel>
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '10px', width: '100%' }}>
                <StyledInput
                  type="text"
                  placeholder="인증번호를 입력하세요."
                  value={formData.verificationCode}
                  onChange={(e) => handleInputChange('verificationCode', e.target.value)}
                  maxLength={6}
                  style={{ width: '100%' }}
                />
                <VerificationButton type="button">인증하기</VerificationButton>
              </div>
            </FormField>

            <FormField style={{ gap: '0px' }}>
              <FormLabel style={{ width: '90px' }}>소속</FormLabel>
              <StyledInput
                type="text"
                placeholder="소속을 입력해 주세요."
                value={formData.affiliation}
                onChange={(e) => handleInputChange('affiliation', e.target.value)}
              />
            </FormField>

            <FormField style={{ gap: '0px' }}>
              <FormLabel style={{ width: '90px' }}>직무</FormLabel>
              <StyledDropdown
                value={formData.job}
                onChange={(e) => handleInputChange('job', e.target.value)}
                isPlaceholder={formData.job === ''}
              >
                <option value="" disabled>코칭할 분야를 선택해 주세요.</option>
                <option value="exercise-fitness">운동/피트니스</option>
                <option value="diet-nutrition">식단/영양</option>
                <option value="physical-health">신체건강/통증 관리</option>
                <option value="mental-health">정신건강/멘탈케어</option>
              </StyledDropdown>
            </FormField>
          </FormColumn>
        </FormGrid>

        {/* Full-width sections: Qualification and Introduction */}
        <FormField style={{ alignItems: 'flex-start' }}>
          <div style={{ width: '120px', flexShrink: 0 }}>
            <FormLabel style={{ marginLeft: 0 }}>자격</FormLabel>
            <div style={{ fontSize: '10px', color: '#666666', marginTop: '8px', marginLeft: 0, whiteSpace: 'nowrap' }}>
              취득한 자격증을 입력해 주세요.
            </div>
          </div>
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '6px', position: 'relative' }}>
            {qualificationFields.map((value, index) => (
              <StyledInput
                key={index}
                type="text"
                placeholder="자격증 명을 입력해 주세요."
                value={value}
                onChange={(e) =>
                  setQualificationFields(prev => prev.map((v, i) => (i === index ? e.target.value : v)))
                }
              />
            ))}
            <AddButton type="button" onClick={handleAddQualification}>추가 +</AddButton>
          </div>
        </FormField>

        <FormField style={{ alignItems: 'flex-start' }}>
          <div style={{ width: '120px', flexShrink: 0, display: 'flex', flexDirection: 'column' }}>
            <FormLabel style={{ marginLeft: 0, marginBottom: '6px' }}>소개</FormLabel>
            <div style={{ fontSize: '10px', color: '#666666', marginLeft: 0, lineHeight: '1.2' }}>
              회원들에게 코치님을<br />소개하는 글을 입력해 주세요.
            </div>
          </div>
          <div style={{ flex: 1, alignSelf: 'flex-start' }}>
            <TextArea
              placeholder="소개 글을 입력해 주세요."
              value={formData.introduction}
              onChange={(e) => handleInputChange('introduction', e.target.value)}
              maxLength={50}
            />
            <CharacterCounter>{characterCount}/50</CharacterCounter>
          </div>
        </FormField>

        <ConsentSection>
          <ConsentItem>
            <ConsentCheckbox
              type="checkbox"
              checked={agreePersonalInfo}
              onChange={(e) => setAgreePersonalInfo(e.target.checked)}
            />
            개인정보 수집·이용 동의
            <ArrowIcon
              onMouseDown={(e) => { e.preventDefault(); e.stopPropagation(); }}
              onClick={(e) => { e.preventDefault(); e.stopPropagation(); setShowPersonalModal(true); }}
            >
              →
            </ArrowIcon>
          </ConsentItem>
          <ConsentItem>
            <ConsentCheckbox
              type="checkbox"
              checked={agreeThirdParty}
              onChange={(e) => setAgreeThirdParty(e.target.checked)}
            />
            개인정보 제3자 제공 동의
            <ArrowIcon
              onMouseDown={(e) => { e.preventDefault(); e.stopPropagation(); }}
              onClick={(e) => { e.preventDefault(); e.stopPropagation(); setShowThirdPartyModal(true); }}
            >
              →
            </ArrowIcon>
          </ConsentItem>
          <ConsentItem>
            <ConsentCheckbox
              type="checkbox"
              checked={agreeAll}
              onChange={(e) => handleAgreeAll(e.target.checked)}
            />
            필수 약관에 모두 동의
          </ConsentItem>
        </ConsentSection>

      {showPersonalModal && (
        <ModalOverlay onClick={() => setShowPersonalModal(false)}>
          <ModalContent onClick={(e) => e.stopPropagation()}>
            <ModalHeader>
              <ModalTitle>개인정보 수집·이용 동의</ModalTitle>
              <ModalClose onClick={() => setShowPersonalModal(false)}>✕</ModalClose>
            </ModalHeader>
            <ModalBody>
              <p>서비스 제공을 위해 다음의 개인정보를 수집·이용합니다.</p>
              <ul>
                <li>수집 항목: 이름, 연락처, 이메일, 생년월일, 서비스 이용 기록</li>
                <li>수집 목적: 회원 식별, 서비스 제공 및 문의 응대, 공지 전달</li>
                <li>보유 기간: 회원 탈퇴 시까지 또는 관련 법령에 따른 보관 기간</li>
              </ul>
              <p>사용자는 동의를 거부할 권리가 있으며, 동의 거부 시 일부 서비스 이용이 제한될 수 있습니다.</p>
            </ModalBody>
          </ModalContent>
        </ModalOverlay>
      )}

      {showThirdPartyModal && (
        <ModalOverlay onClick={() => setShowThirdPartyModal(false)}>
          <ModalContent onClick={(e) => e.stopPropagation()}>
            <ModalHeader>
              <ModalTitle>개인정보 제3자 제공 동의</ModalTitle>
              <ModalClose onClick={() => setShowThirdPartyModal(false)}>✕</ModalClose>
            </ModalHeader>
            <ModalBody>
              <p>서비스 운영 및 고객 지원을 위해 아래와 같이 제3자에게 개인정보를 제공합니다.</p>
              <ul>
                <li>제공 받는 자: 서비스 제공사, 고객지원 대행사</li>
                <li>제공 항목: 이름, 연락처, 이메일, 상담 관련 정보(필요 시)</li>
                <li>제공 목적: 결제 처리, 알림 발송, 고객 문의 처리</li>
                <li>보유 기간: 제공 목적 달성 시까지 또는 관련 법령에 따른 보관 기간</li>
              </ul>
              <p>사용자는 동의를 거부할 권리가 있으며, 동의 거부 시 해당 목적의 서비스 이용이 제한될 수 있습니다.</p>
            </ModalBody>
          </ModalContent>
        </ModalOverlay>
      )}

        <div style={{ display: 'flex', justifyContent: 'center', width: '100%' }}>
          <SubmitButton type="submit">회원가입 완료</SubmitButton>
        </div>
        </form>
      </SignupContentWrapper>
    </SignupContainer>
  );
};
