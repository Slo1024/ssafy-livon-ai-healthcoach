import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { SegmentedTabs } from '../../components/common/Button';
import { Dropdown } from '../../components/common/Dropdown';
import { MemberInfoModal, ApplicationApprovalModal, ReservationCancelConfirmModal, ReservationCancelSuccessModal } from '../../components/common/Modal';
import { useAuth } from '../../hooks/useAuth';
import { ROUTES } from '../../constants/routes';

const PageContainer = styled.div`
  min-height: 100vh;
  background-color: #ffffff;
  padding: 40px 20px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 500;
  font-size: 28px;
  color: #000000;
  line-height: 1.5;
  white-space: pre-line;
  text-align: center;
  max-width: 90%;

  @media (max-width: 1200px) {
    position: static;
    transform: none;
    align-self: center;
    text-align: center;
    max-width: 100%;
    font-size: 24px;
  }

  @media (max-width: 900px) {
    font-size: 22px;
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
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  white-space: nowrap;
  
  &:hover {
    background-color: #f7fafc;
  }
`;

const ViewMemberButton = styled.button<{ $compact?: boolean }>`
  width: ${props => (props.$compact ? '85px' : '86px')};
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background-color: #ffffff;
  color: #4965f6;
  border: 1px solid #4965f6;
  border-radius: 8px;
  font-size: ${props => (props.$compact ? '14px' : '12px')};
  font-weight: 500;
  cursor: pointer;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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
  background-color: ${props => props.$active ? '#4965f6' : '#ffffff'};
  color: ${props => props.$active ? '#ffffff' : '#6b7280'};
  font-size: 14px;
  font-weight: ${props => props.$active ? '600' : '400'};
  cursor: pointer;
  
  &:hover {
    background-color: ${props => props.$active ? '#3b5dd8' : '#f9fafb'};
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;

interface Reservation {
  id: number;
  date: string;
  time: string;
  classTitle: string;
  classDescription: string;
  classType: string;
  status?: string;
  memberName?: string;
}

export const ReservationListPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'current' | 'past'>('current');
  const [filterValue, setFilterValue] = useState('전체');
  const [showMemberInfoModal, setShowMemberInfoModal] = useState(false);
  const [selectedMemberName, setSelectedMemberName] = useState<string>('');
  const [showApplicationApprovalModal, setShowApplicationApprovalModal] = useState(false);
  const [showCancelConfirmModal, setShowCancelConfirmModal] = useState(false);
  const [showCancelSuccessModal, setShowCancelSuccessModal] = useState(false);
  const [cancelReservationId, setCancelReservationId] = useState<number | null>(null);

  const nickname = user?.nickname || '';
  const scheduleMessage = nickname ? `${nickname} 코치님의\n다가오는 상담/코칭 스케줄입니다.` : '코치님의\n다가오는 상담/코칭 스케줄입니다.';

  const handleCurrentClick = () => {
    setActiveTab('current');
  };

  const handlePastClick = () => {
    setActiveTab('past');
    navigate(ROUTES.PAST_RESERVATION);
  };

  const handleFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFilterValue(e.target.value);
  };

  const handleStartConsultation = () => {
    navigate(ROUTES.STREAMING);
  };

  const handleViewMember = (memberName: string) => {
    setSelectedMemberName(memberName);
    setShowMemberInfoModal(true);
  };

  const handleViewAppliedMembers = () => {
    setShowApplicationApprovalModal(true);
  };

  const handleCancelClick = (reservationId: number) => {
    setCancelReservationId(reservationId);
    setShowCancelConfirmModal(true);
  };

  const handleCancelConfirm = () => {
    if (cancelReservationId !== null) {
      setAllReservations(prev => prev.filter(reservation => reservation.id !== cancelReservationId));
      setCancelReservationId(null);
      setShowCancelSuccessModal(true);
    }
  };

  const handleCancelSuccess = () => {
    setShowCancelSuccessModal(false);
  };

  // 더미 데이터 - 전체
  const [allReservations, setAllReservations] = useState<Reservation[]>([
    { id: 1, date: '10.13(월)', time: '오전 10:00 ~ 11:00', classTitle: '체형 교정', classDescription: '자세 교정 운동 클래스', classType: '기업 클래스' },
    { id: 2, date: '10.13(월)', time: '오전 11:00 ~ 12:00', classTitle: '하체 단련', classDescription: '하체 근력 강화 클래스', classType: '기업 클래스' },
    { id: 3, date: '10.13(월)', time: '오후 1:00 ~ 2:00', classTitle: '복근 운동', classDescription: '복근 운동 클래스', classType: '일반 클래스' },
    { id: 4, date: '10.13(월)', time: '오후 2:00 ~ 3:00', classTitle: '홈트 강좌', classDescription: '홈트레이닝 튜토리얼 클래스', classType: '일반 클래스' },
    { id: 5, date: '10.13(월)', time: '오후 3:00 ~ 4:00', classTitle: '체형 교정', classDescription: '자세 교정 운동 클래스', classType: '개인 상담 / 코칭', memberName: '김싸피' },
    { id: 6, date: '10.13(월)', time: '오후 4:00 ~ 5:00', classTitle: '체형 교정', classDescription: '자세 교정 운동 클래스', classType: '개인 상담 / 코칭', memberName: '김싸피' },
  ]);

  // 필터링된 예약 목록
  const filteredReservations = filterValue === '전체'
    ? allReservations
    : allReservations.filter(reservation => reservation.classType === filterValue);

  const filterOptions = [
    { value: '전체', label: '전체' },
    { value: '기업 클래스', label: '기업 클래스' },
    { value: '일반 클래스', label: '일반 클래스' },
    { value: '개인 상담 / 코칭', label: '개인 상담 / 코칭' },
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
              active={activeTab === 'current' ? 'left' : 'right'}
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
              style={{ width: '100%' }}
            />
          </FilterDropdown>
        </TabsAndFilterContainer>

        {filteredReservations.length === 0 && activeTab === 'current' ? (
          <EmptyMessage>현재 상담 예약 신청 내역이 없습니다.</EmptyMessage>
        ) : (
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
              {filteredReservations.map((reservation) => (
                <TableRow key={reservation.id}>
                  <TableCell>{reservation.date} {reservation.time}</TableCell>
                  <TableCell>
                    <ClassTitle>{reservation.classTitle}</ClassTitle>
                    <ClassDescription>{reservation.classDescription}</ClassDescription>
                  </TableCell>
                  <TableCell style={{ color: '#4965f6' }}>{reservation.classType}</TableCell>
                  <TableCell>
                    <ActionButtonContainer>
                      <StartConsultationButton onClick={handleStartConsultation}>
                        상담 시작
                      </StartConsultationButton>
                      {reservation.classType === '개인 상담 / 코칭' && reservation.memberName ? (
                        <ViewMemberButton onClick={() => handleViewMember(reservation.memberName!)}>
                          {`${reservation.memberName} 회원 보기`}
                        </ViewMemberButton>
                      ) : null}
                      {(reservation.classType === '기업 클래스' || reservation.classType === '일반 클래스') && (
                        <ViewMemberButton $compact onClick={handleViewAppliedMembers}>
                          신청 회원
                        </ViewMemberButton>
                      )}
                      <CancelButton onClick={() => handleCancelClick(reservation.id)}>예약 취소</CancelButton>
                    </ActionButtonContainer>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </ReservationTable>
        )}

        {filteredReservations.length > 0 && (
          <PaginationContainer>
            <PaginationButton disabled>‹</PaginationButton>
            {[1, 2, 3, 4, 5, 6, 7, 8].map((page) => (
              <PaginationButton key={page} $active={page === 1}>
                {page}
              </PaginationButton>
            ))}
            <PaginationButton>›</PaginationButton>
          </PaginationContainer>
        )}

        {/* 회원 정보 모달 */}
        <MemberInfoModal
          open={showMemberInfoModal}
          onClose={() => setShowMemberInfoModal(false)}
          memberName={selectedMemberName}
          memberData={{
            height: 170,
            weight: 50,
            sleepTime: 7,
          }}
          question="전완근을 키우고 싶어요"
        />

        {/* 신청 회원 예약 승인 모달 */}
        <ApplicationApprovalModal
          open={showApplicationApprovalModal}
          onClose={() => setShowApplicationApprovalModal(false)}
          members={[
            { id: '1', name: '김싸피' },
            { id: '2', name: '이싸피' },
          ]}
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
