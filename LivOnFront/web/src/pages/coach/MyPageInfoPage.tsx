import React, { useState } from 'react';
import styled from 'styled-components';
import { useAuth } from '../../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import { Input as CommonInput } from '../../components/common/Input';
import { Dropdown as CommonDropdown } from '../../components/common/Dropdown';
import { DateTimePickerModal } from '../../components/common/Modal';
import profilePictureIcon from '../../assets/images/profile_picture.png';
import { ROUTES } from '../../constants/routes';

const PageContainer = styled.div`
  min-height: 100vh;
  background-color: #ffffff;
  padding: clamp(24px, 5vw, 40px) clamp(16px, 4vw, 32px);
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const ContentWrapper = styled.div`
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
`;

const PageTitle = styled.h1`
  font-weight: 700;
  font-size: 40px;
  color: #000000;
  margin: 0 0 24px 0;
  text-align: left;

  @media (max-width: 1200px) {
    text-align: center;
    font-size: 34px;
  }

  @media (max-width: 900px) {
    font-size: 30px;
  }

  @media (max-width: 768px) {
    font-size: 26px;
  }

  @media (max-width: 480px) {
    font-size: 24px;
  }
`;

// 프로필 사진 업로드 섹션 스타일
const ProfileSection = styled.div`
  display: flex;
  gap: 20px;
  margin-top: 16px; /* 탭 가로줄과 약간의 간격 */
  align-items: flex-start;

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: center;
    width: 100%;
    gap: 24px;
  }
`;

const ProfileImageContainer = styled.div`
  width: clamp(160px, 30vw, 197px);
  height: clamp(200px, 36vw, 263px);
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
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
`;

const ProfileImage = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;

const ProfileInfo = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
`;

const ProfileLabel = styled.h3`
  font-size: 12px;
  font-weight: 800;
  color: #000000;
  margin: 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
`;

const FileNameContainer = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  width: 100%;
`;

const FileNameLabel = styled.label`
  font-size: 13px;
  font-weight: 500;
  color: #000000;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  margin-right: 10px;
  white-space: nowrap;
`;

const FileNameInput = styled.input`
  flex: 1;
  height: 36px;
  border: 1px solid #ecedec;
  border-radius: 12px;
  padding: 0 10px;
  font-size: 13px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:focus {
    outline: none;
    border-color: #2d79f3;
  }
`;

const FileButton = styled.button<{ variant?: "primary" | "danger" }>`
  height: 36px;
  padding: 0 16px;
  border: ${(props) =>
    props.variant === "danger" ? "1px solid #ff0000" : "none"};
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background-color: ${(props) =>
    props.variant === "danger" ? "#ffffff" : "#2d79f3"};
  color: ${(props) => (props.variant === "danger" ? "#ff0000" : "white")};
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:hover {
    background-color: ${(props) =>
      props.variant === "danger" ? "#ff0000" : "#1a5fd9"};
    color: ${(props) => (props.variant === "danger" ? "#ffffff" : "white")};
  }
`;

const ProfileDescription = styled.div`
  font-size: 11px;
  color: #666666;
  line-height: 1.5;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
`;

// SignupPage 유사 폼 레이아웃
const FormGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: clamp(16px, 3vw, 24px);
  margin-top: 24px;
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
  gap: 3px;
  width: 100%;

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
`;

const FormLabel = styled.label`
  font-size: 13px;
  font-weight: 500;
  color: #000000;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  width: 100px;
  flex-shrink: 0;
  white-space: nowrap;

  @media (max-width: 768px) {
    width: 100%;
    white-space: normal;
  }
`;

const InputWithButton = styled.div`
  display: flex;
  gap: 6px;
  align-items: center;
  flex: 1;
  width: 100%;

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
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
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:hover {
    background-color: #1a5fd9;
  }
`;

const RadioGroup = styled.div`
  display: flex;
  gap: 20px;
  align-items: center;
  flex: 1;
  flex-wrap: wrap;
`;

const EmailInputContainer = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  flex: 1;
  width: 100%;

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
`;

const EmailSeparator = styled.span`
  font-size: 16px;
  color: #000000;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
`;

// SignupPage의 자격/소개 섹션 스타일
const AddButton = styled.button`
  align-self: flex-end;
  margin-top: 0;
  margin-bottom: 12px;
  border: none;
  background: transparent;
  padding: 0;
  font-size: 13px;
  font-weight: 500;
  color: #000000;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
