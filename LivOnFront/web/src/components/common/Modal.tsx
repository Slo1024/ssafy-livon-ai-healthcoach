import React, { useState, useEffect } from "react";
import styled from "styled-components";
import profilePictureIcon from "../../assets/images/profile_picture.png";
import { SegmentedTabs } from "../common/Button";
import {
  getMemberInfoApi,
  getParticipantInfoApi,
} from "../../api/reservationApi";
import { CONFIG } from "../../constants/config";
import { BaseModalProps, Overlay } from "./ModalStyles";
import { DateTimePickerModal } from "./DateTimePickerModal";

// 분리된 모달들을 re-export
export { ConfirmModal } from "./ConfirmModal";
export type { ConfirmModalProps } from "./ConfirmModal";
export { InfoUpdateSuccessModal } from "./InfoUpdateSuccessModal";
export type { InfoUpdateSuccessModalProps } from "./InfoUpdateSuccessModal";
export { DateTimePickerModal } from "./DateTimePickerModal";
export type { DateTimePickerModalProps } from "./DateTimePickerModal";

// =====================
// 클래스 정보 수정 모달
// =====================

const EditModalCard = styled.div`
  width: 680px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  background: #ffffff;
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
`;

const EditModalTitle = styled.h2`
  margin: 0 0 32px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
  text-align: left;
`;

const FormField = styled.div`
  margin-bottom: 24px;
`;

const FormLabel = styled.label`
  display: block;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
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

const ButtonRow = styled.div`
  display: flex;
  gap: 12px;
  margin-top: 32px;
  justify-content: flex-end;
`;

const SaveButton = styled.button`
  padding: 12px 24px;
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

const CloseButton = styled.button`
  padding: 12px 24px;
  background-color: #ffffff;
  color: #4965f6;
  border: 1px solid #4965f6;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:hover {
    background-color: #f7fafc;
  }
`;

export interface ClassEditModalProps extends BaseModalProps {
  classNameData?: {
    name: string;
    description: string;
    targetMember: string;
    dateTime: string;
    file?: string;
  };
  onSave: (data: {
    name: string;
    description: string;
    targetMember: string;
    dateTime: string;
    file?: string;
  }) => void;
}

export const ClassEditModal: React.FC<ClassEditModalProps> = ({
  open,
  onClose,
  classNameData,
  onSave,
  className,
  style,
}) => {
  const [formData, setFormData] = useState({
    name: classNameData?.name || "",
    description: classNameData?.description || "",
    targetMember: classNameData?.targetMember || "",
    dateTime: classNameData?.dateTime || "",
    file: classNameData?.file || "",
  });
  const [showDateTimeModal, setShowDateTimeModal] = useState(false);
  const [selectedDates, setSelectedDates] = useState<Date[]>([]);
  const [selectedTimes, setSelectedTimes] = useState<string[]>([]);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (classNameData) {
      setFormData({
        name: classNameData.name || "",
        description: classNameData.description || "",
        targetMember: classNameData.targetMember || "",
        dateTime: classNameData.dateTime || "",
        file: classNameData.file || "",
      });
    }
  }, [classNameData]);

  useEffect(() => {
    if (!open) {
      // 모달이 닫힐 때 상태 초기화
      setSelectedDates([]);
      setSelectedTimes([]);
      setSelectedFile(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  }, [open]);

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

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      handleInputChange("file", file.name);
    }
  };

  const handleSave = () => {
    onSave(formData);
    onClose();
  };

  if (!open) return null;

  return (
    <Overlay onClick={onClose}>
      <EditModalCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <EditModalTitle>클래스 정보 및 수정</EditModalTitle>

        <FormField>
          <FormLabel>클래스 명</FormLabel>
          <FormInput
            type="text"
            value={formData.name}
            onChange={(e) => handleInputChange("name", e.target.value)}
            placeholder="클래스 명을 입력하세요"
          />
        </FormField>

        <FormField>
          <FormLabel>클래스 정보</FormLabel>
          <FormTextArea
            value={formData.description}
            onChange={(e) => handleInputChange("description", e.target.value)}
            placeholder="클래스 정보를 입력하세요"
          />
        </FormField>

        <FormField>
          <FormLabel>코칭 대상 회원 선택</FormLabel>
          <FormDropdown
            value={formData.targetMember}
            onChange={(e) => handleInputChange("targetMember", e.target.value)}
          >
            <option value="" disabled>
              클래스가 일반 개인 회원 대상인지, 기업 소속 회원 대상인지 선택해
              주세요.
            </option>
            <option value="기업 클래스">기업 클래스</option>
            <option value="일반 클래스">일반 클래스</option>
            <option value="개인 상담 / 코칭">개인 상담 / 코칭</option>
          </FormDropdown>
        </FormField>

        <FormField>
          <FormLabel>날짜 / 시간 선택</FormLabel>
          <FormInput
            type="text"
            value={formData.dateTime}
            onChange={(e) => handleInputChange("dateTime", e.target.value)}
            placeholder="날짜 / 시간을 선택하세요"
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

        <ButtonRow>
          <SaveButton onClick={handleSave}>저장</SaveButton>
          <CloseButton onClick={onClose}>닫기</CloseButton>
        </ButtonRow>
      </EditModalCard>

      {/* 날짜/시간 선택 모달 */}
      <DateTimePickerModal
        open={showDateTimeModal}
        onClose={() => setShowDateTimeModal(false)}
        onSelect={handleDateTimeSelect}
        initialDates={selectedDates}
        initialTimes={selectedTimes}
      />
    </Overlay>
  );
};

