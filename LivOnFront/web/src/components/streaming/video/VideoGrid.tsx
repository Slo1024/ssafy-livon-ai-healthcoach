import React, { useRef, useState, useEffect } from 'react';
import styled from 'styled-components';
import { LocalVideoTrack, RemoteVideoTrack, RemoteAudioTrack, Track } from 'livekit-client';
import { VideoComponent } from './VideoComponent';
import { AudioComponent } from './AudioComponent';

const VideoGridOuterContainer = styled.div`
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
`;

const VideoGridScrollContainer = styled.div<{ $hasScroll: boolean; $participantCount: number }>`
  width: 100%;
  height: 100%;
  overflow-y: ${props => props.$hasScroll ? 'auto' : 'hidden'};
  overflow-x: hidden;
  padding-right: ${props => props.$hasScroll ? '0' : '0'};
  
  /* 스크롤바 숨기기 (크롬, 엣지, 사파리) */
  &::-webkit-scrollbar {
    display: none;
  }
  
  /* 스크롤바 숨기기 (파이어폭스) */
  scrollbar-width: none;
  
  /* 3명 이상일 때 4행 높이로 제한 */
  ${props => {
    if (props.$participantCount > 2) {
      // 4행의 높이 계산: 각 행 높이 = (100% - padding 16px) / 4
      // 4행 높이 = (100% - 16px) / 4 * 4 = 100% - 16px
      // 하지만 gap도 고려해야 함: gap 8px * 3 = 24px
      // 총 높이 = 100% - 16px (padding) + 24px (gap) = 100% + 8px
      // 실제로는 그리드가 자동으로 계산하므로, 그냥 높이 제한만 설정
      return `
        max-height: 100%;
      `;
    }
    return '';
  }}
`;

const VideoGridContainer = styled.div<{ 
  $viewMode: 'gallery' | 'speaker' | 'shared'; 
  $participantCount: number;
}>`
  display: grid;
  width: 100%;
  min-height: 100%;
  
  ${props => {
    const count = props.$participantCount;
    
    if (count === 1) {
      return `
        grid-template-columns: 1fr;
        grid-template-rows: 1fr;
        gap: 0;
        padding: 0;
        height: 100%;
      `;
    } else if (count === 2) {
      return `
        grid-template-columns: 1fr 1fr;
        grid-template-rows: 1fr;
        gap: 8px;
        padding: 8px;
        height: 100%;
      `;
    } else if (count === 3) {
      // 상단 2개, 하단 1개 (정중앙)
      return `
        grid-template-columns: 1fr 1fr;
        grid-template-rows: 1fr 1fr;
        gap: 8px;
        padding: 8px;
        height: 100%;
      `;
    } else if (count === 4) {
      // 2x2 그리드
      return `
        grid-template-columns: 1fr 1fr;
        grid-template-rows: 1fr 1fr;
        gap: 8px;
        padding: 8px;
        height: 100%;
      `;
    } else if (count === 5) {
      // 상단 3개, 하단 2개 (정중앙)
      return `
        grid-template-columns: repeat(3, 1fr);
        grid-template-rows: 1fr 1fr;
        gap: 8px;
        padding: 8px;
        height: 100%;
      `;
    } else if (count === 6) {
      // 3x2 그리드
      return `
        grid-template-columns: repeat(3, 1fr);
        grid-template-rows: 1fr 1fr;
        gap: 8px;
        padding: 8px;
        height: 100%;
      `;
    } else if (count === 7) {
      // 상단 4개, 하단 3개 (정중앙)
      return `
        grid-template-columns: repeat(4, 1fr);
        grid-template-rows: 1fr 1fr;
        gap: 8px;
        padding: 8px;
        height: 100%;
      `;
    } else if (count === 8) {
      // 4x2 그리드
      return `
        grid-template-columns: repeat(4, 1fr);
        grid-template-rows: 1fr 1fr;
        gap: 8px;
        padding: 8px;
        height: 100%;
      `;
    } else if (count === 9) {
      // 상단 5개, 하단 4개 (정중앙)
      return `
        grid-template-columns: repeat(5, 1fr);
        grid-template-rows: 1fr 1fr;
        gap: 8px;
        padding: 8px;
        height: 100%;
      `;
    } else if (count === 10) {
      // 5x2 그리드
      return `
        grid-template-columns: repeat(5, 1fr);
        grid-template-rows: 1fr 1fr;
        gap: 8px;
        padding: 8px;
        height: 100%;
      `;
    } else {
      // 11명 이상: 5열 고정 그리드
      // 행은 자동으로 생성되며, 각 행의 높이는 동일
      // 스크롤 컨테이너가 4행까지만 보여줌 (20명)
      // 20명 넘으면 스크롤 가능
      return `
        grid-template-columns: repeat(5, 1fr);
        grid-auto-rows: 1fr;
        gap: 8px;
        padding: 8px;
      `;
    }
  }}
`;

