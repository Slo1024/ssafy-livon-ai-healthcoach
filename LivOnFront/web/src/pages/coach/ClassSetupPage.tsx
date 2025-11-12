import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import { SegmentedTabs } from "../../components/common/Button";
import {
  DateTimePickerModal,
  SaveConfirmModal,
  ClassCreatedModal,
} from "../../components/common/Modal";
import { Dropdown } from "../../components/common/Dropdown";
import { Input } from "../../components/common/Input";
import { useAuth } from "../../hooks/useAuth";
import { ROUTES } from "../../constants/routes";

const PageContainer = styled.div`
  min-height: 100vh;
  background-color: #ffffff;
  padding: 40px 20px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
`;

const ContentWrapper = styled.div`
  max-width: 1200px;
  margin: 0 auto;
`;

const PageTitle = styled.h1`
  font-weight: 700;
  font-size: 40px;
  color: #000000;
  margin: 0 0 24px 0;
`;

const TabsContainer = styled.div`
  display: flex;
  align-items: center;
  margin-bottom: 0;
`;

const Divider = styled.div`
  width: 100vw;
  height: 2px;
  background-color: #4965f6;
  margin: 0;
  position: relative;
  left: 50%;
  transform: translateX(-50%);
`;

const IntroMessage = styled.p`
  font-size: 20px;
  font-weight: 500;
  color: #374151;
  margin: 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
`;

const FormContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 24px;
`;

const FormField = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

const FormLabel = styled.label`
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
`;

const FormInput = styled.input`
  width: 100%;
  height: 48px;
  border: 1px solid #4965f6;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:focus {
    outline: none;
    border-color: #3b5dd8;
  }
`;

const FormTextArea = styled.textarea`
  width: 100%;
  min-height: 120px;
  border: 2px dashed #4965f6;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  resize: vertical;

  &:focus {
    outline: none;
    border-color: #3b5dd8;
  }
`;

const FormDropdown = styled.select`
  width: 100%;
  height: 48px;
  border: 1px solid #4965f6;
  border-radius: 8px;
  padding: 0 12px;
  padding-right: 36px;
  font-size: 14px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  background-color: white;
  background-image: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='10' height='6' viewBox='0 0 10 6'><path fill='%234965f6' d='M1 0l4 4 4-4 1 1-5 5-5-5z'/></svg>");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 10px 6px;
  cursor: pointer;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;

  &:focus {
    outline: none;
    border-color: #3b5dd8;
  }
`;

const ButtonContainer = styled.div`
  display: flex;
  gap: 12px;
  margin-top: 32px;
  justify-content: center;
`;

const SaveButton = styled.button`
  padding: 12px 48px;
  background-color: #4965f6;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:hover {
    background-color: #3b5dd8;
  }
`;

const CancelButton = styled.button`
  padding: 12px 48px;
  background-color: #ffffff;
  color: #374151;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:hover {
    background-color: #f9fafb;
  }