// =====================
// 삭제 확인 모달 (삭제/취소 버튼)
// =====================

const DeleteConfirmCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

const DeleteConfirmTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
`;

const DeleteConfirmButtonRow = styled.div`
  display: flex;
  gap: 12px;
  justify-content: center;
`;

const DeleteButton = styled.button`
  padding: 12px 24px;
  background-color: #ffffff;
  color: #ff0000;
  border: 1px solid #ff0000;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:hover {
    background-color: #fef2f2;
  }
`;

const CancelButton = styled.button`
  padding: 12px 24px;
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

export interface DeleteConfirmModalProps extends BaseModalProps {
  onConfirm: () => void;
}

export const DeleteConfirmModal: React.FC<DeleteConfirmModalProps> = ({
  open,
  onClose,
  onConfirm,
  className,
  style,
}) => {
  const handleConfirm = () => {
    onConfirm();
    onClose();
  };

  if (!open) return null;

  return (
    <Overlay onClick={onClose}>
      <DeleteConfirmCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <DeleteConfirmTitle>클래스를 삭제하시겠습니까?</DeleteConfirmTitle>
        <DeleteConfirmButtonRow>
          <DeleteButton onClick={handleConfirm}>삭제</DeleteButton>
          <CancelButton onClick={onClose}>취소</CancelButton>
        </DeleteConfirmButtonRow>
      </DeleteConfirmCard>
    </Overlay>
  );
};

// =====================
// 저장 확인 모달 (저장/취소 버튼)
// =====================

const SaveConfirmCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

const SaveConfirmTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
`;

const SaveConfirmButtonRow = styled.div`
  display: flex;
  gap: 12px;
  justify-content: center;
`;

const SaveConfirmButton = styled.button`
  padding: 12px 24px;
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

const SaveCancelButton = styled.button`
  padding: 12px 24px;
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

export interface SaveConfirmModalProps extends BaseModalProps {
  onConfirm: () => void;
}

export const SaveConfirmModal: React.FC<SaveConfirmModalProps> = ({
  open,
  onClose,
  onConfirm,
  className,
  style,
}) => {
  const handleConfirm = () => {
    onConfirm();
    onClose();
  };

  if (!open) return null;

  return (
    <Overlay onClick={onClose}>
      <SaveConfirmCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <SaveConfirmTitle>신규 클래스를 저장하시겠습니까?</SaveConfirmTitle>
        <SaveConfirmButtonRow>
          <SaveConfirmButton onClick={handleConfirm}>저장</SaveConfirmButton>
          <SaveCancelButton onClick={onClose}>취소</SaveCancelButton>
        </SaveConfirmButtonRow>
      </SaveConfirmCard>
    </Overlay>
  );
};

// =====================
// 클래스 개설 완료 모달
// =====================

const ClassCreatedCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

const ClassCreatedTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
`;

const ClassListButton = styled.button`
  width: 100%;
  padding: 12px 24px;
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

export interface ClassCreatedModalProps extends BaseModalProps {
  onGoToList: () => void;
}

export const ClassCreatedModal: React.FC<ClassCreatedModalProps> = ({
  open,
  onClose,
  onGoToList,
  className,
  style,
}) => {
  const handleGoToList = () => {
    onGoToList();
    onClose();
  };

  if (!open) return null;

  return (
    <Overlay onClick={onClose}>
      <ClassCreatedCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <ClassCreatedTitle>신규 클래스를 개설되었습니다.</ClassCreatedTitle>
        <ClassListButton onClick={handleGoToList}>
          클래스 목록으로
        </ClassListButton>
      </ClassCreatedCard>
    </Overlay>
  );
};

// =====================
// 회원 정보 모달
// =====================

const MemberInfoCard = styled.div`
  width: 600px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
`;

const MemberInfoTitle = styled.h2`
  margin: 0 0 8px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
`;

const MemberInfoDescription = styled.p`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
`;

const MemberContentContainer = styled.div`
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
`;

const ProfileIconContainer = styled.div`
  width: 120px;
  height: 160px;
  background-color: #f3f4f6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