`;

const TextArea = styled.textarea`
  width: 100%;
  height: 48px;
  min-height: 48px;
  border: 1px solid #ecedec;
  border-radius: 12px;
  padding: 0 12px;
  font-size: 13px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  line-height: 48px;
  resize: none;
  overflow: hidden;
  -ms-overflow-style: none;
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }

  &:focus {
    outline: none;
    border-color: #2d79f3;
  }

  &::placeholder {
    color: #999999;
    font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
      Roboto, sans-serif;
  }
`;

const CharacterCounter = styled.div`
  text-align: right;
  font-size: 12px;
  color: #999999;
  margin-top: 2px;
  margin-bottom: 16px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
`;

const Field = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: stretch;
  }
`;

const Label = styled.div`
  width: 100px;
  flex-shrink: 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  color: #000;

  @media (max-width: 768px) {
    width: 100%;
  }
`;

const Input = styled.input`
  flex: 1;
  height: 40px;
  border: 1px solid #ecedec;
  border-radius: 12px;
  padding: 0 12px;
  font-size: 13px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
`;

// (기존 TextArea 삭제됨 - 위의 TextArea 스타일을 사용)

const SubmitButton = styled.button`
  width: 100%;
  height: 40px;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
  background-color: #2d79f3;
  color: white;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
`;