`;

export const ClassSetupPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, isLoading } = useAuth();

  // 코치 전용 가드
  useEffect(() => {
    if (!isLoading && user && user.role !== "coach") {
      navigate(ROUTES.COACH_ONLY, { replace: true });
    }
  }, [isLoading, user, navigate]);
  const [showDateTimeModal, setShowDateTimeModal] = useState(false);
  const [showSaveConfirmModal, setShowSaveConfirmModal] = useState(false);
  const [showClassCreatedModal, setShowClassCreatedModal] = useState(false);
  const [selectedDates, setSelectedDates] = useState<Date[]>([]);
  const [selectedTimes, setSelectedTimes] = useState<string[]>([]);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [formData, setFormData] = useState({
    classType: "",
    className: "",
    classInfo: "",
    dateTime: "",
    file: "",
  });

  const nickname = user?.nickname || "";
  const pageTitle = nickname
    ? `${nickname} 코치님의 클래스`
    : "코치님의 클래스";

  const handleListClick = () => {
    navigate(ROUTES.CLASS_LIST);
  };

  const handleSetupClick = () => {
    // 이미 클래스 개설 페이지에 있음
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleDateTimeClick = () => {
    setShowDateTimeModal(true);
  };

  const handleDateTimeSelect = (dates: Date[], times: string[]) => {
    setSelectedDates(dates);
    setSelectedTimes(times);

    // 날짜와 시간을 문자열로 포맷팅
    if (dates.length > 0 && times.length > 0) {
      const dateStr = dates
        .map(
          (date) =>
            `${date.getFullYear()}년 ${
              date.getMonth() + 1
            }월 ${date.getDate()}일`
        )
        .join(", ");
      const timeStr = times
        .map((t) => {
          if (t.startsWith("AM ")) {
            return `오전 ${t.replace("AM ", "")}`;
          } else if (t.startsWith("PM ")) {
            return `오후 ${t.replace("PM ", "")}`;
          }
          return t;
        })
        .join(", ");
      handleInputChange("dateTime", `${dateStr} ${timeStr}`);
    } else if (dates.length > 0) {
      const dateStr = dates
        .map(
          (date) =>
            `${date.getFullYear()}년 ${
              date.getMonth() + 1
            }월 ${date.getDate()}일`
        )
        .join(", ");
      handleInputChange("dateTime", dateStr);
    }
  };

  const handleFileClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      handleInputChange("file", file.name);
    }
  };

  const handleSave = () => {
    setShowSaveConfirmModal(true);
  };

  const handleSaveConfirm = () => {
    // 저장 로직 (추후 API 연동)
    console.log("Form Data:", formData);
    setShowSaveConfirmModal(false);
    setShowClassCreatedModal(true);
  };

  const handleGoToList = () => {
    navigate(ROUTES.CLASS_LIST);
  };

  const handleCancel = () => {
    navigate(ROUTES.CLASS_LIST);
  };

  const classTypeOptions = [
    { value: "기업 클래스", label: "기업 클래스" },
    { value: "일반 클래스", label: "일반 클래스" },
    { value: "개인 상담 / 코칭", label: "개인 상담 / 코칭" },
  ];

  return (
    <PageContainer>
      <ContentWrapper>
        <PageTitle>{pageTitle}</PageTitle>

        <TabsContainer>
          <SegmentedTabs
            leftLabel="클래스 목록"
            rightLabel="클래스 개설"
            active="right"
            onLeftClick={handleListClick}
            onRightClick={handleSetupClick}
            tabWidth={120}
            showDivider={false}
          />
        </TabsContainer>

        <Divider />

        <IntroMessage>
          회원들을 위한 새로운 클래스를 개설할 수 있습니다.
        </IntroMessage>

        <FormContainer>
          <FormField>
            <FormLabel>클래스 형태 선택</FormLabel>
            <FormDropdown
              value={formData.classType}
              onChange={(e) => handleInputChange("classType", e.target.value)}
            >
              <option value="" disabled>
                클래스 형태를 선택해 주세요.
              </option>
              {classTypeOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </FormDropdown>
          </FormField>

          <FormField>
            <FormLabel>클래스 명</FormLabel>
            <FormInput
              type="text"
              value={formData.className}
              onChange={(e) => handleInputChange("className", e.target.value)}
              placeholder="클래스 명을 입력해 주세요."
            />
          </FormField>

          <FormField>
            <FormLabel>클래스 정보</FormLabel>
            <FormTextArea
              value={formData.classInfo}
              onChange={(e) => handleInputChange("classInfo", e.target.value)}
              placeholder="클래스에 대한 설명을 입력해 주세요."
            />
          </FormField>

          <FormField>
            <FormLabel>날짜 / 시간 선택</FormLabel>
            <FormInput
              type="text"
              value={formData.dateTime}
              onChange={(e) => handleInputChange("dateTime", e.target.value)}
              placeholder="클릭하여 선택"
              readOnly
              onClick={handleDateTimeClick}
              style={{ cursor: "pointer" }}
            />
          </FormField>

          <FormField>
            <FormLabel>파일 첨부</FormLabel>
            <input
              ref={fileInputRef}
              type="file"
              style={{ display: "none" }}
              onChange={handleFileChange}
              accept="*/*"
            />
            <FormDropdown
              value={formData.file || ""}
              onChange={() => {}}
              onMouseDown={(e) => {
                e.preventDefault();
                fileInputRef.current?.click();
              }}
              onFocus={(e) => e.target.blur()}
              style={{ cursor: "pointer" }}
              tabIndex={0}
            >
              <option value="">파일 찾기</option>
              {selectedFile && (
                <option value={selectedFile.name}>{selectedFile.name}</option>
              )}
            </FormDropdown>
          </FormField>
        </FormContainer>

        <ButtonContainer>
          <SaveButton onClick={handleSave}>저장</SaveButton>
          <CancelButton onClick={handleCancel}>취소</CancelButton>
        </ButtonContainer>

        {/* 날짜/시간 선택 모달 */}
        <DateTimePickerModal
          open={showDateTimeModal}
          onClose={() => setShowDateTimeModal(false)}
          onSelect={handleDateTimeSelect}
          initialDates={selectedDates}
          initialTimes={selectedTimes}
        />

        {/* 저장 확인 모달 */}
        <SaveConfirmModal
          open={showSaveConfirmModal}
          onClose={() => setShowSaveConfirmModal(false)}
          onConfirm={handleSaveConfirm}
        />

        {/* 클래스 개설 완료 모달 */}
        <ClassCreatedModal
          open={showClassCreatedModal}
          onClose={() => setShowClassCreatedModal(false)}
          onGoToList={handleGoToList}
        />
      </ContentWrapper>
    </PageContainer>
  );
};

export default ClassSetupPage;