`;

const ProfileImage = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;

const MemberDataContainer = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

const MemberDataItem = styled.div`
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 16px;
  color: #111827;
`;

const QASection = styled.div`
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
`;

const QATitle = styled.h3`
  margin: 0 0 12px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 16px;
  color: #111827;
`;

const QAQuestion = styled.div`
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
`;

const MemberInfoConfirmButton = styled.button`
  width: 100%;
  padding: 12px 24px;
  background-color: #4965f6;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  margin-top: 24px;

  &:hover {
    background-color: #3b5dd8;
  }
`;

const LoadingText = styled.div`
  text-align: center;
  padding: 20px;
  color: #6b7280;
  font-size: 14px;
`;

const ErrorText = styled.div`
  text-align: center;
  padding: 20px;
  color: #ef4444;
  font-size: 14px;
`;

export interface MemberInfoModalProps extends BaseModalProps {
  memberName?: string;
  memberId?: string; // userId를 받을 수 있도록 추가
  consultationId?: number; // 1:1 상담 ID (있으면 getParticipantInfoApi 사용)
  memberData?: {
    height?: number;
    weight?: number;
    sleepTime?: number;
  };
  question?: string;
}

export const MemberInfoModal: React.FC<MemberInfoModalProps> = ({
  open,
  onClose,
  memberName,
  memberId,
  consultationId,
  memberData,
  question,
  className,
  style,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [memberInfo, setMemberInfo] = useState<{
    nickname: string;
    profileImage?: string;
    height?: number;
    weight?: number;
    sleepTime?: number;
    preQna?: string;
  } | null>(null);

  // 회원 정보 가져오기
  useEffect(() => {
    const fetchMemberInfo = async () => {
      if (!open) {
        return;
      }

      // consultationId가 있으면 getParticipantInfoApi 사용 (1:1 상담)
      if (consultationId) {
        try {
          setLoading(true);
          setError(null);
          const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);

          if (!token || token.trim() === "") {
            throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
          }

          console.log("참여자 정보 조회 시작:", {
            consultationId,
            hasToken: !!token,
            tokenLength: token.length,
          });

          const response = await getParticipantInfoApi(token, consultationId);
          const participantInfo = response.memberInfo;
          const healthData = participantInfo?.healthData;

          setMemberInfo({
            nickname: participantInfo.nickname || memberName || "회원",
            height: healthData?.height,
            weight: healthData?.weight,
            sleepTime: healthData?.sleepTime,
            preQna: question || undefined,
          });
        } catch (err) {
          console.error("참여자 정보 조회 오류:", {
            error: err,
            consultationId,
            hasToken: !!localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY),
            errorMessage: err instanceof Error ? err.message : String(err),
          });

          const errorMessage =
            err instanceof Error
              ? err.message
              : "참여자 정보를 불러오는데 실패했습니다.";
          setError(errorMessage);

          // 에러 발생 시 기본값 사용
          setMemberInfo({
            nickname: memberName || "회원",
            height: memberData?.height,
            weight: memberData?.weight,
            sleepTime: memberData?.sleepTime,
            preQna: question,
          });
        } finally {
          setLoading(false);
        }
        return;
      }

      // memberId가 있으면 getMemberInfoApi 사용
      if (memberId) {
        try {
          setLoading(true);
          setError(null);
          const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);

          if (!token) {
            throw new Error("인증 토큰이 없습니다.");
          }

          const info = await getMemberInfoApi(token, memberId);
          setMemberInfo({
            nickname: info.nickname || memberName || "회원",
            profileImage: info.profileImage,
            height: info.height,
            weight: info.weight,
            sleepTime: info.sleepTime,
            preQna: info.preQna || question,
          });
        } catch (err) {
          const errorMessage =
            err instanceof Error
              ? err.message
              : "회원 정보를 불러오는데 실패했습니다.";
          setError(errorMessage);
          console.error("회원 정보 조회 오류:", err);
          // 에러 발생 시 기본값 사용
          setMemberInfo({
            nickname: memberName || "회원",
            height: memberData?.height,
            weight: memberData?.weight,
            sleepTime: memberData?.sleepTime,
            preQna: question,
          });
        } finally {
          setLoading(false);
        }
        return;
      }

      // memberId와 consultationId가 모두 없으면 기존 방식 사용 (memberData 사용)
      if (memberName) {
        setMemberInfo({
          nickname: memberName,
          height: memberData?.height,
          weight: memberData?.weight,
          sleepTime: memberData?.sleepTime,
          preQna: question,
        });
      }
    };

    fetchMemberInfo();
  }, [open, memberId, consultationId, memberName, memberData, question]);

  // 모달이 닫힐 때 상태 초기화
  useEffect(() => {
    if (!open) {
      setMemberInfo(null);
      setError(null);
      setLoading(false);
    }
  }, [open]);

  if (!open) return null;

  const displayName = memberInfo?.nickname || memberName || "회원";
  const height = memberInfo?.height ?? memberData?.height;
  const weight = memberInfo?.weight ?? memberData?.weight;
  const sleepTime = memberInfo?.sleepTime ?? memberData?.sleepTime;
  const qaQuestion = memberInfo?.preQna || question;
  const profileImage = memberInfo?.profileImage || profilePictureIcon;
  
  // 건강 데이터 존재 여부 확인
  const hasHealthData = height !== undefined || weight !== undefined || sleepTime !== undefined;
  const hasQnA = qaQuestion && qaQuestion.trim() !== "";

  return (
    <Overlay onClick={onClose}>
      <MemberInfoCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <MemberInfoTitle>{displayName} 회원님 정보</MemberInfoTitle>
        <MemberInfoDescription>
          회원님의 신체 데이터를 AI로 분석한 결과를 확인할 수 있습니다.
        </MemberInfoDescription>

        {loading ? (
          <LoadingText>회원 정보를 불러오는 중...</LoadingText>
        ) : error ? (
          <ErrorText>{error}</ErrorText>
        ) : (
          <>
            <MemberContentContainer>
              <ProfileIconContainer>
                <ProfileImage src={profileImage} alt="회원 프로필" />
              </ProfileIconContainer>

              <MemberDataContainer>
                {hasHealthData ? (
                  <>
                    {height !== undefined && (
                      <MemberDataItem>신장 {height}cm</MemberDataItem>
                    )}
                    {weight !== undefined && (
                      <MemberDataItem>체중 {weight}kg</MemberDataItem>
                    )}
                    {sleepTime !== undefined && (
                      <MemberDataItem>수면 시간 {sleepTime}시간</MemberDataItem>
                    )}
                  </>
                ) : (
                  <MemberDataItem style={{ color: "#6b7280", fontStyle: "italic" }}>
                    건강 데이터가 없습니다.
                  </MemberDataItem>
                )}
              </MemberDataContainer>
            </MemberContentContainer>

            <QASection>
              <QATitle>Q&A</QATitle>
              {hasQnA ? (
                <QAQuestion>Q. {qaQuestion}</QAQuestion>
              ) : (
                <QAQuestion style={{ color: "#6b7280", fontStyle: "italic" }}>
                  사전 질문이 없습니다.
                </QAQuestion>
              )}
            </QASection>
          </>
        )}

        <MemberInfoConfirmButton onClick={onClose}>
          확인
        </MemberInfoConfirmButton>
      </MemberInfoCard>
    </Overlay>
  );
};

// =====================
// 신청 회원 예약 승인 모달
// =====================

const ApplicationApprovalCard = styled.div`
  width: 680px;
  max-width: 90vw;
  max-height: 80vh;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
