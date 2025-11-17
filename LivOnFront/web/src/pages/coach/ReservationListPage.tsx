import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import { SegmentedTabs } from "../../components/common/Button";
import { Dropdown } from "../../components/common/Dropdown";
import {
  MemberInfoModal,
  ApplicationApprovalModal,
  ReservationCancelConfirmModal,
  ReservationCancelSuccessModal,
} from "../../components/common/Modal";
import type { ApplicationMember } from "../../components/common/Modal";
import { useAuth } from "../../hooks/useAuth";
import { ROUTES } from "../../constants/routes";
import {
  getCoachConsultationsApi,
  cancelIndividualConsultationApi,
  cancelGroupConsultationParticipationApi,
} from "../../api/reservationApi";
import { CONFIG } from "../../constants/config";

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
  box-sizing: border-box;
  width: 100%;
`;

const TitleAndMessageContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  position: relative;
  gap: 12px;

  @media (max-width: 1200px) {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 8px;
  }
`;

const PageTitle = styled.h1`
  font-weight: 700;
  font-size: 40px;
  color: #000000;
  margin: 0;
  align-self: flex-start;

  @media (max-width: 1200px) {
    align-self: center;
    text-align: center;
    font-size: 34px;
  }

  @media (max-width: 900px) {
    align-self: center;
    font-size: 30px;
  }

  @media (max-width: 768px) {
    align-self: center;
    text-align: center;
    font-size: 26px;
  }

  @media (max-width: 480px) {
    font-size: 24px;
  }
`;

const ScheduleMessage = styled.div`
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 500;
  font-size: 28px;
  color: #000000;
  line-height: 1.5;
  white-space: pre-line;
  text-align: center;

  @media (max-width: 1200px) {
    position: static;
    transform: none;
    width: 100%;
    margin-top: 0;
  }

  @media (max-width: 768px) {
    font-size: 20px;
  }

  @media (max-width: 480px) {
    font-size: 18px;
  }
`;

const TabsAndFilterContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 0;
`;

const TabsWrapper = styled.div`
  display: flex;
  align-items: center;
`;

const FilterDropdown = styled.div`
  display: flex;
  align-items: center;
  min-width: 180px;
  margin-left: auto;
`;

const ReservationTable = styled.table`
  width: 100%;
  max-width: 100%;
  border-collapse: collapse;
  margin-top: 24px;
  table-layout: auto;
  box-sizing: border-box;
`;

const TableHeader = styled.thead`
  background-color: #f9fafb;
`;

const TableBody = styled.tbody``;

const TableHeaderCell = styled.th`
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 14px;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
  white-space: nowrap;
  width: auto;

  @media (max-width: 768px) {
    font-size: 13px;
  }
`;

const TableRow = styled.tr`
  border-bottom: 1px solid #e5e7eb;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: transparent;
  }
`;

const TableCell = styled.td`
  padding: 16px;
  font-size: 14px;
  color: #374151;
  vertical-align: middle;
  word-break: keep-all;
  white-space: normal;

  &:nth-child(1) {
    width: 200px;
    white-space: nowrap;
  }

  &:last-child {
    min-width: 360px;
  }

  @media (max-width: 960px) {
    padding: 16px 8px;

    &:last-child {
      min-width: 0;
      width: auto;
      padding: 16px 2px 16px 6px;
    }
  }

  @media (max-width: 900px) {
    padding: 14px 8px;

    &:last-child {
      min-width: 0;
      width: auto;
      padding: 14px 2px 14px 6px;
    }
  }

  @media (max-width: 768px) {
    padding: 12px 6px;
    font-size: 13px;

    &:nth-child(1) {
      width: 170px;
      white-space: nowrap;
    }

    &:last-child {
      min-width: 0;
      width: auto;
      padding: 12px 2px 12px 4px;
    }
  }
`;

const ClassTitle = styled.span`
  display: block;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
`;

const ClassDescription = styled.span`
  display: block;
  font-size: 13px;
  color: #6b7280;
`;

const ClassCapacityInfo = styled.span`
  display: block;
  font-size: 12px;
  color: #4965f6;
  margin-top: 4px;
  font-weight: 500;
`;

const ActionButtonContainer = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;

  @media (max-width: 960px) {
    flex-wrap: nowrap;
    justify-content: flex-end;
    gap: 6px;
  }

  @media (max-width: 768px) {
    gap: 4px;
  }
`;

const StartConsultationButton = styled.button`
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
  flex-shrink: 0;

  &:hover {
    background-color: #f7fafc;
  }
`;