const VideoTileWrapper = styled.div<{ 
  $isMain?: boolean; 
  $fullScreen?: boolean; 
  $participantCount?: number;
  $isLastRow?: boolean;
  $lastRowItemCount?: number;
  $lastRowIndex?: number;
  $rowIndex?: number;
  $totalRows?: number;
}>`
  position: relative;
  width: 100%;
  height: 100%;
  background-color: #1f2937;
  border-radius: ${props => props.$fullScreen ? '0' : '8px'};
  overflow: hidden;
  aspect-ratio: ${props => props.$fullScreen ? 'unset' : '16/9'};
  min-height: 0;
  
  /* 10명 이하 하단 정중앙 배치 (3, 5, 7, 9명) */
  ${props => {
    if (props.$participantCount === 3 && props.$isLastRow && props.$lastRowIndex === 0) {
      // 3명: 하단 1개 정중앙
      return `
        grid-column: 1 / -1;
        justify-self: center;
        width: 50%;
        max-width: 50%;
      `;
    } else if (props.$participantCount === 5 && props.$isLastRow && props.$lastRowItemCount === 2 && props.$lastRowIndex !== undefined) {
      // 5명: 하단 2개 정중앙 (가로 한 줄로 나란히)
      // 3열 그리드에서 2개를 정중앙에 배치하려면 컬럼 1, 2를 사용
      // 하지만 더 정확한 정중앙을 위해 전체 컬럼을 차지하고 margin으로 조정
      const padding = 8;
      const gap = 8;
      const totalColumns = 3;
      const videoWidthCalc = `calc((100% - ${padding * 2}px - ${(totalColumns - 1) * gap}px) / ${totalColumns})`;
      const gapCalc = `${gap}px`;
      // 하단 그룹 전체 너비: 2개 비디오 + 1개 gap
      const totalGroupWidthCalc = `calc(${videoWidthCalc} * 2 + ${gapCalc})`;
      // 좌측 오프셋: (100% - 그룹 전체 너비) / 2
      const leftOffsetCalc = `calc((100% - ${totalGroupWidthCalc}) / 2)`;
      // 각 비디오의 위치 (가로 한 줄로 나란히)
      const videoPositionCalc = props.$lastRowIndex === 0
        ? leftOffsetCalc
        : `calc(${leftOffsetCalc} + ${videoWidthCalc} + ${gapCalc})`;
      return `
        grid-column: 1 / -1;
        grid-row: 2;
        justify-self: start;
        width: ${videoWidthCalc};
        margin-left: ${videoPositionCalc};
      `;
    } else if (props.$participantCount === 7 && props.$isLastRow && props.$lastRowItemCount === 3 && props.$lastRowIndex !== undefined) {
      // 7명: 하단 3개 정중앙 (가로 한 줄로 나란히)
      // 4열 그리드에서 3개를 정중앙에 배치
      const padding = 8;
      const gap = 8;
      const totalColumns = 4;
      const videoWidthCalc = `calc((100% - ${padding * 2}px - ${(totalColumns - 1) * gap}px) / ${totalColumns})`;
      const gapCalc = `${gap}px`;
      // 하단 그룹 전체 너비: 3개 비디오 + 2개 gap
      const totalGaps = 2 * gap;
      const totalGroupWidthCalc = `calc(${videoWidthCalc} * 3 + ${totalGaps}px)`;
      // 좌측 오프셋: (100% - 그룹 전체 너비) / 2
      const leftOffsetCalc = `calc((100% - ${totalGroupWidthCalc}) / 2)`;
      // 각 비디오의 위치 (가로 한 줄로 나란히)
      const videoPositionCalc = props.$lastRowIndex === 0
        ? leftOffsetCalc
        : `calc(${leftOffsetCalc} + ${props.$lastRowIndex} * (${videoWidthCalc} + ${gapCalc}))`;
      return `
        grid-column: 1 / -1;
        grid-row: 2;
        justify-self: start;
        width: ${videoWidthCalc};
        margin-left: ${videoPositionCalc};
      `;
    } else if (props.$participantCount === 9 && props.$isLastRow && props.$lastRowItemCount === 4 && props.$lastRowIndex !== undefined) {
      // 9명: 하단 4개 정중앙 (가로 한 줄로 나란히)
      // 5열 그리드에서 4개를 정중앙에 배치
      const padding = 8;
      const gap = 8;
      const totalColumns = 5;
      const videoWidthCalc = `calc((100% - ${padding * 2}px - ${(totalColumns - 1) * gap}px) / ${totalColumns})`;
      const gapCalc = `${gap}px`;
      // 하단 그룹 전체 너비: 4개 비디오 + 3개 gap
      const totalGaps = 3 * gap;
      const totalGroupWidthCalc = `calc(${videoWidthCalc} * 4 + ${totalGaps}px)`;
      // 좌측 오프셋: (100% - 그룹 전체 너비) / 2
      const leftOffsetCalc = `calc((100% - ${totalGroupWidthCalc}) / 2)`;
      // 각 비디오의 위치 (가로 한 줄로 나란히)
      const videoPositionCalc = props.$lastRowIndex === 0
        ? leftOffsetCalc
        : `calc(${leftOffsetCalc} + ${props.$lastRowIndex} * (${videoWidthCalc} + ${gapCalc}))`;
      return `
        grid-column: 1 / -1;
        grid-row: 2;
        justify-self: start;
        width: ${videoWidthCalc};
        margin-left: ${videoPositionCalc};
      `;
    }
    return '';
  }}
  
  /* 11명 이상 마지막 행이 5개 미만일 때 정중앙 배치 (가로 한 줄로 나란히) */
  ${props => {
    if (props.$participantCount !== undefined && props.$participantCount >= 11 && props.$isLastRow && props.$lastRowItemCount && props.$lastRowItemCount < 5 && props.$lastRowIndex !== undefined && props.$rowIndex !== undefined && props.$totalRows !== undefined) {
      const padding = 8; // px (좌우 각 8px)
      const gap = 8; // px
      const totalColumns = 5;
      const itemCount = props.$lastRowItemCount;
      
      // 각 비디오 너비: calc((100% - 16px - 32px) / 5) = calc((100% - 48px) / 5)
      // gap은 4개 (5개 컬럼 사이)
      const videoWidthCalc = `calc((100% - ${padding * 2}px - ${(totalColumns - 1) * gap}px) / ${totalColumns})`;
      const gapCalc = `${gap}px`;
      
      // 하단 그룹 전체 너비 계산
      const totalGaps = (itemCount - 1) * gap;
      const totalGroupWidthCalc = `calc(${videoWidthCalc} * ${itemCount} + ${totalGaps}px)`;
      
      // 좌측 오프셋 계산 (정중앙 배치)
      const leftOffsetCalc = `calc((100% - ${totalGroupWidthCalc}) / 2)`;
      
      // 각 비디오의 위치 계산 (가로 한 줄로 나란히)
      const videoPositionCalc = props.$lastRowIndex === 0
        ? leftOffsetCalc
        : `calc(${leftOffsetCalc} + ${props.$lastRowIndex} * (${videoWidthCalc} + ${gapCalc}))`;
      
      // 마지막 행의 행 번호 (1-based)
      const gridRowNum = props.$rowIndex + 1;
      
      return `
        grid-column: 1 / -1;
        grid-row: ${gridRowNum};
        justify-self: start;
        width: ${videoWidthCalc};
        margin-left: ${videoPositionCalc};
      `;
    }
    return '';
  }}
`;