`;

const ApplicationApprovalTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
`;

const ApplicationTabsContainer = styled.div`
  margin-bottom: 24px;
`;

const ApplicationMemberList = styled.div`
  flex: 1;
  overflow-y: auto;
  margin-bottom: 24px;
  max-height: 400px;
`;

const ApplicationMemberItem = styled.div`
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e5e7eb;

  &:last-child {
    border-bottom: none;
  }
`;

const ApplicationProfileImage = styled.img`
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  margin-right: 16px;
  background-color: #f3f4f6;
`;

const ApplicationMemberName = styled.div`
  flex: 1;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 16px;
  font-weight: 500;
  color: #111827;
`;

const ApplicationButtonContainer = styled.div`
  display: flex;
  gap: 8px;
`;

const ApplicationMemberInfoButton = styled.button`
  padding: 8px 16px;
  background-color: #ffffff;
  color: #4965f6;
  border: 1px solid #4965f6;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  white-space: nowrap;

  &:hover {
    background-color: #f7fafc;
  }
`;

const ApplicationApproveButton = styled.button`
  padding: 8px 16px;
  background-color: #ffffff;
  color: #4965f6;
  border: 1px solid #4965f6;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  white-space: nowrap;

  &:hover {
    background-color: #f7fafc;
  }
`;

const ApplicationCancelApprovalButton = styled.button`
  padding: 8px 16px;
  background-color: #ffffff;
  color: #4965f6;
  border: 1px solid #4965f6;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  white-space: nowrap;

  &:hover {
    background-color: #f7fafc;
  }
`;

const ApplicationConfirmButton = styled.button`
  width: 100%;
  padding: 12px 24px;
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

export interface ApplicationMember {
  id: string;
  name: string;
}

export interface ApplicationApprovalModalProps extends BaseModalProps {
  members?: ApplicationMember[];
  onMemberInfoClick?: (memberName: string) => void;
}

// =====================
// 예약 취소 확인 모달
// =====================

const ReservationCancelConfirmCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

const ReservationCancelConfirmTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
  line-height: 1.3;
  white-space: pre-line;
`;