const ViewMemberButton = styled.button<{ $compact?: boolean }>`
  width: ${(props) => (props.$compact ? "85px" : "86px")};
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background-color: #ffffff;
  color: #4965f6;
  border: 1px solid #4965f6;
  border-radius: 8px;
  font-size: ${(props) => (props.$compact ? "14px" : "12px")};
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  text-align: center;
  white-space: nowrap;
  line-height: 1;
  flex-shrink: 0;

  &:hover {
    background-color: #f7fafc;
  }
`;

const CancelButton = styled.button`
  padding: 8px 16px;
  background-color: #ffffff;
  color: #ff0000;
  border: 1px solid #ff0000;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  white-space: nowrap;
  flex-shrink: 0;

  &:hover {
    background-color: #fef2f2;
  }
`;

const PaginationContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
  gap: 8px;
`;

const EmptyMessage = styled.div`
  text-align: center;
  padding: 80px 20px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
`;

const PaginationButton = styled.button<{ $active?: boolean }>`
  min-width: 36px;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background-color: ${(props) => (props.$active ? "#4965f6" : "#ffffff")};
  color: ${(props) => (props.$active ? "#ffffff" : "#6b7280")};
  font-size: 14px;
  font-weight: ${(props) => (props.$active ? "600" : "400")};
  cursor: pointer;

  &:hover {
    background-color: ${(props) => (props.$active ? "#3b5dd8" : "#f9fafb")};
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;

const LoadingMessage = styled.div`
  text-align: center;
  padding: 80px 20px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 500;
  font-size: 16px;
  color: #6b7280;
`;

const ErrorMessage = styled.div`
  text-align: center;
  padding: 80px 20px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 500;
  font-size: 16px;
  color: #ef4444;
`;

// 에러 메시지 상수
const ERROR_MESSAGES = {
  FETCH_FAILED: "예약 목록을 불러오는데 실패했습니다.",
  CANCEL_FAILED: "예약 취소에 실패했습니다.",
  TOKEN_REQUIRED: "인증 토큰이 없습니다. 로그인이 필요합니다.",
} as const;

interface CoachConsultation {
  consultationId: number;
  type: string;
  startAt: string;
  endAt: string;
  preQna?: string;
  title?: string;
  description?: string;
  capacity?: number;
  currentParticipants?: number;
  participants?: Array<{
    userId: string;
    nickname: string;
    profileImage: string;
    email: string;
    status?: "PENDING" | "APPROVED" | string;
    applicationStatus?: "PENDING" | "APPROVED" | string;
  }>;
}

export const ReservationListPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, isLoading } = useAuth();

  // 코치 전용 가드
  useEffect(() => {
    if (!isLoading && user && user.role !== "coach") {
      navigate(ROUTES.COACH_ONLY, { replace: true });
    }
  }, [isLoading, user, navigate]);
  const [filterValue, setFilterValue] = useState("전체");
  const [showMemberInfoModal, setShowMemberInfoModal] = useState(false);
  const [selectedMemberName, setSelectedMemberName] = useState<string>("");
  const [showApplicationApprovalModal, setShowApplicationApprovalModal] =
    useState(false);
  const [selectedParticipants, setSelectedParticipants] = useState<
    ApplicationMember[]
  >([]);
  const [
    selectedApplicationConsultationId,
    setSelectedApplicationConsultationId,
  ] = useState<number | null>(null);
  const [showCancelConfirmModal, setShowCancelConfirmModal] = useState(false);
  const [showCancelSuccessModal, setShowCancelSuccessModal] = useState(false);
  const [cancelReservationId, setCancelReservationId] = useState<number | null>(
    null
  );
  const [cancelReservationType, setCancelReservationType] = useState<
    string | null
  >(null);

  // API 관련 상태
  const [reservations, setReservations] = useState<CoachConsultation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const pageSize = 10;
  // 취소된 예약 ID 추적 (새로고침 시 제외하기 위함) - localStorage에서 불러오기
  const loadCancelledReservationIds = (): Set<number> => {
    try {
      const stored = localStorage.getItem("cancelledReservationIds");
      if (stored) {
        const ids = JSON.parse(stored) as number[];
        return new Set(ids);
      }
    } catch (e) {
      console.error("취소된 예약 ID 로드 오류:", e);
    }
    return new Set<number>();
  };
  const [cancelledReservationIds, setCancelledReservationIds] = useState<Set<number>>(
    loadCancelledReservationIds
  );
  
  // 취소된 예약 ID를 localStorage에 저장하는 헬퍼 함수
  const saveCancelledReservationIds = (ids: Set<number>) => {
    try {
      localStorage.setItem("cancelledReservationIds", JSON.stringify(Array.from(ids)));
    } catch (e) {
      console.error("취소된 예약 ID 저장 오류:", e);
    }
  };

  const nickname = user?.nickname || "";
  const scheduleMessage = nickname
    ? `${nickname} 코치님의\n다가오는 상담/코칭 스케줄입니다.`
    : "코치님의\n다가오는 상담/코칭 스케줄입니다.";

  // 날짜/시간 포맷팅 함수
  const formatDateTime = (
    startAt: string,
    endAt: string
  ): { date: string; time: string } => {
    try {
      const start = new Date(startAt);
      const end = new Date(endAt);

      const month = start.getMonth() + 1;
      const date = start.getDate();
      const dayNames = ["일", "월", "화", "수", "목", "금", "토"];
      const dayName = dayNames[start.getDay()];

      const startHour = start.getHours();
      const startMinute = String(start.getMinutes()).padStart(2, "0");
      const endHour = end.getHours();
      const endMinute = String(end.getMinutes()).padStart(2, "0");

      const period = startHour < 12 ? "오전" : "오후";
      const displayStartHour =
        startHour > 12 ? startHour - 12 : startHour === 0 ? 12 : startHour;
      const displayEndHour =
        endHour > 12 ? endHour - 12 : endHour === 0 ? 12 : endHour;

      return {
        date: `${month}.${date}(${dayName})`,
        time: `${period} ${displayStartHour}:${startMinute} ~ ${displayEndHour}:${endMinute}`,
      };
    } catch (e) {
      return { date: startAt, time: endAt };
    }
  };

  // 클래스 형태 변환 함수
  const getClassType = (type: string): string => {
    switch (type) {
      case "ONE":
        return "개인 상담 / 코칭";
      case "GROUP":
        return "일반 클래스";
      default:
        return "일반 클래스";
    }
  };

  // 필터 타입 변환 함수
  const getFilterType = (filterValue: string): "ONE" | "GROUP" | undefined => {
    switch (filterValue) {
      case "개인 상담 / 코칭":
        return "ONE";
      case "클래스":
      case "기업 클래스":
      case "일반 클래스":
        return "GROUP";
      default:
        return undefined;
    }
  };

  // 예약 목록 가져오기
  const fetchReservations = async (
    page: number = 0,
    type?: "ONE" | "GROUP"
  ) => {
    try {
      setLoading(true);
      setError(null);
      const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);

      if (!token) {
        throw new Error(ERROR_MESSAGES.TOKEN_REQUIRED);
      }

      const response = await getCoachConsultationsApi(
        token,
        "upcoming",
        type,
        page,
        pageSize
      );

      // 취소된 예약 ID를 제외하고 필터링 (localStorage에서 최신 상태 불러오기)
      const currentCancelledIds = loadCancelledReservationIds();
      const filteredItems = response.items.filter(
        (item) => !currentCancelledIds.has(item.consultationId)
      );

      setReservations(filteredItems);
      setCurrentPage(response.page);
      setTotalPages(response.totalPages);
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : ERROR_MESSAGES.FETCH_FAILED;
      setError(errorMessage);
      console.error("예약 목록 조회 오류:", err);
    } finally {
      setLoading(false);
    }
  };

  // 컴포넌트 마운트 시 및 필터 변경 시 데이터 가져오기
  useEffect(() => {
    const type = getFilterType(filterValue);
    fetchReservations(0, type);
  }, [filterValue]);

  // 페이지 변경 시 데이터 가져오기
  const handlePageChange = (page: number) => {
    if (page >= 0 && page < totalPages) {
      const type = getFilterType(filterValue);
      fetchReservations(page, type);
    }
  };

  const handleCurrentClick = () => {
    // 현재 예약 페이지에 있으므로 아무 동작 없음
  };

  const handlePastClick = () => {
    navigate(ROUTES.PAST_RESERVATION);
  };

  const handleFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFilterValue(e.target.value);
    setCurrentPage(0); // 필터 변경 시 첫 페이지로
  };

  const handleStartConsultation = (consultationId: number) => {
    navigate(ROUTES.STREAMING, {
      state: { consultationId },
    });
  };

  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null);
  const [selectedConsultationId, setSelectedConsultationId] = useState<
    number | null
  >(null);
  const [
    shouldReopenApplicationModal,
    setShouldReopenApplicationModal,
  ] = useState(false);

  const handleViewMember = (
    memberName: string,
    memberId?: string,
    consultationId?: number,
    options?: { reopenApplicationModal?: boolean }
  ) => {
    setSelectedMemberName(memberName);
    setSelectedMemberId(memberId || null);
    setSelectedConsultationId(consultationId || null);
    setShouldReopenApplicationModal(Boolean(options?.reopenApplicationModal));
    setShowMemberInfoModal(true);
  };

  const handleCloseMemberInfoModal = () => {
    setShowMemberInfoModal(false);
    setSelectedMemberId(null);
    setSelectedConsultationId(null);
    if (shouldReopenApplicationModal) {
      setShowApplicationApprovalModal(true);
      setShouldReopenApplicationModal(false);
    }
  };

  const handleViewAppliedMembers = (
    consultationId: number,
    participants?: Array<{
      userId: string;
      nickname: string;
      profileImage: string;
      email: string;
      status?: "PENDING" | "APPROVED" | string;
      applicationStatus?: "PENDING" | "APPROVED" | string;
    }>
  ) => {
    if (participants && participants.length > 0) {
      const normalizedParticipants: ApplicationMember[] = participants.map(
        (participant) => ({
          id: participant.userId,
          name: participant.nickname,
          status:
            participant.status === "APPROVED" ||
            participant.applicationStatus === "APPROVED"
              ? "APPROVED"
              : "PENDING",
        })
      );
      setSelectedParticipants(normalizedParticipants);
      setSelectedApplicationConsultationId(consultationId);
      setShowApplicationApprovalModal(true);
    } else {
      setSelectedParticipants([]);
      setSelectedApplicationConsultationId(null);
    }
  };

  const handleCancelClick = (reservationId: number, type: string) => {
    setCancelReservationId(reservationId);
    setCancelReservationType(type);
    setShowCancelConfirmModal(true);
  };

  const handleCancelConfirm = async () => {
    if (cancelReservationId === null || !cancelReservationType) return;

    const reservationIdToRemove = cancelReservationId;

    // 모달 먼저 닫기
    closeCancelModal();

    // 취소된 예약 ID에 추가 (새로고침 시에도 제외되도록) - localStorage에도 저장
    setCancelledReservationIds((prev) => {
      const newSet = new Set(prev).add(reservationIdToRemove);
      saveCancelledReservationIds(newSet);
      return newSet;
    });

    // 목록에서 해당 예약 즉시 제거 (UI 반응성 향상)
    setReservations((prev) =>
      prev.filter((reservation) => reservation.consultationId !== reservationIdToRemove)
    );

    // 성공 모달 표시
    setShowCancelSuccessModal(true);

    try {
      const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);
      if (!token) {
        throw new Error(ERROR_MESSAGES.TOKEN_REQUIRED);
      }

      if (cancelReservationType === "ONE") {
        await cancelIndividualConsultationApi(token, cancelReservationId);
      } else if (cancelReservationType === "GROUP") {
        await cancelGroupConsultationParticipationApi(
          token,
          cancelReservationId
        );
      }

      // 목록 새로고침하지 않음 (이미 목록에서 제거했고, cancelledReservationIds로 추적 중)
      // 필요시 백그라운드에서 새로고침 (사용자 경험에 영향 없음)
      setTimeout(() => {
        const type = getFilterType(filterValue);
        fetchReservations(currentPage, type);
      }, 1000);
    } catch (err: any) {
      // 404 또는 400 에러인 경우에도 목록에서 제거 유지 (이미 취소되었거나 존재하지 않는 예약일 수 있음)
      if (err?.response?.status === 404 || err?.response?.status === 400) {
        console.warn(`예약 취소 API ${err?.response?.status} 응답 (이미 취소되었거나 존재하지 않는 예약일 수 있음):`, err);
        // 목록 새로고침하지 않음 (이미 목록에서 제거했고, cancelledReservationIds로 추적 중)
        setTimeout(() => {
          const type = getFilterType(filterValue);
          fetchReservations(currentPage, type);
        }, 1000);
      } else {
        // 다른 에러인 경우 취소된 ID에서 제거하고 목록 복원 (실제 취소가 실패했을 수 있음)
        setCancelledReservationIds((prev) => {
          const newSet = new Set(prev);
          newSet.delete(reservationIdToRemove);
          saveCancelledReservationIds(newSet);
          return newSet;
        });
        
        const type = getFilterType(filterValue);
        fetchReservations(currentPage, type);
        
        const errorMessage =
          err instanceof Error ? err.message : ERROR_MESSAGES.CANCEL_FAILED;
        setError(errorMessage);
        setShowCancelSuccessModal(false);
        console.error("예약 취소 오류:", err);
      }
    }
  };

  // 모달 닫기 핸들러
  const closeCancelModal = () => {
    setShowCancelConfirmModal(false);
    setCancelReservationId(null);
    setCancelReservationType(null);
  };

  const filterOptions = [
    { value: "전체", label: "전체" },
    { value: "클래스", label: "클래스" },
    { value: "개인 상담 / 코칭", label: "개인 상담 / 코칭" },
  ];

  return (
    <PageContainer>
      <ContentWrapper>
        <TitleAndMessageContainer>
          <PageTitle>예약 현황</PageTitle>
          <ScheduleMessage>{scheduleMessage}</ScheduleMessage>
        </TitleAndMessageContainer>

        <TabsAndFilterContainer>
          <TabsWrapper>
            <SegmentedTabs
              leftLabel="현재 예약"
              rightLabel="지난 예약"
              active="left"
              onLeftClick={handleCurrentClick}
              onRightClick={handlePastClick}
              tabWidth={120}
              showDivider={false}
            />
          </TabsWrapper>

          <FilterDropdown>
            <Dropdown
              options={filterOptions}
              value={filterValue}
              onChange={handleFilterChange}
              style={{ width: "100%" }}
            />
          </FilterDropdown>
        </TabsAndFilterContainer>

        {loading ? (
          <LoadingMessage>예약 목록을 불러오는 중...</LoadingMessage>
        ) : error ? (
          <ErrorMessage>{error}</ErrorMessage>
        ) : reservations.length === 0 ? (
          <EmptyMessage>현재 상담 예약 신청 내역이 없습니다.</EmptyMessage>
        ) : (
          <>
            <ReservationTable>
              <TableHeader>
                <tr>
                  <TableHeaderCell>날짜 / 시간</TableHeaderCell>
                  <TableHeaderCell>클래스</TableHeaderCell>
                  <TableHeaderCell>클래스 형태</TableHeaderCell>
                  <TableHeaderCell></TableHeaderCell>
                </tr>
              </TableHeader>
              <TableBody>
                {reservations.map((reservation) => {
                  const { date, time } = formatDateTime(
                    reservation.startAt,
                    reservation.endAt
                  );
                  const classType = getClassType(reservation.type);
                  const isIndividual = reservation.type === "ONE";
                  // 코치 본인을 제외한 첫 번째 참가자(회원) 찾기
                  // user.id는 이메일일 수 있고, participants.userId는 UUID이므로
                  // userId와 email 둘 다 비교해야 함
                  const coachUserId = user?.id;
                  const coachEmail = user?.email;
                  // console.log("🔍 [ReservationListPage] Debug Info:", {
                  //   consultationId: reservation.consultationId,
                  //   coachUserId: coachUserId,
                  //   coachEmail: coachEmail,
                  //   coachUserNickname: user?.nickname,
                  //   participants: reservation.participants?.map((p) => ({
                  //     userId: p.userId,
                  //     email: p.email,
                  //     nickname: p.nickname,
                  //     isCoachByUserId: p.userId === coachUserId,
                  //     isCoachByEmail: p.email === coachEmail,
                  //   })),
                  // });
                  const firstParticipant =
                    reservation.participants &&
                    reservation.participants.length > 0
                      ? reservation.participants.find((participant) => {
                          // userId와 email 둘 다 비교
                          const isNotCoach =
                            participant.userId !== coachUserId &&
                            participant.email !== coachEmail;
                          // console.log(
                          //   `  - Checking participant: ${participant.nickname} (userId: ${participant.userId}, email: ${participant.email}) - isNotCoach: ${isNotCoach}`
                          // );
                          return isNotCoach;
                        }) || null
                      : null;
                  // console.log(
                  //   `  ✅ Selected firstParticipant: ${firstParticipant?.nickname} (${firstParticipant?.userId})`
                  // );

                  return (
                    <TableRow key={reservation.consultationId}>
                      <TableCell>
                        {date} {time}
                      </TableCell>
                      <TableCell>
                        <ClassTitle>
                          {reservation.type === "ONE"
                            ? "개인 상담 / 코칭"
                            : reservation.title || "제목 없음"}
                        </ClassTitle>
                        {reservation.type === "GROUP" &&
                          reservation.description && (
                            <ClassDescription>
                              {reservation.description}
                            </ClassDescription>
                          )}
                        {reservation.type === "GROUP" &&
                          reservation.capacity !== undefined &&
                          reservation.currentParticipants !== undefined && (
                            <ClassCapacityInfo>
                              예약 인원: {reservation.currentParticipants} /{" "}
                              {reservation.capacity}명
                            </ClassCapacityInfo>
                          )}
                      </TableCell>
                      <TableCell style={{ color: "#4965f6" }}>
                        {classType}
                      </TableCell>
                      <TableCell>
                        <ActionButtonContainer>
                          <StartConsultationButton
                            onClick={() =>
                              handleStartConsultation(
                                reservation.consultationId
                              )
                            }
                          >
                            상담 시작
                          </StartConsultationButton>
                          {isIndividual && firstParticipant ? (
                      <ViewMemberButton
                        onClick={() =>
                          handleViewMember(
                            firstParticipant.nickname,
                            firstParticipant.userId,
                            reservation.consultationId
                          )
                        }
                      >
                              {firstParticipant.nickname} 회원 보기
                            </ViewMemberButton>
                          ) : (
                            <ViewMemberButton
                              $compact
                              onClick={() =>
                                handleViewAppliedMembers(
                                  reservation.consultationId,
                                  reservation.participants
                                )
                              }
                            >
                              신청 회원
                            </ViewMemberButton>
                          )}
                          <CancelButton
                            onClick={() =>
                              handleCancelClick(
                                reservation.consultationId,
                                reservation.type
                              )
                            }
                          >
                            예약 취소
                          </CancelButton>
                        </ActionButtonContainer>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </ReservationTable>

            {totalPages > 1 && (
              <PaginationContainer>
                <PaginationButton
                  disabled={currentPage === 0}
                  onClick={() => handlePageChange(currentPage - 1)}
                >
                  ‹
                </PaginationButton>
                {Array.from({ length: totalPages }, (_, i) => i).map((page) => {
                  // 페이지 번호 표시 로직: 현재 페이지 주변만 표시
                  if (
                    page === 0 ||
                    page === totalPages - 1 ||
                    (page >= currentPage - 2 && page <= currentPage + 2)
                  ) {
                    return (
                      <PaginationButton
                        key={page}
                        $active={page === currentPage}
                        onClick={() => handlePageChange(page)}
                      >
                        {page + 1}
                      </PaginationButton>
                    );
                  } else if (
                    page === currentPage - 3 ||
                    page === currentPage + 3
                  ) {
                    return <span key={page}>...</span>;
                  }
                  return null;
                })}
                <PaginationButton
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => handlePageChange(currentPage + 1)}
                >
                  ›
                </PaginationButton>
              </PaginationContainer>
            )}
          </>
        )}

        {/* 회원 정보 모달 */}
        <MemberInfoModal
          open={showMemberInfoModal}
          onClose={handleCloseMemberInfoModal}
          memberName={selectedMemberName}
          memberId={selectedMemberId || undefined}
          consultationId={selectedConsultationId || undefined}
          question={
            reservations.find(
              (r) => r.consultationId === selectedConsultationId
            )?.preQna || undefined
          }
        />

        {/* 신청 회원 예약 승인 모달 */}
        <ApplicationApprovalModal
          open={showApplicationApprovalModal}
          onClose={() => {
            setShowApplicationApprovalModal(false);
            setSelectedApplicationConsultationId(null);
            setSelectedMemberId(null);
            setShouldReopenApplicationModal(false);
          }}
          members={selectedParticipants}
          consultationId={selectedApplicationConsultationId || undefined}
          onMemberInfoClick={(memberName, memberId) => {
            handleViewMember(memberName, memberId, undefined, {
              reopenApplicationModal: true,
            });
            setShowApplicationApprovalModal(false);
          }}
        />

        {/* 예약 취소 확인 모달 */}
        <ReservationCancelConfirmModal
          open={showCancelConfirmModal}
          onClose={closeCancelModal}
          onConfirm={handleCancelConfirm}
        />

        {/* 예약 취소 완료 모달 */}
        <ReservationCancelSuccessModal
          open={showCancelSuccessModal}
          onClose={() => setShowCancelSuccessModal(false)}
          onConfirm={() => {
            // 확인 버튼을 눌러도 목록을 새로고침하지 않음 (이미 삭제된 상태 유지)
            setShowCancelSuccessModal(false);
          }}
        />
      </ContentWrapper>
    </PageContainer>
  );
};

export default ReservationListPage;
