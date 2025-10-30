import React, { useState } from 'react';
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
  font-size: 24px;
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
  border-radius: 6px;
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
  gap: 20px;
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
  gap: 15px;
`;

const FormLabel = styled.label`
  font-size: 13px;
  font-weight: 500;
  color: #000000;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  width: 120px;
  flex-shrink: 0;
  white-space: nowrap;
`;

const InputWithButton = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  flex: 1;
`;

const StyledInput = styled.input`
  flex: 1;
  height: 40px;
  border: 1px solid #ecedec;
  border-radius: 6px;
  padding: 0 12px;
  font-size: 13px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  min-width: 0;

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
  height: 36px;
  padding: 0 16px;
  border: 1px solid #2d79f3;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background-color: white;
  color: #2d79f3;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  &:hover {
    background-color: #f0f7ff;
  }
`;

const TextArea = styled.textarea`
  width: 100%;
  min-height: 100px;
  border: 1px solid #ecedec;
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 13px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  resize: vertical;

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
  margin-top: 5px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const EmailInputContainer = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  flex: 1;
`;

const EmailSeparator = styled.span`
  font-size: 16px;
  color: #000000;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const StyledDropdown = styled.select`
  height: 40px;
  border: 1px solid #ecedec;
  border-radius: 6px;
  padding: 0 12px;
  font-size: 13px;
  background-color: white;
  cursor: pointer;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  flex: 1;
  min-width: 0;

  &:focus {
    outline: none;
    border-color: #2d79f3;
  }

  &::placeholder {
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
`;

const SubmitButton = styled.button`
  width: 686px;
  height: 80px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
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
    if (formData.qualificationInput.trim()) {
      setFormData(prev => ({
        ...prev,
        qualifications: [...prev.qualifications, prev.qualificationInput.trim()],
        qualificationInput: '',
      }));
    }
  };

  const handleRemoveQualification = (index: number) => {
    setFormData(prev => ({
      ...prev,
      qualifications: prev.qualifications.filter((_, i) => i !== index),
    }));
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

            <FormField>
              <FormLabel>비밀번호 확인</FormLabel>
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

            <FormField>
              <FormLabel>자격</FormLabel>
              <QualificationInput>
                <FormLabel style={{ fontSize: '12px', fontWeight: 400 }}>
                  취득한 자격증을 입력해 주세요.
                </FormLabel>
                <InputWithButton>
                  <StyledInput
                    type="text"
                    placeholder="자격증 명을 입력해 주세요."
                    value={formData.qualificationInput}
                    onChange={(e) => handleInputChange('qualificationInput', e.target.value)}
                    onKeyPress={(e) => {
                      if (e.key === 'Enter') {
                        e.preventDefault();
                        handleAddQualification();
                      }
                    }}
                  />
                  <AddButton type="button" onClick={handleAddQualification}>
                    추가 +
                  </AddButton>
                </InputWithButton>
                {formData.qualifications.length > 0 && (
                  <QualificationList>
                    {formData.qualifications.map((qual, index) => (
                      <QualificationItem key={index}>
                        <span>{qual}</span>
                        <button
                          type="button"
                          onClick={() => handleRemoveQualification(index)}
                          style={{
                            marginLeft: 'auto',
                            background: 'none',
                            border: 'none',
                            color: '#dc3545',
                            cursor: 'pointer',
                            fontSize: '14px',
                            fontFamily: "'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
                          }}
                        >
                          삭제
                        </button>
                      </QualificationItem>
                    ))}
                  </QualificationList>
                )}
              </QualificationInput>
            </FormField>

            <FormField>
              <FormLabel>소개</FormLabel>
              <div style={{ flex: 1 }}>
                <FormLabel style={{ fontSize: '12px', fontWeight: 400, marginBottom: '8px', display: 'block' }}>
                  회원들에게 코치님을 소개하는 글을 입력해 주세요.
                </FormLabel>
                <TextArea
                  placeholder="소개 글을 입력해 주세요."
                  value={formData.introduction}
                  onChange={(e) => handleInputChange('introduction', e.target.value)}
                  maxLength={50}
                />
                <CharacterCounter>{characterCount}/50</CharacterCounter>
              </div>
            </FormField>
          </FormColumn>

          <FormColumn>
            <FormField>
              <FormLabel>연락처</FormLabel>
              <StyledInput
                type="tel"
                placeholder="연락처를 입력해주세요."
                value={formData.contact}
                onChange={(e) => handleInputChange('contact', e.target.value)}
              />
            </FormField>

            <FormField>
              <FormLabel>이메일</FormLabel>
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <EmailInputContainer>
                  <StyledInput
                    type="text"
                    placeholder="이메일을 입력하세요"
                    value={formData.emailId}
                    onChange={(e) => handleInputChange('emailId', e.target.value)}
                  />
                  <EmailSeparator>@</EmailSeparator>
                  <StyledDropdown
                    value={formData.emailDomain}
                    onChange={(e) => handleInputChange('emailDomain', e.target.value)}
                  >
                    <option value="" disabled>이메일 주소를 입력하세요</option>
                    {emailDomains.map((domain) => (
                      <option key={domain} value={domain}>
                        {domain}
                      </option>
                    ))}
                  </StyledDropdown>
                </EmailInputContainer>
                <VerificationButton type="button">인증번호 보내기</VerificationButton>
              </div>
            </FormField>

            <FormField>
              <FormLabel>인증하기</FormLabel>
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <StyledInput
                  type="text"
                  placeholder="6자리 인증번호"
                  value={formData.verificationCode}
                  onChange={(e) => handleInputChange('verificationCode', e.target.value)}
                  maxLength={6}
                />
                <VerificationButton type="button">인증하기</VerificationButton>
              </div>
            </FormField>

            <FormField>
              <FormLabel>소속</FormLabel>
              <StyledInput
                type="text"
                placeholder="소속을 입력해 주세요."
                value={formData.affiliation}
                onChange={(e) => handleInputChange('affiliation', e.target.value)}
              />
            </FormField>

            <FormField>
              <FormLabel>직무</FormLabel>
              <StyledDropdown
                value={formData.job}
                onChange={(e) => handleInputChange('job', e.target.value)}
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

        <ConsentSection>
          <ConsentItem>
            <ConsentCheckbox
              type="checkbox"
              checked={agreePersonalInfo}
              onChange={(e) => setAgreePersonalInfo(e.target.checked)}
            />
            개인정보 수집·이용 동의
            <ArrowIcon>→</ArrowIcon>
          </ConsentItem>
          <ConsentItem>
            <ConsentCheckbox
              type="checkbox"
              checked={agreeThirdParty}
              onChange={(e) => setAgreeThirdParty(e.target.checked)}
            />
            개인정보 제3자 제공 동의
            <ArrowIcon>→</ArrowIcon>
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

        <div style={{ display: 'flex', justifyContent: 'center', width: '100%' }}>
          <SubmitButton type="submit">회원가입 완료</SubmitButton>
        </div>
        </form>
      </SignupContentWrapper>
    </SignupContainer>
  );
};