const ReservationCancelConfirmButtonContainer = styled.div`
  display: flex;
  gap: 8px;
  margin-top: 0;
`;

const ReservationCancelConfirmButton = styled.button`
  flex: 1;
  height: 48px;
  background-color: #4965f6;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:hover {
    background-color: #3b5dd8;
  }
`;

export interface ReservationCancelConfirmModalProps extends BaseModalProps {
  onConfirm: () => void;
  onCancel?: () => void;
}

export const ReservationCancelConfirmModal: React.FC<
  ReservationCancelConfirmModalProps
> = ({ open, onClose, onConfirm, onCancel, className, style }) => {
  if (!open) return null;

  const handleConfirm = () => {
    onConfirm();
    onClose();
  };

  const handleCancel = () => {
    if (onCancel) {
      onCancel();
    }
    onClose();
  };

  return (
    <Overlay onClick={handleCancel}>
      <ReservationCancelConfirmCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <ReservationCancelConfirmTitle>
          예약을 취소하시겠습니까?
        </ReservationCancelConfirmTitle>
        <ReservationCancelConfirmButtonContainer>
          <ReservationCancelConfirmButton onClick={handleConfirm}>
            네
          </ReservationCancelConfirmButton>
          <ReservationCancelConfirmButton onClick={handleCancel}>
            아니오
          </ReservationCancelConfirmButton>
        </ReservationCancelConfirmButtonContainer>
      </ReservationCancelConfirmCard>
    </Overlay>
  );
};

// =====================
// 예약 취소 완료 모달
// =====================

const ReservationCancelSuccessCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

const ReservationCancelSuccessTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
  line-height: 1.3;
  white-space: pre-line;
`;

const ReservationCancelSuccessButton = styled.button`
  margin-top: 0;
  width: 100%;
  height: 48px;
  background-color: #4965f6;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:hover {
    background-color: #3b5dd8;
  }
`;

export interface ReservationCancelSuccessModalProps extends BaseModalProps {
  onConfirm?: () => void;
}

export const ReservationCancelSuccessModal: React.FC<
  ReservationCancelSuccessModalProps
> = ({ open, onClose, onConfirm, className, style }) => {
  if (!open) return null;

  const handleConfirm = () => {
    if (onConfirm) {
      onConfirm();
    }
    onClose();
  };

  return (
    <Overlay onClick={onClose}>
      <ReservationCancelSuccessCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <ReservationCancelSuccessTitle>
          예약이 취소되었습니다.
        </ReservationCancelSuccessTitle>
        <ReservationCancelSuccessButton onClick={handleConfirm}>
          확인
        </ReservationCancelSuccessButton>
      </ReservationCancelSuccessCard>
    </Overlay>
  );
};

// =====================
// 상담 요약본 모달
// =====================

const ConsultationSummaryCard = styled.div`
  width: 600px;
  max-width: 90vw;
  max-height: 80vh;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
`;

const ConsultationSummaryTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
`;

const ConsultationSummaryDate = styled.div`
  margin-bottom: 8px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  color: #6b7280;
`;

const ConsultationSummaryTime = styled.div`
  margin-bottom: 24px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  color: #6b7280;
`;

const ConsultationSummaryContent = styled.div`
  flex: 1;
  overflow-y: auto;
  margin-bottom: 24px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  color: #374151;
  line-height: 1.8;

  p {
    margin: 0 0 16px 0;

    &:last-child {
      margin-bottom: 0;
    }
  }
`;

const ConsultationSummaryConfirmButton = styled.button`
  width: 100%;
  padding: 12px 24px;
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

export interface ConsultationSummaryModalProps extends BaseModalProps {
  memberName: string;
  reservationId: number;
  date: string;
  startTime: string;
  endTime: string;
}

export const ConsultationSummaryModal: React.FC<
  ConsultationSummaryModalProps
> = ({
  open,
  onClose,
  memberName,
  reservationId,
  date,
  startTime,
  endTime,
  className,
  style,
}) => {
  if (!open) return null;

  return (
    <Overlay onClick={onClose}>
      <ConsultationSummaryCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <ConsultationSummaryTitle>
          {memberName} 회원님 상담 요약본
        </ConsultationSummaryTitle>
        <ConsultationSummaryDate>{date}</ConsultationSummaryDate>
        <ConsultationSummaryTime>
          {startTime} 시작 {endTime} 종료
        </ConsultationSummaryTime>
        <ConsultationSummaryContent>
          <p>
            {memberName} 코치님은 이번 상담에서 {memberName} 회원님에게 ~~~이
            ~~~하므로 ~~~~을 ~~~~해서 ~~~할 것을 권장하셨고,
          </p>
          <p>
            {memberName} 회원님은 ~~~ 동안 ~~~해서 ~~~할 것이라고
            얘기하였습니다.
          </p>
        </ConsultationSummaryContent>
        <ConsultationSummaryConfirmButton onClick={onClose}>
          확인
        </ConsultationSummaryConfirmButton>
      </ConsultationSummaryCard>
    </Overlay>
  );
};

// =====================
// 신청 회원 정보 모달 (승인 기능 없음)
// =====================

const ApplicationMemberInfoCard = styled.div`
  width: 680px;
  max-width: 90vw;
  max-height: 80vh;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
