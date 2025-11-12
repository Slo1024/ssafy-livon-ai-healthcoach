import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import { SegmentedTabs } from "../../components/common/Button";
import { Dropdown } from "../../components/common/Dropdown";
import {
  MemberInfoModal,
  ApplicationApprovalModal,
  ConsultationSummaryModal,
  ApplicationMemberInfoModal,
} from "../../components/common/Modal";
import { useAuth } from "../../hooks/useAuth";
import { ROUTES } from "../../constants/routes";
import { getCoachConsultationsApi } from "../../api/reservationApi";
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
    gap: 16px;
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

const Divider = styled.div`
  width: 100vw;
  height: 2px;
  background-color: #4965f6;
  margin: 0;
  position: relative;
  left: 50%;
  transform: translateX(-50%);
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
  color: #111827;
  border-bottom: 1px solid #e5e7eb;
`;

const TableBody = styled.tbody``;

const TableRow = styled.tr`
  border-bottom: 1px solid #e5e7eb;

  &:hover {
    background-color: #f9fafb;
  }
`;

const TableCell = styled.td`
  padding: 16px;
  font-size: 14px;
  color: #374151;
  vertical-align: middle;
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
  flex-wrap: wrap;
`;

const MemberInfoButton = styled.button`
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

const ConsultationSummaryButton = styled.button`
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

const ViewMemberButton = styled.button`
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

const PaginationContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
  gap: 8px;
`;

const PaginationButton = styled.button<{ $active?: boolean }>`
  min-width: 36px;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  background-color: ${(props) => (props.$active ? "#4965f6" : "#ffffff")};
  color: ${(props) => (props.$active ? "#ffffff" : "#374151")};
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:hover:not(:disabled) {
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

const EmptyMessage = styled.div`
  text-align: center;
  padding: 80px 20px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 24px;
  color: #111827;
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