export const MyPageInfoPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [profileImage, setProfileImage] = useState<File | null>(null);
  const [profileFileName, setProfileFileName] = useState("");
  const [emailDomains] = useState([
    "gmail.com",
    "naver.com",
    "daum.net",
    "kakao.com",
  ]);
  const [formData, setFormData] = useState({
    nickname: '',
    password: '',
    confirmPassword: '',
    gender: 'male',
    birthDate: '',
    emailId: '',
    emailDomain: '',
    affiliation: '',
    introduction: '',
  });
  const [qualificationFields, setQualificationFields] = useState<string[]>([
    "",
  ]);
  const [introductionCount, setIntroductionCount] = useState(0);
  const [showDateTimeModal, setShowDateTimeModal] = useState(false);
  const [selectedDates, setSelectedDates] = useState<Date[]>([]);
  const [selectedTimes, setSelectedTimes] = useState<string[]>([]);
  const [blockedTimesByDate, setBlockedTimesByDate] = useState<
    Map<string, string[]>
  >(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [profileImageUrl, setProfileImageUrl] = useState<string | null>(null);

  // 사용자 정보 조회
  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        setLoading(true);
        setError(null);
        const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);

        if (!token) {
          throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
        }

        const response = await getMyProfileApi(token);

        if (response.isSuccess && response.result) {
          const userData = response.result;

          // 이메일 분리
          let emailId = "";
          let emailDomain = "";
          if (userData.email) {
            const emailParts = userData.email.split("@");
            emailId = emailParts[0] || "";
            emailDomain = emailParts[1] || "";
          }

          // 성별 변환: "여자" -> "female", "남자" -> "male"
          let genderValue = "male";
          if (userData.gender === "여자") {
            genderValue = "female";
          } else if (userData.gender === "남자") {
            genderValue = "male";
          }

          // 생일 형식 변환: "2000-10-10" -> "2000.10.10"
          let birthDateValue = "";
          if (userData.birthdate) {
            birthDateValue = userData.birthdate.replace(/-/g, ".");
          }

          // formData 업데이트
          setFormData((prev) => ({
            ...prev,
            userId: userData.userId || "",
            nickname: userData.nickname || "",
            contact: userData.phoneNumber || "",
            emailId: emailId,
            emailDomain: emailDomain,
            gender: genderValue,
            birthDate: birthDateValue,
            affiliation: userData.organizations || "",
          }));

          // 프로필 이미지 설정
          if (userData.profileImage) {
            setProfileImageUrl(userData.profileImage);
          }
        } else {
          throw new Error(
            response.message || "사용자 정보를 불러오는데 실패했습니다."
          );
        }
      } catch (err) {
        const errorMessage =
          err instanceof Error
            ? err.message
            : "사용자 정보를 불러오는데 실패했습니다.";
        setError(errorMessage);
        console.error("사용자 정보 조회 오류:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchUserInfo();
  }, []);

  const handleInputChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleAddQualification = () => setQualificationFields(prev => [...prev, '']);
  const nickname = user?.nickname;
  const navigateToVerification = () => navigate(ROUTES.COACH_MYPAGE_VERIFICATION);

  // 코치 전용 가드
  useEffect(() => {
    if (!isLoading && user && user.role !== "coach") {
      navigate(ROUTES.COACH_ONLY, { replace: true });
    }
  }, [isLoading, user, navigate]);
  if (loading) {
    return (
      <PageContainer>
        <ContentWrapper>
          <LoadingMessage>사용자 정보를 불러오는 중...</LoadingMessage>
        </ContentWrapper>
      </PageContainer>
    );
  }

  if (error) {
    return (
      <PageContainer>
        <ContentWrapper>
          <ErrorMessage>{error}</ErrorMessage>
        </ContentWrapper>
      </PageContainer>
    );
  }

  return (
    <PageContainer>
      <ContentWrapper>
        <PageTitle>{nickname ? `${nickname} 코치님 마이페이지` : '코치님 마이페이지'}</PageTitle>

      {/* 프로필 사진 업로드 섹션 */}
      <ProfileSection>
        <ProfileImageContainer>
          {profileImage ? (
            <ProfileImage src={URL.createObjectURL(profileImage)} alt="Profile" />
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
            <FileNameInput type="text" value={profileFileName} readOnly />
            <FileButton type="button" onClick={() => document.getElementById('mypage-profile-input')?.click()}>
              파일 등록
            </FileButton>
            <FileButton
              type="button"
              variant="danger"
              onClick={() => { setProfileImage(null); setProfileFileName(''); }}
              disabled={!profileImage}
            >
              파일 삭제
            </FileButton>
          </FileNameContainer>
          <ProfileDescription>
            프로필 사진은 300x400px 사이즈를 권장합니다.<br />
            파일 형식은 JPGE(.jpg, .jpeg) 또는 PNG(.png)만 지원합니다.<br />
            업로드 파일 용량은 2MB 이하만 가능합니다.
          </ProfileDescription>
          <input
            id="mypage-profile-input"
            type="file"
            accept="image/jpeg,image/jpg,image/png"
            style={{ display: 'none' }}
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (!file) return;
              if (file.size > 2 * 1024 * 1024) { alert('파일 용량은 2MB 이하만 가능합니다.'); return; }
              const valid = ['image/jpeg','image/jpg','image/png'];
              if (!valid.includes(file.type)) { alert('파일 형식은 JPG, JPEG 또는 PNG만 지원합니다.'); return; }
              setProfileImage(file);
              setProfileFileName(file.name);
            }}
          />
        </ProfileInfo>
      </ProfileSection>

      {/* SignupPage 형태의 폼 (이메일 인증 관련 요소 제외) */}
      <FormGrid>
        <FormColumn>
          <FormField>
            <FormLabel>닉네임</FormLabel>
            <InputWithButton>
              <CommonInput
                placeholder="닉네임을 입력하세요"
                value={formData.nickname}
                onChange={(e) => handleInputChange('nickname', e.target.value)}
                style={{ flex: 1 }}
              />
              <SmallButton type="button">중복확인</SmallButton>
            </InputWithButton>
          </FormField>

          <FormField>
            <FormLabel>비밀번호</FormLabel>
            <CommonInput
              type="password"
              placeholder="비밀번호를 입력해주세요."
              value={formData.password}
              onChange={(e) => handleInputChange('password', e.target.value)}
              style={{ flex: 1 }}
            />
          </FormField>

          <FormField>
            <FormLabel>비밀번호 확인</FormLabel>
            <CommonInput
              type="password"
              placeholder="비밀번호를 한 번 더 입력해주세요."
              value={formData.confirmPassword}
              onChange={(e) => handleInputChange('confirmPassword', e.target.value)}
              style={{ flex: 1 }}
            />
          </FormField>

          <FormField>
            <FormLabel>성별</FormLabel>
            <RadioGroup>
              <label><input type="radio" name="gender" checked={formData.gender==='male'} onChange={() => handleInputChange('gender','male')} /> 남</label>
              <label><input type="radio" name="gender" checked={formData.gender==='female'} onChange={() => handleInputChange('gender','female')} /> 여</label>
            </RadioGroup>
          </FormField>

          <FormField>
            <FormLabel>생년월일</FormLabel>
            <CommonInput
              placeholder="YYYY.MM.DD"
              value={formData.birthDate}
              onChange={(e) => handleInputChange('birthDate', e.target.value)}
              style={{ flex: 1 }}
            />
          </FormField>
        </FormColumn>

        <FormColumn>
          <FormField style={{ alignItems: 'flex-start' }}>
            <FormLabel style={{ marginTop: '17px' }}>이메일</FormLabel>
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '10px', width: '100%' }}>
              <EmailInputContainer>
                <CommonInput
                  placeholder="이메일을 입력하세요."
                  value={formData.emailId}
                  onChange={(e) => handleInputChange('emailId', e.target.value)}
                  style={{ flex: 1 }}
                />
              </ProfileImagePlaceholder>
            )}
          </ProfileImageContainer>
          <ProfileInfo>
            <ProfileLabel>프로필 사진</ProfileLabel>
            <FileNameContainer>
              <FileNameLabel>파일명</FileNameLabel>
              <FileNameInput
                type="text"
                value={
                  profileFileName || (profileImageUrl ? "프로필 이미지" : "")
                }
                readOnly
              />
              {/* 인증번호 보내기 / 인증하기 섹션 제거 */}
            </div>
          </FormField>

          <FormField>
            <FormLabel>소속</FormLabel>
            <CommonInput
              placeholder="소속을 입력해 주세요."
              value={formData.affiliation}
              onChange={(e) => handleInputChange('affiliation', e.target.value)}
              style={{ flex: 1 }}
            />
          </FormField>

        </FormColumn>
      </FormGrid>

      {/* SignupPage와 동일한 자격/소개 섹션 */}
      <FormField style={{ alignItems: 'flex-start', marginTop: '18px' }}>
        <div style={{ width: '120px', flexShrink: 0 }}>
          <FormLabel>자격</FormLabel>
          <div style={{ fontSize: '10px', color: '#666666', marginTop: '0px', whiteSpace: 'nowrap' }}>
            취득한 자격증을 입력해 주세요.
          </div>
        </div>
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '6px', position: 'relative', width: '100%' }}>
          {qualificationFields.map((value, index) => (
            <CommonInput
              key={index}
              placeholder="자격증 명을 입력해 주세요."
              value={value}
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (!file) return;
                if (file.size > 2 * 1024 * 1024) {
                  alert("파일 용량은 2MB 이하만 가능합니다.");
                  return;
                }
                const valid = ["image/jpeg", "image/jpg", "image/png"];
                if (!valid.includes(file.type)) {
                  alert("파일 형식은 JPG, JPEG 또는 PNG만 지원합니다.");
                  return;
                }
                setProfileImage(file);
                setProfileFileName(file.name);
              }}
              style={{ width: '100%' }}
            />
          </ProfileInfo>
        </ProfileSection>

      <FormField style={{ alignItems: 'flex-start' }}>
        <div style={{ width: '120px', flexShrink: 0, display: 'flex', flexDirection: 'column' }}>
          <FormLabel style={{ marginBottom: '0px' }}>소개</FormLabel>
          <div style={{ fontSize: '10px', color: '#666666', lineHeight: '1.2', marginTop: '0px' }}>
            회원들에게 코치님을<br />소개하는 글을 입력해 주세요.
          </div>
        </div>
        <div style={{ flex: 1, alignSelf: 'flex-start', width: '100%' }}>
          <TextArea
            placeholder="소개 글을 입력해 주세요."
            value={formData.introduction}
            onChange={(e) => { handleInputChange('introduction', (e.target as HTMLTextAreaElement).value); setIntroductionCount((e.target as HTMLTextAreaElement).value.length); }}
            maxLength={50}
          />
          <CharacterCounter>{introductionCount}/50</CharacterCounter>
        </div>
      </FormField>

            <FormField>
              <FormLabel>이름</FormLabel>
              <CommonInput
                placeholder="이름을 입력해주세요."
                value={formData.name}
                onChange={(e) => handleInputChange("name", e.target.value)}
                style={{ flex: 1 }}
              />
            </FormField>

            <FormField>
              <FormLabel>닉네임</FormLabel>
              <InputWithButton>
                <CommonInput
                  placeholder="닉네임을 입력하세요"
                  value={formData.nickname}
                  onChange={(e) =>
                    handleInputChange("nickname", e.target.value)
                  }
                  style={{ flex: 1 }}
                />
                <SmallButton type="button">중복확인</SmallButton>
              </InputWithButton>
            </FormField>

      <div style={{ marginTop: '24px' }}>
        <SubmitButton onClick={navigateToVerification}>정보 수정</SubmitButton>
      </div>
      </ContentWrapper>
    </PageContainer>
  );
};

export default MyPageInfoPage;