`;

const ApplicationMemberInfoTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
`;

const ApplicationMemberInfoList = styled.div`
  flex: 1;
  overflow-y: auto;
  margin-bottom: 24px;
  max-height: 400px;
`;

const ApplicationMemberInfoItem = styled.div`
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e5e7eb;

  &:last-child {
    border-bottom: none;
  }
`;

const ApplicationMemberInfoProfileImage = styled.img`
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  margin-right: 16px;
  background-color: #f3f4f6;
`;

const ApplicationMemberInfoName = styled.div`
  flex: 1;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 16px;
  font-weight: 500;
  color: #111827;
`;

const ApplicationMemberInfoButtonContainer = styled.div`
  display: flex;
  gap: 8px;
`;

const ApplicationMemberInfoMemberInfoButton = styled.button`
  padding: 8px 16px;
  background-color: #ffffff;
  color: #4965f6;
  border: 1px solid #4965f6;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  white-space: nowrap;

  &:hover {
    background-color: #f7fafc;
  }
`;

const ApplicationMemberInfoDeleteButton = styled.button`
  padding: 8px 16px;
  background-color: #ffffff;
  color: #4965f6;
  border: 1px solid #4965f6;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  white-space: nowrap;

  &:hover {
    background-color: #f7fafc;
  }
`;

const ApplicationMemberInfoConfirmButton = styled.button`
  width: 100%;
  padding: 12px 24px;
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

export interface ApplicationMemberInfoModalProps extends BaseModalProps {
  members?: ApplicationMember[];
}

export const ApplicationMemberInfoModal: React.FC<
  ApplicationMemberInfoModalProps
> = ({ open, onClose, members = [], className, style }) => {
  const [memberList, setMemberList] = useState<ApplicationMember[]>(members);
  const [showMemberInfoModal, setShowMemberInfoModal] = useState(false);
  const [selectedMemberName, setSelectedMemberName] = useState<string>("");
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null);

  useEffect(() => {
    if (open) {
      setMemberList(members);
    }
  }, [open, members]);

  const handleMemberInfoClick = (memberName: string, memberId?: string) => {
    setSelectedMemberName(memberName);
    setSelectedMemberId(memberId || null);
    setShowMemberInfoModal(true);
  };

  const handleDeleteMember = (memberId: string) => {
    setMemberList((prev) => prev.filter((m) => m.id !== memberId));
  };

  const handleConfirm = () => {
    onClose();
  };

  if (!open) return null;

  return (
    <>
      <Overlay onClick={onClose}>
        <ApplicationMemberInfoCard
          className={className}
          style={style}
          onClick={(e) => e.stopPropagation()}
        >
          <ApplicationMemberInfoTitle>
            신청 회원 정보
          </ApplicationMemberInfoTitle>

          <ApplicationMemberInfoList>
            {memberList.length === 0 ? (
              <div
                style={{
                  textAlign: "center",
                  padding: "40px 0",
                  color: "#6b7280",
                  fontSize: "14px",
                }}
              >
                신청 회원이 없습니다.
              </div>
            ) : (
              memberList.map((member) => (
                <ApplicationMemberInfoItem key={member.id}>
                  <ApplicationMemberInfoProfileImage
                    src={profilePictureIcon}
                    alt={member.name}
                  />
                  <ApplicationMemberInfoName>
                    {member.name} 회원님
                  </ApplicationMemberInfoName>
                  <ApplicationMemberInfoButtonContainer>
                    <ApplicationMemberInfoMemberInfoButton
                      onClick={() =>
                        handleMemberInfoClick(member.name, member.id)
                      }
                    >
                      회원 정보
                    </ApplicationMemberInfoMemberInfoButton>
                    <ApplicationMemberInfoDeleteButton
                      onClick={() => handleDeleteMember(member.id)}
                    >
                      회원 삭제
                    </ApplicationMemberInfoDeleteButton>
                  </ApplicationMemberInfoButtonContainer>
                </ApplicationMemberInfoItem>
              ))
            )}
          </ApplicationMemberInfoList>

          <ApplicationMemberInfoConfirmButton onClick={handleConfirm}>
            확인
          </ApplicationMemberInfoConfirmButton>
        </ApplicationMemberInfoCard>
      </Overlay>

      {/* 회원 정보 모달 */}
      <MemberInfoModal
        open={showMemberInfoModal}
        onClose={() => {
          setShowMemberInfoModal(false);
          setSelectedMemberId(null);
        }}
        memberName={selectedMemberName}
        memberId={selectedMemberId || undefined}
      />
    </>
  );
};

// =====================
// FAQ 답변 모달
// =====================

const FAQAnswerCard = styled.div`
  width: 600px;
  max-width: 90vw;
  max-height: 80vh;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
