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
  getGroupConsultationsApi,
  deleteGroupConsultationApi,
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
  }
  &:nth-child(3) {
    width: 150px;
    text-align: right;
  }
  &:nth-child(4) {
    width: 200px;
    text-align: right;
  }
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
  color: #111827;

  &:nth-child(3) {
    text-align: right;
    color: #6b7280;
  }
  &:nth-child(4) {
    text-align: right;
  }
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
  const [deletingClassId, setDeletingClassId] = useState<number | null>(null);

  // API 관련 상태
  const [classes, setClasses] = useState<GroupConsultationListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const pageSize = 10;

  const nickname = user?.nickname || "";
  const pageTitle = nickname
    ? `${nickname} 코치님의 클래스`
    : "코치님의 클래스";

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

  // 클래스 목록 가져오기
  const fetchClasses = async (page: number = 0) => {
    try {
      setLoading(true);
      setError(null);
      const response = await getGroupConsultationsApi(false, page, pageSize);

      if (response.isSuccess && response.result) {
        setClasses(response.result.items);
        setCurrentPage(response.result.page);
        setTotalPages(response.result.totalPages);
        setTotalItems(response.result.totalItems);
      } else {
        setError(response.message || "클래스 목록을 불러오는데 실패했습니다.");
      }
    } catch (err) {
      const errorMessage =
        err instanceof Error
          ? err.message
          : "클래스 목록을 불러오는데 실패했습니다.";
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

  const handleListClick = () => {
    setActiveTab("list");
  };

  const handleSetupClick = () => {
    setActiveTab("setup");
    navigate(ROUTES.CLASS_SETUP);
  };

  const handleFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFilterValue(e.target.value);
  };

  const handleEditClick = (classItem: GroupConsultationListItem) => {
    setEditingClass(classItem);
    setShowEditModal(true);
  };

  const handleDeleteClick = (classId: number) => {
    setDeletingClassId(classId);
    setShowDeleteConfirmModal(true);
  };

  const handleSave = (data: {
    name: string;
    description: string;
    targetMember: string;
    dateTime: string;
    file?: string;
  }) => {
    // 저장 로직 (추후 API 연동)
    setShowEditModal(false);
    setShowSaveConfirmModal(true);
    // 저장 후 목록 새로고침
    fetchClasses(currentPage);
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
        setError(response.message || "클래스 삭제에 실패했습니다.");
        setShowDeleteConfirmModal(false);
        setDeletingClassId(null);
      }
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "클래스 삭제에 실패했습니다.";
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

        <Divider />

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
                  <TableHeaderCell>개설일</TableHeaderCell>
                  <TableHeaderCell></TableHeaderCell>
                </tr>
              </TableHeader>
              <TableBody>
                {classes.map((classItem, index) => (
                  <TableRow key={classItem.id}>
                    <TableCell>{currentPage * pageSize + index + 1}</TableCell>
                    <TableCell>{classItem.title}</TableCell>
                    <TableCell>{formatDate(classItem.startAt)} 개설</TableCell>
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
      <ClassEditModal
        open={showEditModal}
        onClose={() => setShowEditModal(false)}
        classNameData={
          editingClass
            ? {
                name: editingClass.title,
                description: "",
                targetMember: "",
                dateTime: formatDate(editingClass.startAt),
                file: editingClass.imageUrl || "",
              }
            : undefined
        }
        onSave={handleSave}
      />

      {/* 저장 확인 모달 */}
      <ConfirmModal
        open={showSaveConfirmModal}
        onClose={() => setShowSaveConfirmModal(false)}
        message="클래스의 정보가 수정되었습니다."
      />

      {/* 삭제 확인 모달 */}
      <DeleteConfirmModal
        open={showDeleteConfirmModal}
        onClose={() => {
          setShowDeleteConfirmModal(false);
          setDeletingClassId(null);
        }}
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