export const PastReservationPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, isLoading } = useAuth();

  // 코치 전용 가드
  useEffect(() => {
    if (!isLoading && user && user.role !== "coach") {
      navigate(ROUTES.COACH_ONLY, { replace: true });
    }
  }, [isLoading, user, navigate]);
  const [activeTab, setActiveTab] = useState<"current" | "past">("past");
  const [filterValue, setFilterValue] = useState("전체");
  const [showMemberInfoModal, setShowMemberInfoModal] = useState(false);
  const [selectedMemberName, setSelectedMemberName] = useState<string>("");
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null);
  const [showConsultationSummaryModal, setShowConsultationSummaryModal] =
    useState(false);
  const [selectedReservationId, setSelectedReservationId] = useState<
    number | null
  >(null);
  const [selectedReservationData, setSelectedReservationData] = useState<{
    startAt: string;
    endAt: string;
  } | null>(null);
  const [showApplicationMemberInfoModal, setShowApplicationMemberInfoModal] =
    useState(false);

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
    ? `${nickname} 코치님의\n지난 상담 예약 목록입니다.`
    : "코치님의\n지난 상담 예약 목록입니다.";

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

  // 상담 요약본용 날짜/시간 포맷팅
  const formatSummaryDateTime = (
    startAt: string,
    endAt: string
  ): { date: string; startTime: string; endTime: string } => {
    try {
      const start = new Date(startAt);
      const end = new Date(endAt);

      const year = start.getFullYear();
      const month = String(start.getMonth() + 1).padStart(2, "0");
      const date = String(start.getDate()).padStart(2, "0");

      const startHour = start.getHours();
      const startMinute = String(start.getMinutes()).padStart(2, "0");
      const endHour = end.getHours();
      const endMinute = String(end.getMinutes()).padStart(2, "0");

      const startPeriod = startHour < 12 ? "AM" : "PM";
      const endPeriod = endHour < 12 ? "AM" : "PM";
      const displayStartHour =
        startHour > 12 ? startHour - 12 : startHour === 0 ? 12 : startHour;
      const displayEndHour =
        endHour > 12 ? endHour - 12 : endHour === 0 ? 12 : endHour;

      return {
        date: `${year}-${month}-${date}`,
        startTime: `${displayStartHour}:${startMinute} ${startPeriod}`,
        endTime: `${displayEndHour}:${endMinute} ${endPeriod}`,
      };
    } catch (e) {
      return { date: startAt, startTime: "", endTime: "" };
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
        "past",
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
    navigate(ROUTES.RESERVATION_LIST);
  };

  const handlePastClick = () => {
    setActiveTab("past");
  };

  const handleFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFilterValue(e.target.value);
    setCurrentPage(0); // 필터 변경 시 첫 페이지로
  };

  const handleViewMember = (memberName: string, memberId?: string) => {
    setSelectedMemberName(memberName);
    setSelectedMemberId(memberId || null);
    setShowMemberInfoModal(true);
  };

  const handleViewConsultationSummary = (reservation: CoachConsultation) => {
    setSelectedReservationId(reservation.consultationId);
    const firstParticipant =
      reservation.participants && reservation.participants.length > 0
        ? reservation.participants[0].nickname
        : "";
    setSelectedMemberName(firstParticipant);
    setSelectedReservationData({
      startAt: reservation.startAt,
      endAt: reservation.endAt,
    });
    setShowConsultationSummaryModal(true);
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
      setShowApplicationMemberInfoModal(true);
    }
  };

  const filterOptions = [
    { value: "전체", label: "전체" },
    { value: "기업 클래스", label: "기업 클래스" },
    { value: "일반 클래스", label: "일반 클래스" },
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

        <Divider />

        {loading ? (
          <LoadingMessage>예약 목록을 불러오는 중...</LoadingMessage>
        ) : error ? (
          <ErrorMessage>{error}</ErrorMessage>
        ) : reservations.length === 0 ? (
          <EmptyMessage>지난 상담 예약 내역이 없습니다.</EmptyMessage>
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
                          {isIndividual && firstParticipant ? (
                            <>
                              <MemberInfoButton
                                onClick={() =>
                                  handleViewMember(
                                    firstParticipant.nickname,
                                    firstParticipant.userId
                                  )
                                }
                              >
                                회원 정보
                              </MemberInfoButton>
                              <ConsultationSummaryButton
                                onClick={() =>
                                  handleViewConsultationSummary(reservation)
                                }
                              >
                                상담 요약본
                              </ConsultationSummaryButton>
                            </>
                          ) : (
                            <ViewMemberButton
                              onClick={() =>
                                handleViewAppliedMembers(
                                  reservation.participants
                                )
                              }
                            >
                              신청 회원
                            </ViewMemberButton>
                          )}
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
          }}
          memberName={selectedMemberName}
          memberId={selectedMemberId || undefined}
        />

        {/* 상담 요약본 모달 */}
        <ConsultationSummaryModal
          open={showConsultationSummaryModal}
          onClose={() => {
            setShowConsultationSummaryModal(false);
            setSelectedReservationId(null);
            setSelectedReservationData(null);
          }}
          memberName={selectedMemberName}
          reservationId={selectedReservationId || 0}
          date={
            selectedReservationData
              ? formatSummaryDateTime(
                  selectedReservationData.startAt,
                  selectedReservationData.endAt
                ).date
              : ""
          }
          startTime={
            selectedReservationData
              ? formatSummaryDateTime(
                  selectedReservationData.startAt,
                  selectedReservationData.endAt
                ).startTime
              : ""
          }
          endTime={
            selectedReservationData
              ? formatSummaryDateTime(
                  selectedReservationData.startAt,
                  selectedReservationData.endAt
                ).endTime
              : ""
          }
        />

        {/* 신청 회원 정보 모달 */}
        <ApplicationMemberInfoModal
          open={showApplicationMemberInfoModal}
          onClose={() => setShowApplicationMemberInfoModal(false)}
          members={reservations
            .filter((r) => r.participants && r.participants.length > 0)
            .flatMap((r) => r.participants || [])
            .map((p) => ({ id: p.userId, name: p.nickname }))}
        />
      </ContentWrapper>
    </PageContainer>
  );
};

export default PastReservationPage;
