import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import { SegmentedTabs, Button } from "../../components/common/Button";
import { Dropdown } from "../../components/common/Dropdown";
import {
  ClassEditModal,
  DeleteConfirmModal,
  ConfirmModal,
} from "../../components/common/Modal";
import { useAuth } from "../../hooks/useAuth";
import { ROUTES } from "../../constants/routes";
import {
  getMyGroupConsultationsApi,
  deleteGroupConsultationApi,
  updateGroupConsultationApi,
  getGroupConsultationDetailApi,
  GroupConsultationListItem,
} from "../../api/classApi";

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

const ClassTable = styled.table`
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

  &:nth-child(1) {
    width: 60px;
  }
  &:nth-child(2) {
    width: auto;
    white-space: nowrap;
    word-break: keep-all;
  }
  &:nth-child(3) {
    width: 150px;
    text-align: center;
  }
  &:nth-child(4) {
    width: 200px;
    text-align: right;
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
  color: #111827;

  &:nth-child(2) {
    white-space: nowrap;
    word-break: keep-all;
  }
  &:nth-child(3) {
    text-align: center;
    color: #6b7280;
  }
  &:nth-child(4) {
    text-align: right;
  }
`;

const DateTimeContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.4;
`;

const DateText = styled.div`
  font-weight: 500;
  color: #111827;
`;

const TimeText = styled.div`
  font-size: 13px;
  color: #6b7280;
`;

const ActionButtonContainer = styled.div`
  display: flex;
  gap: 8px;
  justify-content: flex-end;
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
  padding: 40px;
  color: #6b7280;
  font-size: 16px;
`;

const ErrorMessage = styled.div`
  text-align: center;
  padding: 40px;
  color: #ef4444;
  font-size: 16px;
`;

const EmptyMessage = styled.div`
  text-align: center;
  padding: 40px;
  color: #6b7280;
  font-size: 16px;