const SharedContentPanel = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  padding: 40px;
  overflow-y: auto;
`;

const SharedContentCard = styled.div`
  background-color: #ffffff;
  border-radius: 16px;
  padding: 32px;
  max-width: 600px;
  width: 100%;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
`;

const SharedContentHeader = styled.div`
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
`;

const ProfileIcon = styled.div`
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
`;

const SharedContentInfo = styled.div`
  flex: 1;
`;

const SharedContentName = styled.h3`
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px 0;
`;

const SharedContentData = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
`;

const SharedContentAnalysis = styled.div`
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
`;

const AnalysisText = styled.p`
  font-size: 14px;
  color: #374151;
  line-height: 1.8;
  margin: 0 0 16px 0;
`;

const TipBox = styled.div`
  margin-top: 16px;
  padding: 16px;
  background-color: #fef3c7;
  border-left: 4px solid #f59e0b;
  border-radius: 4px;
`;

const TipLabel = styled.strong`
  display: block;
  color: #92400e;
  margin-bottom: 8px;
`;

const TipText = styled.p`
  margin: 0;
  color: #78350f;
  font-size: 14px;
  line-height: 1.6;
`;

const ScrollButtonContainer = styled.div`
  position: absolute;
  right: 16px;
  bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 10;
`;

const ScrollButton = styled.button`
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.6);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: rgba(0, 0, 0, 0.8);
  }
  
  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
  
  svg {
    width: 20px;
    height: 20px;
  }
`;