`;

const FAQAnswerTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
  display: flex;
  align-items: flex-start;
  gap: 12px;
`;

const FAQAnswerQuestion = styled.span`
  font-weight: 700;
  color: #111827;
`;

const FAQAnswerContent = styled.div`
  flex: 1;
  overflow-y: auto;
  margin-bottom: 24px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  color: #374151;
  line-height: 1.8;
  white-space: pre-line;
`;

const FAQAnswerConfirmButton = styled.button`
  width: 100%;
  padding: 12px 24px;
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

export interface FAQAnswerModalProps extends BaseModalProps {
  question: string;
  answer: string;
}

export const FAQAnswerModal: React.FC<FAQAnswerModalProps> = ({
  open,
  onClose,
  question,
  answer,
  className,
  style,
}) => {
  if (!open) return null;

  return (
    <Overlay onClick={onClose}>
      <FAQAnswerCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <FAQAnswerTitle>
          <FAQAnswerQuestion>Q.</FAQAnswerQuestion>
          <span>{question}</span>
        </FAQAnswerTitle>
        <FAQAnswerContent>{answer}</FAQAnswerContent>
        <FAQAnswerConfirmButton onClick={onClose}>확인</FAQAnswerConfirmButton>
      </FAQAnswerCard>
    </Overlay>
  );
};

// =====================
// 문의 사항 전달 완료 모달
// =====================

const InquirySuccessCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

const InquirySuccessTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
  line-height: 1.3;
  white-space: pre-line;
`;

const InquirySuccessButton = styled.button`
  margin-top: 0;
  width: 100%;
  height: 48px;
  background-color: #4965f6;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:hover {
    background-color: #3b5dd8;
  }
`;

export interface InquirySuccessModalProps extends BaseModalProps {
  onConfirm?: () => void;
}

export const InquirySuccessModal: React.FC<InquirySuccessModalProps> = ({
  open,
  onClose,
  onConfirm,
  className,
  style,
}) => {
  if (!open) return null;

  const handleConfirm = () => {
    if (onConfirm) {
      onConfirm();
    }
    onClose();
  };

  return (
    <Overlay onClick={onClose}>
      <InquirySuccessCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <InquirySuccessTitle>문의 사항이 전달되었습니다.</InquirySuccessTitle>
        <InquirySuccessButton onClick={handleConfirm}>
          확인
        </InquirySuccessButton>
      </InquirySuccessCard>
    </Overlay>
  );
};

// =====================
// 스트리밍 종료 모달
// =====================

const StreamingEndCard = styled.div`
  width: 600px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

const StreamingEndTitle = styled.h2`
  margin: 0 0 16px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
`;

const StreamingEndMessage = styled.p`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 16px;
  color: #6b7280;
  line-height: 1.6;
  white-space: pre-line;
`;

const StreamingEndButton = styled.button`
  width: 100%;
  padding: 12px 24px;
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

export interface StreamingEndModalProps extends BaseModalProps {
  onConfirm?: () => void;
}

export const StreamingEndModal: React.FC<StreamingEndModalProps> = ({
  open,
  onClose,
  onConfirm,
  className,
  style,
}) => {
  if (!open) return null;

  const handleConfirm = () => {
    if (onConfirm) {
      onConfirm();
    }
    onClose();
  };

  return (
    <Overlay onClick={onClose}>
      <StreamingEndCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <StreamingEndTitle>상담이 종료되었습니다.</StreamingEndTitle>
        <StreamingEndMessage>
          AI를 통한 상담 내용 요약본은
          <br />
          잠시 후 '지난 예약 현황 - 상담 요약본'에서 확인하실 수 있습니다.
        </StreamingEndMessage>
        <StreamingEndButton onClick={handleConfirm}>확인</StreamingEndButton>
      </StreamingEndCard>
    </Overlay>
  );
};

export const ApplicationApprovalModal: React.FC<
  ApplicationApprovalModalProps
