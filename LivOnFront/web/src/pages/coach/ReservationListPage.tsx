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
  border-collapse: collapse;
  margin-top: 24px;
`;

const TableHeader = styled.thead`
  background-color: #f9fafb;
`;

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

const TableBody = styled.tbody``;

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
  @media (max-width: 900px) {
    padding: 14px 12px;
  }

  @media (max-width: 768px) {
    padding: 12px 10px;
    font-size: 13px;

    &:nth-child(1) {
      width: 170px;
      white-space: nowrap;
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

const ActionButtonContainer = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
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

interface CoachConsultation {
  consultationId: number;
  type: string;
  status: string;
  startAt: string;
  endAt: string;
  sessionId: string;
  preQna?: string;
  aiSummary?: string;
  title?: string;
  description?: string;
  imageUrl?: string;
  capacity?: number;
  currentParticipants?: number;
  participants?: Array<{
    userId: string;
    nickname: string;
    profileImage: string;
    email: string;
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
  const [activeTab, setActiveTab] = useState<"current" | "past">("current");
  const [filterValue, setFilterValue] = useState("전체");
  const [showMemberInfoModal, setShowMemberInfoModal] = useState(false);
  const [selectedMemberName, setSelectedMemberName] = useState<string>("");
  const [showApplicationApprovalModal, setShowApplicationApprovalModal] =
    useState(false);
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
  const [totalItems, setTotalItems] = useState(0);
  const pageSize = 10;

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
        throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
      }

      const response = await getCoachConsultationsApi(
        token,
        "upcoming",
        type,
        page,
        pageSize
      );

      setReservations(response.items);
      setCurrentPage(response.page);
      setTotalPages(response.totalPages);
      setTotalItems(response.totalItems);
    } catch (err) {
      const errorMessage =
        err instanceof Error
          ? err.message
          : "예약 목록을 불러오는데 실패했습니다.";
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
    setActiveTab("current");
  };

  const handlePastClick = () => {
    setActiveTab("past");
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

  const handleViewMember = (
    memberName: string,
    memberId?: string,
    consultationId?: number
  ) => {
    setSelectedMemberName(memberName);
    setSelectedMemberId(memberId || null);
    setSelectedConsultationId(consultationId || null);
    setShowMemberInfoModal(true);
  };

  const handleViewAppliedMembers = (
    participants?: Array<{
      userId: string;
      nickname: string;
      profileImage: string;
      email: string;
    }>
  ) => {
    if (participants && participants.length > 0) {
      setShowApplicationApprovalModal(true);
    }
  };

  const handleCancelClick = (reservationId: number, type: string) => {
    setCancelReservationId(reservationId);
    setCancelReservationType(type);
    setShowCancelConfirmModal(true);
  };

  const handleCancelConfirm = async () => {
    if (cancelReservationId === null || !cancelReservationType) return;

    try {
      const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);
      if (!token) {
        throw new Error("인증 토큰이 없습니다.");
      }

      if (cancelReservationType === "ONE") {
        await cancelIndividualConsultationApi(token, cancelReservationId);
      } else if (cancelReservationType === "GROUP") {
        await cancelGroupConsultationParticipationApi(
          token,
          cancelReservationId
        );
      }

      setShowCancelConfirmModal(false);
      setShowCancelSuccessModal(true);
      setCancelReservationId(null);
      setCancelReservationType(null);

      // 목록 새로고침
      const type = getFilterType(filterValue);
      fetchReservations(currentPage, type);
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "예약 취소에 실패했습니다.";
      setError(errorMessage);
      setShowCancelConfirmModal(false);
      setCancelReservationId(null);
      setCancelReservationType(null);
      console.error("예약 취소 오류:", err);
    }
  };

  const handleCancelSuccess = () => {
    setShowCancelSuccessModal(false);
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
              active={activeTab === "current" ? "left" : "right"}
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
                  const firstParticipant =
                    reservation.participants &&
                    reservation.participants.length > 0
                      ? reservation.participants[0]
                      : null;

                  return (
                    <TableRow key={reservation.consultationId}>
                      <TableCell>
                        {date} {time}
                      </TableCell>
                      <TableCell>
                        <ClassTitle>
                          {reservation.title || "제목 없음"}
                        </ClassTitle>
                        <ClassDescription>
                          {reservation.description || ""}
                        </ClassDescription>
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
          onClose={() => {
            setShowMemberInfoModal(false);
            setSelectedMemberId(null);
            setSelectedConsultationId(null);
          }}
          memberName={selectedMemberName}
          memberId={selectedMemberId || undefined}
          consultationId={selectedConsultationId || undefined}
          question={
            reservations.find(
              (r) => r.consultationId === selectedConsultationId
            )?.preQna
          }
        />

        {/* 신청 회원 예약 승인 모달 */}
        <ApplicationApprovalModal
          open={showApplicationApprovalModal}
          onClose={() => setShowApplicationApprovalModal(false)}
          members={reservations
            .filter((r) => r.participants && r.participants.length > 0)
            .flatMap((r) => r.participants || [])
            .map((p) => ({ id: p.userId, name: p.nickname }))}
        />

        {/* 예약 취소 확인 모달 */}
        <ReservationCancelConfirmModal
          open={showCancelConfirmModal}
          onClose={() => {
            setShowCancelConfirmModal(false);
            setCancelReservationId(null);
          }}
          onConfirm={handleCancelConfirm}
        />

        {/* 예약 취소 완료 모달 */}
        <ReservationCancelSuccessModal
          open={showCancelSuccessModal}
          onClose={() => setShowCancelSuccessModal(false)}
          onConfirm={handleCancelSuccess}
        />
      </ContentWrapper>
    </PageContainer>
  );
};

export default ReservationListPage;
