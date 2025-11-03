import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { SegmentedTabs, Button } from '../../components/common/Button';
import { Dropdown } from '../../components/common/Dropdown';
import { ClassEditModal, DeleteConfirmModal, ConfirmModal } from '../../components/common/Modal';
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

export const ClassListPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'list' | 'setup'>('list');
  const [filterValue, setFilterValue] = useState('전체');
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteConfirmModal, setShowDeleteConfirmModal] = useState(false);
  const [showSaveConfirmModal, setShowSaveConfirmModal] = useState(false);
  const [showDeleteSuccessModal, setShowDeleteSuccessModal] = useState(false);
  const [editingClass, setEditingClass] = useState<{ id: number; name: string; date: string } | null>(null);
  const [deletingClassId, setDeletingClassId] = useState<number | null>(null);

  const nickname = user?.nickname || '';
  const pageTitle = nickname ? `${nickname} 코치님의 클래스` : '코치님의 클래스';

  const handleListClick = () => {
    setActiveTab('list');
  };

  const handleSetupClick = () => {
    setActiveTab('setup');
    navigate(ROUTES.CLASS_SETUP);
  };

  const handleFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFilterValue(e.target.value);
  };

  const handleEditClick = (classItem: { id: number; name: string; date: string }) => {
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
  };

  const handleDeleteConfirm = () => {
    // 삭제 로직 (추후 API 연동)
    setShowDeleteConfirmModal(false);
    setShowDeleteSuccessModal(true);
    setDeletingClassId(null);
  };

  // 더미 데이터
  const classes = [
    { id: 1, name: '하체 운동 클래스', date: '2025-10-13' },
    { id: 2, name: '스트레칭 클래스', date: '2025-10-12' },
    { id: 3, name: '상체 운동 클래스', date: '2025-10-11' },
    { id: 4, name: '유산소 운동 클래스', date: '2025-10-10' },
  ];

  const filterOptions = [
    { value: '전체', label: '전체' },
    { value: '기업 클래스', label: '기업 클래스' },
    { value: '일반 클래스', label: '일반 클래스' },
    { value: '개인 상담 / 코칭', label: '개인 상담 / 코칭' },
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
              active={activeTab === 'list' ? 'left' : 'right'}
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
              style={{ width: '100%' }}
            />
          </FilterDropdown>
        </TabsAndFilterContainer>

        <Divider />

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
                <TableCell>{index + 1}</TableCell>
                <TableCell>{classItem.name}</TableCell>
                <TableCell>{classItem.date} 개설</TableCell>
                <TableCell>
                  <ActionButtonContainer>
                    <Button variant="info-edit" onClick={() => handleEditClick(classItem)}>
                      정보 및 수정
                    </Button>
                    <Button variant="delete" onClick={() => handleDeleteClick(classItem.id)}>
                      클래스 삭제
                    </Button>
                  </ActionButtonContainer>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </ClassTable>

        <PaginationContainer>
          <PaginationButton disabled>‹</PaginationButton>
          {[1, 2, 3, 4, 5, 6, 7, 8].map((page) => (
            <PaginationButton key={page} $active={page === 1}>
              {page}
            </PaginationButton>
          ))}
          <PaginationButton>›</PaginationButton>
        </PaginationContainer>
      </ContentWrapper>

      {/* 클래스 정보 수정 모달 */}
      <ClassEditModal
        open={showEditModal}
        onClose={() => setShowEditModal(false)}
        classNameData={editingClass ? {
          name: editingClass.name,
          description: '하체의 모든 근육들을 골고루 발달시키도록 체계적으로 코칭해 드리겠습니다.',
          targetMember: '',
          dateTime: '9월 18일 오후 1시',
          file: ''
        } : undefined}
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
