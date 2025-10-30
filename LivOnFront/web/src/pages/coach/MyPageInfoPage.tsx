import React, { useState } from 'react';
import styled from 'styled-components';
import { useAuth } from '../../hooks/useAuth';
import { Link, useLocation } from 'react-router-dom';

const PageContainer = styled.div`
  width: 100%;
  max-width: 1080px;
  margin: 0 auto;
  padding: 30px 20px 60px;
  box-sizing: border-box;
`;

const Title = styled.h1`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 700;
  font-size: 40px;
  color: #000;
  margin: 0 0 16px 0;
`;

const Tabs = styled.div`
  display: flex;
  gap: 0;
  align-items: center;
  margin-bottom: 0;
  
  a, button {
    appearance: none;
    border: 1px solid #4965f6;
    padding: 8px 14px;
    font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    font-size: 14px;
    cursor: pointer;
  }

  .active {
    background: #4965f6;
    color: #fff;
  }

  .inactive {
    background: #fff;
    color: #4965f6;
  }
`;

const Underline = styled.div`
  height: 0px;
  border-bottom: 2px solid #4965f6;
  width: 100%;
  margin: -1px 0 24px; /* 겹치게 */
`;

const Field = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
`;

const Label = styled.div`
  width: 100px;
  flex-shrink: 0;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 14px;
  color: #000;
`;

const Input = styled.input`
  flex: 1;
  height: 40px;
  border: 1px solid #ecedec;
  border-radius: 12px;
  padding: 0 12px;
  font-size: 13px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const TextArea = styled.textarea`
  width: 100%;
  height: 80px;
  border: 1px solid #ecedec;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 13px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const SubmitButton = styled.button`
  width: 100%;
  height: 40px;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
  background-color: #2d79f3;
  color: white;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const ModalOverlay = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
`;

const ModalCard = styled.div`
  width: 560px;
  max-width: 90vw;
  background: #fff;
  border-radius: 12px;
  padding: 24px 20px;
  text-align: center;
`;

export const MyPageInfoPage: React.FC = () => {
  const { user } = useAuth();
  const location = useLocation();
  const [showModal, setShowModal] = useState(false);

  const nickname = user?.nickname || '코치님';

  return (
    <PageContainer>
      <Title>{nickname} 코치님 마이페이지</Title>
      <Tabs>
        <Link to="/coach/mypage/info" className={location.pathname.includes('/coach/mypage/info') ? 'active' : 'inactive'}>코치님 정보</Link>
        <Link to="/mypage/coach-verification" className={!location.pathname.includes('/coach/mypage/info') ? 'active' : 'inactive'}>코치 인증 여부</Link>
      </Tabs>
      <Underline />

      {/* 간단 폼 - 회원가입 구성에서 필수만 노출 (이메일 인증 요소 제외) */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
        <div>
          <Field>
            <Label>아이디</Label>
            <Input placeholder="아이디를 입력하세요" />
          </Field>
          <Field>
            <Label>이름</Label>
            <Input placeholder="이름을 입력해주세요." />
          </Field>
          <Field>
            <Label>닉네임</Label>
            <Input placeholder="닉네임을 입력하세요" />
          </Field>
          <Field>
            <Label>비밀번호</Label>
            <Input type="password" placeholder="비밀번호를 입력해주세요." />
          </Field>
          <Field>
            <Label>비밀번호 확인</Label>
            <Input type="password" placeholder="비밀번호를 한 번 더 입력해주세요." />
          </Field>
          <Field>
            <Label>성별</Label>
            <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
              <label><input type="radio" name="gender" defaultChecked /> 남</label>
              <label><input type="radio" name="gender" /> 여</label>
            </div>
          </Field>
          <Field>
            <Label>생년월일</Label>
            <Input placeholder="YYYY.MM.DD" />
          </Field>
        </div>
        <div>
          <Field>
            <Label>연락처</Label>
            <Input placeholder="연락처를 입력해주세요." />
          </Field>
          <Field>
            <Label>이메일</Label>
            <Input placeholder="이메일을 입력하세요." />
          </Field>
          <Field>
            <Label>소속</Label>
            <Input placeholder="소속을 입력해 주세요." />
          </Field>
          <Field>
            <Label>직무</Label>
            <Input placeholder="코칭할 분야를 선택해 주세요." />
          </Field>
        </div>
      </div>

      <div style={{ marginTop: '18px' }}>
        <Label>자격</Label>
        <Input placeholder="자격증 명을 입력해 주세요." />
      </div>

      <div style={{ marginTop: '18px' }}>
        <Label>소개</Label>
        <TextArea placeholder="소개 글을 입력해 주세요." />
      </div>

      {/* 예약 받지 않는 날 */}
      <div style={{ marginTop: '18px' }}>
        <Label>예약 받지 않는 날</Label>
        <select style={{ width: '100%', height: '40px', borderRadius: '12px', border: '1px solid #ecedec', padding: '0 12px' }}>
          <option value="" disabled selected>클릭하여 선택</option>
          <option>매주 월요일</option>
          <option>매주 화요일</option>
          <option>매주 수요일</option>
          <option>매주 목요일</option>
          <option>매주 금요일</option>
          <option>매주 토요일</option>
          <option>매주 일요일</option>
        </select>
      </div>

      <div style={{ marginTop: '24px' }}>
        <SubmitButton onClick={() => setShowModal(true)}>정보 수정</SubmitButton>
      </div>

      {showModal && (
        <ModalOverlay onClick={() => setShowModal(false)}>
          <ModalCard onClick={(e) => e.stopPropagation()}>
            <div style={{ fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', fontWeight: 800, fontSize: '22px', marginBottom: '18px' }}>
              코치님의 정보가<br />수정되었습니다.
            </div>
            <SubmitButton style={{ width: '90%', margin: '0 auto' }} onClick={() => setShowModal(false)}>확인</SubmitButton>
          </ModalCard>
        </ModalOverlay>
      )}
    </PageContainer>
  );
};

export default MyPageInfoPage;