interface RemoteTrackInfo {
  trackPublication: any;
  participantIdentity: string;
  participant: any;
}

interface VideoGridProps {
  localTrack: LocalVideoTrack | undefined;
  remoteTracks: RemoteTrackInfo[];
  isVideoEnabled: boolean;
  isScreenSharing: boolean;
  sharedContent: {
    type: 'ai-analysis';
    memberName: string;
  } | null;
  viewMode: 'gallery' | 'speaker' | 'shared';
  participantName: string;
}

export const VideoGrid: React.FC<VideoGridProps> = ({
  localTrack,
  remoteTracks,
  isVideoEnabled,
  isScreenSharing,
  sharedContent,
  viewMode,
  participantName,
}) => {
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const [canScrollUp, setCanScrollUp] = useState(false);
  const [canScrollDown, setCanScrollDown] = useState(false);

  // 참가자 수 계산 (로컬 비디오 + 원격 비디오)
  const localVideoCount = (localTrack && isVideoEnabled) ? 1 : 0;
  const remoteVideoCount = remoteTracks.filter(item => item.trackPublication.track?.kind === Track.Kind.Video).length;
  const participantCount = localVideoCount + remoteVideoCount;

  // 20명 초과 여부 확인
  const hasMoreThan20 = participantCount > 20;

  // 스크롤 가능 여부 확인
  useEffect(() => {
    const checkScrollability = () => {
      if (scrollContainerRef.current && hasMoreThan20) {
        const container = scrollContainerRef.current;
        const canScroll = container.scrollHeight > container.clientHeight;
        setCanScrollUp(container.scrollTop > 0);
        setCanScrollDown(canScroll && container.scrollTop < container.scrollHeight - container.clientHeight - 1);
      } else {
        setCanScrollUp(false);
        setCanScrollDown(false);
      }
    };

    checkScrollability();
    const scrollContainer = scrollContainerRef.current;
    if (scrollContainer) {
      scrollContainer.addEventListener('scroll', checkScrollability);
      // ResizeObserver로 크기 변경 감지
      const resizeObserver = new ResizeObserver(checkScrollability);
      resizeObserver.observe(scrollContainer);
      
      return () => {
        scrollContainer.removeEventListener('scroll', checkScrollability);
        resizeObserver.disconnect();
      };
    }
  }, [hasMoreThan20, participantCount]);

  // 스크롤 함수
  const scrollUp = () => {
    if (scrollContainerRef.current) {
      const rowHeight = scrollContainerRef.current.clientHeight / 4; // 4행 기준
      scrollContainerRef.current.scrollBy({ top: -rowHeight, behavior: 'smooth' });
    }
  };

  const scrollDown = () => {
    if (scrollContainerRef.current) {
      const rowHeight = scrollContainerRef.current.clientHeight / 4; // 4행 기준
      scrollContainerRef.current.scrollBy({ top: rowHeight, behavior: 'smooth' });
    }
  };

  // 모든 비디오 트랙 수집 (로컬 + 원격)
  const allVideoTracks: Array<{
    track: LocalVideoTrack | RemoteVideoTrack;
    identity: string;
    isLocal: boolean;
  }> = [];

  if (localTrack && isVideoEnabled) {
    allVideoTracks.push({
      track: localTrack,
      identity: participantName,
      isLocal: true,
    });
  }

  remoteTracks
    .filter((item) => item.trackPublication.track?.kind === Track.Kind.Video)
    .forEach((item) => {
      allVideoTracks.push({
        track: item.trackPublication.track as RemoteVideoTrack,
        identity: item.participantIdentity,
        isLocal: false,
      });
    });

  return (
    <VideoGridOuterContainer>
      <VideoGridScrollContainer 
        ref={scrollContainerRef}
        $hasScroll={hasMoreThan20}
        $participantCount={participantCount}
      >
        <VideoGridContainer 
          $viewMode={viewMode} 
          $participantCount={participantCount}
        >
      {isScreenSharing && sharedContent ? (
        <SharedContentPanel>
          <SharedContentCard>
            <SharedContentHeader>
              <ProfileIcon>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
              </ProfileIcon>
              <SharedContentInfo>
                <SharedContentName>{sharedContent.memberName}님의 AI 분석본</SharedContentName>
                <SharedContentData>
                  <div>생성일: {new Date().toLocaleDateString('ko-KR')}</div>
                  <div>분석 유형: 건강 상태 분석</div>
                </SharedContentData>
              </SharedContentInfo>
            </SharedContentHeader>
            <SharedContentAnalysis>
              <AnalysisText>
                현재 혈압 수치와 건강 상태를 종합적으로 분석한 결과, 규칙적인 운동과 건강한 식습관 유지가 필요합니다.
              </AnalysisText>
              <TipBox>
                <TipLabel>TIP</TipLabel>
                <TipText>혈압약 복용 중이므로 격렬한 운동은 피하세요.</TipText>
              </TipBox>
            </SharedContentAnalysis>
          </SharedContentCard>
        </SharedContentPanel>
      ) : (
        <>
          {/* 모든 비디오 트랙 렌더링 (5x4 그리드) */}
          {allVideoTracks.map((videoItem, index) => {
            // 원격 비디오의 경우 key 생성
            const key = videoItem.isLocal 
              ? `local-video-${participantName}`
              : `remote-video-${videoItem.identity}-${index}`;

            // 행과 열 계산
            let rowIndex: number = 0;
            let colIndex: number = 0;
            let totalRows: number = 1;
            let isLastRow: boolean = false;
            let lastRowItemCount: number = 0;
            let lastRowIndex: number | undefined = undefined;
            
            if (participantCount <= 10) {
              // 10명 이하 특별 레이아웃
              if (participantCount === 1) {
                rowIndex = 0;
                colIndex = 0;
                totalRows = 1;
                isLastRow = false;
                lastRowItemCount = 0;
                lastRowIndex = undefined;
              } else if (participantCount === 2) {
                // 2명: 가로 반반
                rowIndex = 0;
                colIndex = index;
                totalRows = 1;
                isLastRow = false;
                lastRowItemCount = 0;
                lastRowIndex = undefined;
              } else if (participantCount === 3) {
                // 3명: 상단 2개, 하단 1개
                rowIndex = Math.floor(index / 2);
                colIndex = index % 2;
                totalRows = 2;
                isLastRow = rowIndex === 1;
                lastRowItemCount = 1;
                lastRowIndex = isLastRow ? 0 : undefined;
              } else if (participantCount === 4) {
                // 4명: 2x2 그리드
                rowIndex = Math.floor(index / 2);
                colIndex = index % 2;
                totalRows = 2;
                isLastRow = false;
                lastRowItemCount = 0;
                lastRowIndex = undefined;
              } else if (participantCount === 5) {
                // 5명: 상단 3개, 하단 2개
                rowIndex = Math.floor(index / 3);
                colIndex = index % 3;
                totalRows = 2;
                isLastRow = rowIndex === 1;
                lastRowItemCount = 2;
                // 하단 행의 경우: index 3, 4 -> 하단 행 내에서의 인덱스는 0, 1
                lastRowIndex = isLastRow ? (index - 3) : undefined;
              } else if (participantCount === 6) {
                // 6명: 3x2 그리드
                rowIndex = Math.floor(index / 3);
                colIndex = index % 3;
                totalRows = 2;
                isLastRow = false;
                lastRowItemCount = 0;
                lastRowIndex = undefined;
              } else if (participantCount === 7) {
                // 7명: 상단 4개, 하단 3개
                rowIndex = Math.floor(index / 4);
                colIndex = index % 4;
                totalRows = 2;
                isLastRow = rowIndex === 1;
                lastRowItemCount = 3;
                lastRowIndex = isLastRow ? colIndex : undefined;
              } else if (participantCount === 8) {
                // 8명: 4x2 그리드
                rowIndex = Math.floor(index / 4);
                colIndex = index % 4;
                totalRows = 2;
                isLastRow = false;
                lastRowItemCount = 0;
                lastRowIndex = undefined;
              } else if (participantCount === 9) {
                // 9명: 상단 5개, 하단 4개
                rowIndex = Math.floor(index / 5);
                colIndex = index % 5;
                totalRows = 2;
                isLastRow = rowIndex === 1;
                lastRowItemCount = 4;
                lastRowIndex = isLastRow ? colIndex : undefined;
              } else if (participantCount === 10) {
                // 10명: 5x2 그리드
                rowIndex = Math.floor(index / 5);
                colIndex = index % 5;
                totalRows = 2;
                isLastRow = false;
                lastRowItemCount = 0;
                lastRowIndex = undefined;
              }
            } else {
              // 11명 이상: 5열 그리드
              rowIndex = Math.floor(index / 5);
              colIndex = index % 5;
              totalRows = Math.ceil(participantCount / 5);
              isLastRow = rowIndex === totalRows - 1;
              lastRowItemCount = participantCount % 5 === 0 ? 5 : participantCount % 5;
              lastRowIndex = (isLastRow && lastRowItemCount < 5) ? colIndex : undefined;
            }

            return (
              <VideoTileWrapper 
                key={key}
                $isMain={viewMode === 'speaker'} 
                $fullScreen={participantCount === 1}
                $participantCount={participantCount}
                $isLastRow={isLastRow && ((participantCount <= 10 && (participantCount === 3 || participantCount === 5 || participantCount === 7 || participantCount === 9)) || (participantCount >= 11 && lastRowItemCount < 5 && lastRowItemCount > 0))}
                $lastRowItemCount={isLastRow && ((participantCount <= 10 && (participantCount === 3 || participantCount === 5 || participantCount === 7 || participantCount === 9)) || (participantCount >= 11 && lastRowItemCount < 5 && lastRowItemCount > 0)) ? lastRowItemCount : undefined}
                $lastRowIndex={lastRowIndex}
                $rowIndex={rowIndex}
                $totalRows={totalRows}
              >
                <VideoComponent
                  track={videoItem.track}
                  participantIdentity={videoItem.identity}
                  local={videoItem.isLocal}
                />
              </VideoTileWrapper>
            );
          })}
          
          {/* 원격 오디오만 있는 경우 */}
          {remoteTracks
            .filter((item) => item.trackPublication.track?.kind === Track.Kind.Audio)
            .map((item) => {
              const track = item.trackPublication.track;
              if (track && track.kind === Track.Kind.Audio) {
                return <AudioComponent key={item.trackPublication.track?.sid || `audio-${item.participantIdentity}-${item.participant.sid}`} track={track as RemoteAudioTrack} />;
              }
              return null;
            })}
        </>
      )}
        </VideoGridContainer>
      </VideoGridScrollContainer>
      
      {/* 스크롤 버튼 (20명 넘을 때만 표시) */}
      {hasMoreThan20 && (
        <ScrollButtonContainer>
          <ScrollButton 
            onClick={scrollUp}
            disabled={!canScrollUp}
            title="위로 스크롤"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M18 15l-6-6-6 6"/>
            </svg>
          </ScrollButton>
          <ScrollButton 
            onClick={scrollDown}
            disabled={!canScrollDown}
            title="아래로 스크롤"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M6 9l6 6 6-6"/>
            </svg>
          </ScrollButton>
        </ScrollButtonContainer>
      )}
    </VideoGridOuterContainer>
  );
};