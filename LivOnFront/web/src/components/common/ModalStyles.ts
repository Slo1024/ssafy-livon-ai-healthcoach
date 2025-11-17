import styled from "styled-components";
import type { CSSProperties } from "react";

export interface BaseModalProps {
  open: boolean;
  onClose: () => void;
  className?: string;
  style?: CSSProperties;
}

export const Overlay = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
`;

export const Card = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

export const Title = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
  line-height: 1.3;
  white-space: pre-line; /* 줄바꿈 지원 */
`;

export const ConfirmButton = styled.button`
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

// =====================
// 클래스 정보 수정 모달
// =====================
export const EditModalCard = styled.div`
  width: 680px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  background: #ffffff;
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
`;

export const EditModalTitle = styled.h2`
  margin: 0 0 32px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
  text-align: left;
`;

export const FormField = styled.div`
  margin-bottom: 24px;
`;

export const FormLabel = styled.label`
  display: block;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
`;

export const FormInput = styled.input`
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

export const FormTextArea = styled.textarea`
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

export const FormDropdown = styled.select`
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

export const ButtonRow = styled.div`
  display: flex;
  gap: 12px;
  margin-top: 32px;
  justify-content: flex-end;
`;

export const SaveButton = styled.button`
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

export const CloseButton = styled.button`
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

// =====================
// 삭제 확인 모달
// =====================
export const DeleteConfirmCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

export const DeleteConfirmTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
`;

export const DeleteConfirmButtonRow = styled.div`
  display: flex;
  gap: 12px;
  justify-content: center;
`;

export const DeleteButton = styled.button`
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

export const CancelButton = styled.button`
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

// =====================
// 저장 확인 모달
// =====================
export const SaveConfirmCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

export const SaveConfirmTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
`;

export const SaveConfirmButtonRow = styled.div`
  display: flex;
  gap: 12px;
  justify-content: center;
`;

export const SaveConfirmButton = styled.button`
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

export const SaveCancelButton = styled.button`
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

// =====================
// 클래스 생성 완료 모달
// =====================
export const ClassCreatedCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

export const ClassCreatedTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
  line-height: 1.3;
`;

export const ClassListButton = styled.button`
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

// =====================
// 회원 정보 모달
// =====================
export const MemberInfoCard = styled.div`
  width: 600px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
`;

export const MemberInfoTitle = styled.h2`
  margin: 0 0 8px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
`;

export const MemberInfoDescription = styled.p`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
`;

export const MemberContentContainer = styled.div`
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
`;

export const ProfileIconContainer = styled.div`
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

export const ProfileImage = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;

export const MemberDataContainer = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

export const MemberDataItem = styled.div`
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 16px;
  color: #111827;
`;

export const QASection = styled.div`
  margin-top: 24px;
 	padding-top: 24px;
  border-top: 1px solid #e5e7eb;
`;

export const QATitle = styled.h3`
  margin: 0 0 12px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 16px;
  color: #111827;
`;

export const QAQuestion = styled.div`
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
`;

export const MemberInfoConfirmButton = styled.button`
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

export const LoadingText = styled.div`
  text-align: center;
  padding: 20px;
  color: #6b7280;
  font-size: 14px;
`;

export const ErrorText = styled.div`
  text-align: center;
  padding: 20px;
  color: #ef4444;
  font-size: 14px;
`;

// =====================
// 신청 승인 모달
// =====================
export const ApplicationApprovalCard = styled.div`
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

export const ApplicationApprovalTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
`;

export const ApplicationTabsContainer = styled.div`
  margin-bottom: 24px;
`;

export const ApplicationMemberList = styled.div`
  flex: 1;
  overflow-y: auto;
  margin-bottom: 24px;
  max-height: 400px;
`;

export const ApplicationMemberItem = styled.div`
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e5e7eb;

  &:last-child {
    border-bottom: none;
  }
`;

export const ApplicationProfileImage = styled.img`
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  margin-right: 16px;
  background-color: #f3f4f6;
`;