`;

// 날짜 포맷팅 함수
const formatDate = (dateString: string): string => {
  try {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  } catch (e) {
    return dateString;
  }
};

// 시간 포맷팅 함수
const formatTime = (startAt: string, endAt: string): string => {
  try {
    const startDate = new Date(startAt);
    const endDate = new Date(endAt);
    
    const startHours = String(startDate.getHours()).padStart(2, "0");
    const startMinutes = String(startDate.getMinutes()).padStart(2, "0");
    const endHours = String(endDate.getHours()).padStart(2, "0");
    const endMinutes = String(endDate.getMinutes()).padStart(2, "0");
    
    return `${startHours}:${startMinutes} ~ ${endHours}:${endMinutes}`;
  } catch (e) {
    return "";
  }
};

// 에러 메시지 상수
const ERROR_MESSAGES = {
  FETCH_FAILED: "클래스 목록을 불러오는데 실패했습니다.",
  DELETE_FAILED: "클래스 삭제에 실패했습니다.",
} as const;

export const ClassListPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, isLoading } = useAuth();

  // 코치 전용 가드
  useEffect(() => {
    if (!isLoading && user && user.role !== "coach") {
      navigate(ROUTES.COACH_ONLY, { replace: true });
    }
  }, [isLoading, user, navigate]);
  const [activeTab, setActiveTab] = useState<"list" | "setup">("list");
  const [filterValue, setFilterValue] = useState("전체");
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteConfirmModal, setShowDeleteConfirmModal] = useState(false);
  const [showSaveConfirmModal, setShowSaveConfirmModal] = useState(false);
  const [showDeleteSuccessModal, setShowDeleteSuccessModal] = useState(false);
  const [editingClass, setEditingClass] =
    useState<GroupConsultationListItem | null>(null);
  const [editingClassDescription, setEditingClassDescription] = useState<string>("");
  const [deletingClassId, setDeletingClassId] = useState<number | null>(null);

  // API 관련 상태
  const [classes, setClasses] = useState<GroupConsultationListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const pageSize = 10;

  const nickname = user?.nickname || "";
  const pageTitle = nickname
    ? `${nickname} 코치님의 클래스`
    : "코치님의 클래스";

  // 클래스 목록 가져오기
  const fetchClasses = async (page: number = 0) => {
    try {
      setLoading(true);
      setError(null);
      const response = await getMyGroupConsultationsApi(page, pageSize);

      if (response.isSuccess && response.result) {
        setClasses(response.result.items);
        setCurrentPage(response.result.page);
        setTotalPages(response.result.totalPages);
      } else {
        setError(response.message || ERROR_MESSAGES.FETCH_FAILED);
      }
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : ERROR_MESSAGES.FETCH_FAILED;
      setError(errorMessage);
      console.error("클래스 목록 조회 오류:", err);
    } finally {
      setLoading(false);
    }
  };

  // 컴포넌트 마운트 시 데이터 가져오기
  useEffect(() => {
    fetchClasses(0);
  }, []);

  // 페이지 변경 시 데이터 가져오기
  const handlePageChange = (page: number) => {
    if (page >= 0 && page < totalPages) {
      fetchClasses(page);
    }
  };

  // 모달 닫기 핸들러
  const closeDeleteModal = () => {
    setShowDeleteConfirmModal(false);
    setDeletingClassId(null);
  };

  const handleListClick = () => setActiveTab("list");

  const handleSetupClick = () => {
    setActiveTab("setup");
    navigate(ROUTES.CLASS_SETUP);
  };

  const handleFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFilterValue(e.target.value);
  };

  const handleEditClick = async (classItem: GroupConsultationListItem) => {
    setEditingClass(classItem);
    setShowEditModal(true);
    
    // 클래스 상세 정보를 가져와서 description 등 추가 정보 로드
    try {
      const detailResponse = await getGroupConsultationDetailApi(classItem.id);
      if (detailResponse.isSuccess && detailResponse.result) {
        setEditingClassDescription(detailResponse.result.description || "");
      }
    } catch (err) {
      console.error("클래스 상세 정보 조회 오류:", err);
      setEditingClassDescription("");
    }
  };

  const handleDeleteClick = (classId: number) => {
    setDeletingClassId(classId);
    setShowDeleteConfirmModal(true);
  };

  const handleSave = async (data: {
    name: string;
    description: string;
    targetMember: string;
    dateTime: string;
    file?: string;
  }) => {
    if (!editingClass) return;

    try {
      // dateTime 문자열을 파싱하여 startAt과 endAt 추출
      // 형식: "YYYY-MM-DD HH:mm ~ HH:mm" 또는 "YYYY년 MM월 DD일 오전/오후 HH:mm ~ 오전/오후 HH:mm"
      let startAt = editingClass.startAt;
      let endAt = editingClass.endAt;

      // dateTime이 있으면 파싱 시도
      if (data.dateTime) {
        // ISO 형식이 아닌 경우 파싱 로직 추가 필요
        // 일단 기존 날짜를 유지하고 시간만 업데이트하는 방식으로 처리
        // 실제 구현에서는 dateTime 파싱 로직이 필요할 수 있음
      }

      // API 호출을 위한 데이터 준비
      const updateData: {
        title?: string;
        description?: string;
        startAt?: string;
        endAt?: string;
        imageUrl?: string;
      } = {};

      if (data.name && data.name !== editingClass.title) {
        updateData.title = data.name;
      }
      if (data.description) {
        updateData.description = data.description;
      }
      if (data.file && data.file !== editingClass.imageUrl) {
        updateData.imageUrl = data.file;
      }
      if (startAt !== editingClass.startAt) {
        updateData.startAt = startAt;
      }
      if (endAt !== editingClass.endAt) {
        updateData.endAt = endAt;
      }

      // API 호출
      const response = await updateGroupConsultationApi(
        editingClass.id,
        updateData
      );

      if (response.isSuccess) {
        setShowEditModal(false);
        setShowSaveConfirmModal(true);
        
        // 목록 새로고침
        await fetchClasses(currentPage);
        
        // 수정된 클래스의 최신 정보를 가져와서 editingClass 업데이트
        try {
          const detailResponse = await getGroupConsultationDetailApi(editingClass.id);
          if (detailResponse.isSuccess && detailResponse.result) {
            const updatedClass: GroupConsultationListItem = {
              id: detailResponse.result.id,
              title: detailResponse.result.title,
              imageUrl: detailResponse.result.imageUrl,
              startAt: detailResponse.result.startAt,
              endAt: detailResponse.result.endAt,
              capacity: detailResponse.result.capacity,
              currentParticipants: detailResponse.result.currentParticipants,
              availableSeats: detailResponse.result.availableSeats,
              isFull: detailResponse.result.isFull,
              coachName: detailResponse.result.coach.nickname,
              coachProfileImage: detailResponse.result.coach.profileImage,
            };
            setEditingClass(updatedClass);
            setEditingClassDescription(detailResponse.result.description || "");
          }
        } catch (err) {
          console.error("클래스 상세 정보 조회 오류:", err);
          // 상세 정보 조회 실패해도 목록에서 최신 정보를 가져올 수 있음
          const updatedClass = classes.find(c => c.id === editingClass.id);
          if (updatedClass) {
            setEditingClass(updatedClass);
          }
        }
      } else {
        setError(response.message || "클래스 수정에 실패했습니다.");
        setShowEditModal(false);
      }
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "클래스 수정에 실패했습니다.";
      setError(errorMessage);
      setShowEditModal(false);
      console.error("클래스 수정 오류:", err);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingClassId) return;

    try {
      const response = await deleteGroupConsultationApi(deletingClassId);

      if (response.isSuccess) {
        setShowDeleteConfirmModal(false);
        setShowDeleteSuccessModal(true);
        setDeletingClassId(null);
        // 삭제 후 목록 새로고침
        fetchClasses(currentPage);
      } else {
        setError(response.message || ERROR_MESSAGES.DELETE_FAILED);
        setShowDeleteConfirmModal(false);
        setDeletingClassId(null);
      }
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : ERROR_MESSAGES.DELETE_FAILED;
      setError(errorMessage);
      setShowDeleteConfirmModal(false);
      setDeletingClassId(null);
      console.error("클래스 삭제 오류:", err);
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
        <PageTitle>{pageTitle}</PageTitle>

        <TabsAndFilterContainer>
          <TabsWrapper>
            <SegmentedTabs
              leftLabel="클래스 목록"
              rightLabel="클래스 개설"
              active={activeTab === "list" ? "left" : "right"}
              onLeftClick={handleListClick}
              onRightClick={handleSetupClick}
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
          <LoadingMessage>클래스 목록을 불러오는 중...</LoadingMessage>
        ) : error ? (
          <ErrorMessage>{error}</ErrorMessage>
        ) : classes.length === 0 ? (
          <EmptyMessage>등록된 클래스가 없습니다.</EmptyMessage>
        ) : (
          <>
            <ClassTable>
              <TableHeader>
                <tr>
                  <TableHeaderCell>번호</TableHeaderCell>
                  <TableHeaderCell>클래스명</TableHeaderCell>
                  <TableHeaderCell>진행 일시</TableHeaderCell>
                  <TableHeaderCell></TableHeaderCell>
                </tr>
              </TableHeader>
              <TableBody>
                {classes.map((classItem, index) => (
                  <TableRow key={classItem.id}>
                    <TableCell>{index + 1}</TableCell>
                    <TableCell>{classItem.title}</TableCell>
                    <TableCell>
                      <DateTimeContainer>
                        <DateText>{formatDate(classItem.startAt)}</DateText>
                        <TimeText>{formatTime(classItem.startAt, classItem.endAt)}</TimeText>
                      </DateTimeContainer>
                    </TableCell>
                    <TableCell>
                      <ActionButtonContainer>
                        <Button
                          variant="info-edit"
                          onClick={() => handleEditClick(classItem)}
                        >
                          정보 및 수정
                        </Button>
                        <Button
                          variant="delete"
                          onClick={() => handleDeleteClick(classItem.id)}
                        >
                          클래스 삭제
                        </Button>
                      </ActionButtonContainer>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </ClassTable>

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
      </ContentWrapper>

      {/* 클래스 정보 수정 모달 */}
      {editingClass && (
        <ClassEditModal
          open={showEditModal}
          onClose={() => {
            setShowEditModal(false);
            setEditingClassDescription("");
          }}
          classNameData={{
            name: editingClass.title,
            description: editingClassDescription,
            targetMember: "",
            dateTime: `${formatDate(editingClass.startAt)} ${formatTime(editingClass.startAt, editingClass.endAt)}`,
            file: editingClass.imageUrl || "",
          }}
          onSave={handleSave}
        />
      )}

      {/* 저장 확인 모달 */}
      <ConfirmModal
        open={showSaveConfirmModal}
        onClose={() => setShowSaveConfirmModal(false)}
        message="클래스의 정보가 수정되었습니다."
      />

      {/* 삭제 확인 모달 */}
      <DeleteConfirmModal
        open={showDeleteConfirmModal}
        onClose={closeDeleteModal}
        onConfirm={handleDeleteConfirm}
      />

      {/* 삭제 완료 모달 */}
      <ConfirmModal
        open={showDeleteSuccessModal}
        onClose={() => setShowDeleteSuccessModal(false)}
        message="클래스가 삭제되었습니다."
      />
    </PageContainer>
  );
};

export default ClassListPage;