> = ({ open, onClose, members = [], onMemberInfoClick, className, style }) => {
  const [activeTab, setActiveTab] = useState<"pending" | "approved">("pending");
  const [pendingMembers, setPendingMembers] = useState<ApplicationMember[]>([]);
  const [approvedMembers, setApprovedMembers] = useState<ApplicationMember[]>(
    []
  );
  const [showMemberInfoModal, setShowMemberInfoModal] = useState(false);
  const [selectedMemberName, setSelectedMemberName] = useState<string>("");
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null);

  // 초기 회원 목록 설정
  useEffect(() => {
    if (open) {
      setPendingMembers(members);
      setApprovedMembers([]);
      setActiveTab("pending");
    }
  }, [open, members]);

  const handleApprove = (member: ApplicationMember) => {
    setPendingMembers((prev) => prev.filter((m) => m.id !== member.id));
    setApprovedMembers((prev) => [...prev, member]);
  };

  const handleCancelApproval = (member: ApplicationMember) => {
    setApprovedMembers((prev) => prev.filter((m) => m.id !== member.id));
    setPendingMembers((prev) => [...prev, member]);
  };

  const handleMemberInfoClick = (memberName: string, memberId?: string) => {
    setSelectedMemberName(memberName);
    setSelectedMemberId(memberId || null);
    setShowMemberInfoModal(true);
    if (onMemberInfoClick) {
      onMemberInfoClick(memberName);
    }
  };

  const handleConfirm = () => {
    onClose();
  };

  if (!open) return null;

  const currentMembers =
    activeTab === "pending" ? pendingMembers : approvedMembers;

  return (
    <>
      <Overlay onClick={onClose}>
        <ApplicationApprovalCard
          className={className}
          style={style}
          onClick={(e) => e.stopPropagation()}
        >
          <ApplicationApprovalTitle>
            신청 회원 예약 승인
          </ApplicationApprovalTitle>

          <ApplicationTabsContainer>
            <SegmentedTabs
              leftLabel="승인 대기"
              rightLabel="승인 완료"
              active={activeTab === "pending" ? "left" : "right"}
              onLeftClick={() => setActiveTab("pending")}
              onRightClick={() => setActiveTab("approved")}
              tabWidth={120}
              showDivider={false}
            />
          </ApplicationTabsContainer>

          <ApplicationMemberList>
            {currentMembers.length === 0 ? (
              <div
                style={{
                  textAlign: "center",
                  padding: "40px 0",
                  color: "#6b7280",
                  fontSize: "14px",
                }}
              >
                {activeTab === "pending"
                  ? "승인 대기 중인 회원이 없습니다."
                  : "승인 완료된 회원이 없습니다."}
              </div>
            ) : (
              currentMembers.map((member) => (
                <ApplicationMemberItem key={member.id}>
                  <ApplicationProfileImage
                    src={profilePictureIcon}
                    alt={member.name}
                  />
                  <ApplicationMemberName>
                    {member.name} 회원님
                  </ApplicationMemberName>
                  <ApplicationButtonContainer>
                    {activeTab === "pending" ? (
                      <ApplicationApproveButton
                        onClick={() => handleApprove(member)}
                      >
                        승인
                      </ApplicationApproveButton>
                    ) : (
                      <>
                        <ApplicationMemberInfoButton
                          onClick={() =>
                            handleMemberInfoClick(member.name, member.id)
                          }
                        >
                          회원 정보
                        </ApplicationMemberInfoButton>
                        <ApplicationCancelApprovalButton
                          onClick={() => handleCancelApproval(member)}
                        >
                          승인 취소
                        </ApplicationCancelApprovalButton>
                      </>
                    )}
                  </ApplicationButtonContainer>
                </ApplicationMemberItem>
              ))
            )}
          </ApplicationMemberList>

          <ApplicationConfirmButton onClick={handleConfirm}>
            확인
          </ApplicationConfirmButton>
        </ApplicationApprovalCard>
      </Overlay>

      {/* 회원 정보 모달 */}
      <MemberInfoModal
        open={showMemberInfoModal}
        onClose={() => {
          setShowMemberInfoModal(false);
          setSelectedMemberId(null);
        }}
        memberName={selectedMemberName}
        memberId={selectedMemberId || undefined}
      />
    </>
  );
};

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: React.ReactNode;
  size?: "small" | "medium" | "large";
}

export const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  title,
  children,
  size = "medium",
}) => {
  if (!isOpen) return null;

  const sizeClasses = {
    small: "max-w-md",
    medium: "max-w-lg",
    large: "max-w-2xl",
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black bg-opacity-50"
        onClick={onClose}
      />

      {/* Modal */}
      <div
        className={`relative bg-white rounded-lg shadow-xl w-full mx-4 ${sizeClasses[size]}`}
      >
        {/* Header */}
        {title && (
          <div className="flex items-center justify-between p-6 border-b">
            <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors"
            >
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>
        )}

        {/* Content */}
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
};