export const ApplicationMemberName = styled.div`
  flex: 1;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 16px;
  font-weight: 500;
  color: #111827;
`;

export const ApplicationButtonContainer = styled.div`
  display: flex;
  gap: 8px;
`;

export const ApplicationMemberInfoButton = styled.button`
  padding: 8px 16px;
  background: #ffffff;
  color: #4965f6;
  border: 1px solid #4965f6;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  white-space: nowrap;

  &:hover {
    background-color: #f7fafc;
  }
`;

export const ApplicationApproveButton = styled.button`
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

export const ApplicationCancelApprovalButton = styled.button`
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

export const ApplicationConfirmButton = styled.button`
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

// =====================
// 예약 취소 확인/성공 모달
// =====================
export const ReservationCancelConfirmCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

export const ReservationCancelConfirmTitle = styled.h2`
  margin: 0 0 16px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
  line-height: 1.3;
  white-space: pre-line;
`;

export const ReservationCancelConfirmButtonContainer = styled.div`
  display: flex;
  gap: 8px;
  margin-top: 0;
`;

export const ReservationCancelConfirmButton = styled.button`
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

export const ReservationCancelSuccessCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

export const ReservationCancelSuccessTitle = styled.h2`
  margin: 0 0 16px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
  line-height: 1.3;
  white-space: pre-line;
`;

export const ReservationCancelSuccessButton = styled.button`
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

// =====================
// 상담 요약 모달
// =====================
export const ConsultationSummaryCard = styled.div`
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

export const ConsultationSummaryTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
`;

export const ConsultationSummaryDate = styled.div`
  margin-bottom: 8px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  color: #6b7280;
`;

export const ConsultationSummaryTime = styled.div`
  margin-bottom: 24px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  color: #6b7280;
`;

export const ConsultationSummaryContent = styled.div`
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

export const ConsultationSummaryConfirmButton = styled.button`
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

// =====================
// 신청자 상세 모달
// =====================
export const ApplicationMemberInfoCard = styled.div`
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

export const ApplicationMemberInfoTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
`;

export const ApplicationMemberInfoList = styled.div`
  flex: 1;
  overflow-y: auto;
  margin-bottom: 24px;
  max-height: 400px;
`;

export const ApplicationMemberInfoItem = styled.div`
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e5e7eb;

  &:last-child {
    border-bottom: none;
  }
`;

export const ApplicationMemberInfoProfileImage = styled.img`
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  margin-right: 16px;
  background-color: #f3f4f6;
`;

export const ApplicationMemberInfoName = styled.div`
  flex: 1;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 16px;
  font-weight: 500;
  color: #111827;
`;

export const ApplicationMemberInfoButtonContainer = styled.div`
  display: flex;
  gap: 8px;
`;

export const ApplicationMemberInfoMemberInfoButton = styled.button`
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

export const ApplicationMemberInfoDeleteButton = styled.button`
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

export const ApplicationMemberInfoConfirmButton = styled.button`
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

// =====================
// FAQ 답변 모달
// =====================
export const FAQAnswerCard = styled.div`
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

export const FAQAnswerTitle = styled.h2`
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

export const FAQAnswerQuestion = styled.span`
  font-weight: 700;
  color: #111827;
`;

export const FAQAnswerContent = styled.div`
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

export const FAQAnswerConfirmButton = styled.button`
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

// =====================
// 문의 완료 모달
// =====================
export const InquirySuccessCard = styled.div`
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

export const InquirySuccessTitle = styled.h2`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: #111827;
  line-height: 1.3;
  white-space: pre-line;
`;

export const InquirySuccessButton = styled.button`
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

// =====================
// 스트리밍 종료 모달
// =====================
export const StreamingEndCard = styled.div`
  width: 600px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 24px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  text-align: center;
`;

export const StreamingEndTitle = styled.h2`
  margin: 0 0 16px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
`;

export const StreamingEndMessage = styled.p`
  margin: 0 0 24px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 16px;
  color: #6b7280;
  line-height: 1.6;
  white-space: pre-line;
`;

export const StreamingEndButton = styled.button`
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