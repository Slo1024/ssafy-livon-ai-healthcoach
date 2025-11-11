import React, { useRef, useState, useEffect } from 'react';
import styled from 'styled-components';
import { LocalVideoTrack, RemoteVideoTrack, RemoteAudioTrack, Track } from 'livekit-client';
import { VideoComponent } from './VideoComponent';
import { AudioComponent } from './AudioComponent';
import infoIcon from '../../../assets/images/Vector.png';

const VideoGridOuterContainer = styled.div`
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
`;

const VideoGridScrollContainer = styled.div<{ $hasScroll: boolean; $participantCount: number }>`
  width: 100%;
  height: 100%;
  overflow-y: hidden; /* 마우스 스크롤 방지, 화살표 버튼으로만 이동 */
  overflow-x: hidden;
  padding-right: 0;
  
  /* 15명 이상일 때 정확히 3행 높이로 제한 */
  ${props => {
    if (props.$hasScroll && props.$participantCount > 15) {
      return `
        /* 3행 높이로 고정 (5x3=15개) */
        max-height: 100%;
        position: relative;
      `;
    }
    return '';
  }}
`;

const VideoGridContainer = styled.div<{ 
  $viewMode: 'gallery' | 'speaker' | 'shared'; 
  $participantCount: number;
  $isPaginationMode?: boolean;
}>`
  display: grid;
  width: 100%;
  ${props => props.$participantCount > 15 || props.$isPaginationMode ? '' : 'min-height: 100%;'}
  
  ${props => {
    const count = props.$participantCount;
    
    // 페이지네이션 모드(15명 이상)일 때는 항상 5x3 그리드 사용
    if (props.$isPaginationMode) {
      return `
        grid-template-columns: repeat(5, 1fr);
        grid-template-rows: repeat(3, 1fr);
        gap: 8px;
        padding: 8px;
        height: 100%;
      `;
    }
    
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
      // 스크롤 컨테이너가 3행까지만 보여줌 (5x3=15개)
      // 15명 넘으면 화살표 버튼으로 페이지 이동
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
  $isPaginationLastPageIncomplete?: boolean;
  $paginationLastPageItemIndex?: number;
  $paginationLastPageTotalItems?: number;
  $paginationRowIndex?: number;
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
  
  /* 15명 이상일 때 마지막 페이지에 마지막 행이 5개 미만일 때 정중앙 배치 */
  ${props => {
    if (props.$isPaginationLastPageIncomplete && props.$paginationLastPageItemIndex !== undefined && props.$paginationLastPageTotalItems !== undefined && props.$paginationRowIndex !== undefined) {
      const padding = 8; // px (좌우 각 8px)
      const gap = 8; // px
      const totalColumns = 5;
      const itemCount = props.$paginationLastPageTotalItems;
      const itemIndex = props.$paginationLastPageItemIndex;
      const rowIndex = props.$paginationRowIndex;
      
      // 각 비디오 너비: calc((100% - 16px - 32px) / 5) = calc((100% - 48px) / 5)
      const videoWidthCalc = `calc((100% - ${padding * 2}px - ${(totalColumns - 1) * gap}px) / ${totalColumns})`;
      const gapCalc = `${gap}px`;
      
      // 하단 그룹 전체 너비 계산
      const totalGaps = (itemCount - 1) * gap;
      const totalGroupWidthCalc = `calc(${videoWidthCalc} * ${itemCount} + ${totalGaps}px)`;
      
      // 좌측 오프셋 계산 (정중앙 배치)
      const leftOffsetCalc = `calc((100% - ${totalGroupWidthCalc}) / 2)`;
      
      // 각 비디오의 위치 계산 (가로 한 줄로 나란히)
      const videoPositionCalc = itemIndex === 0
        ? leftOffsetCalc
        : `calc(${leftOffsetCalc} + ${itemIndex} * (${videoWidthCalc} + ${gapCalc}))`;
      
      // 마지막 행의 행 번호 (1-based)
      const gridRowNum = rowIndex + 1;
      
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

const ParticipantInfoButton = styled.button`
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background-color: rgba(17, 24, 39, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: rgba(17, 24, 39, 0.85);
  }

  img {
    width: 14px;
    height: 14px;
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
  showInfoButtons: boolean;
  onOpenParticipantInfo: (participantIdentity: string) => void;
  isParticipantInfoAvailable: (participantIdentity: string) => boolean;
}

export const VideoGrid: React.FC<VideoGridProps> = ({
  localTrack,
  remoteTracks,
  isVideoEnabled,
  isScreenSharing,
  sharedContent,
  viewMode,
  participantName,
  showInfoButtons,
  onOpenParticipantInfo,
  isParticipantInfoAvailable,
}) => {
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const gridContainerRef = useRef<HTMLDivElement>(null);
  const [canScrollUp, setCanScrollUp] = useState(false);
  const [canScrollDown, setCanScrollDown] = useState(false);
  const [currentPage, setCurrentPage] = useState(0); // 현재 표시 중인 페이지 (0부터 시작, 15개씩)

  // 참가자 수 계산 (로컬 비디오 + 원격 비디오)
  const localVideoCount = (localTrack && isVideoEnabled) ? 1 : 0;
  const remoteVideoCount = remoteTracks.filter(item => item.trackPublication.track?.kind === Track.Kind.Video).length;
  const participantCount = localVideoCount + remoteVideoCount;

  // 15명 초과 여부 확인 (5x3=15개만 표시, 나머지는 화살표로 이동)
  const hasMoreThan15 = participantCount > 15;
  const totalPages = hasMoreThan15 ? Math.ceil(participantCount / 15) : 1;

  // 스크롤 가능 여부 확인 (페이지 기반)
  useEffect(() => {
    if (hasMoreThan15) {
      setCanScrollUp(currentPage > 0);
      setCanScrollDown(currentPage < totalPages - 1);
    } else {
      setCanScrollUp(false);
      setCanScrollDown(false);
    }
  }, [hasMoreThan15, currentPage, totalPages]);

  // 화살표 버튼으로 페이지 이동
  const scrollUp = () => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    }
  };

  const scrollDown = () => {
    if (currentPage < totalPages - 1) {
      setCurrentPage(currentPage + 1);
    }
  };

  // 참가자 수가 변경되면 첫 페이지로 리셋
  useEffect(() => {
    setCurrentPage(0);
  }, [participantCount]);

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

  // 15명 이상일 때 현재 페이지에 해당하는 비디오만 필터링
  const displayVideoTracks = hasMoreThan15
    ? allVideoTracks.slice(currentPage * 15, currentPage * 15 + 15)
    : allVideoTracks;

  return (
    <VideoGridOuterContainer>
      <VideoGridScrollContainer 
        ref={scrollContainerRef}
        $hasScroll={hasMoreThan15}
        $participantCount={participantCount}
      >
        <VideoGridContainer 
          ref={gridContainerRef}
          $viewMode={viewMode} 
          $participantCount={hasMoreThan15 ? displayVideoTracks.length : participantCount}
          $isPaginationMode={hasMoreThan15}
        >
      {isScreenSharing && !sharedContent ? (
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
                <SharedContentName>화면 공유가 시작되었습니다</SharedContentName>
                <SharedContentData>
                  <div>다른 참가자들에게 공유 중인 화면을 확인해 주세요.</div>
                </SharedContentData>
              </SharedContentInfo>
            </SharedContentHeader>
          </SharedContentCard>
        </SharedContentPanel>
      ) : (
        <>
          {/* 현재 페이지의 비디오 트랙 렌더링 (15개씩) */}
          {displayVideoTracks.map((videoItem, displayIndex) => {
            // 전체 인덱스 계산 (현재 페이지 고려)
            const index = hasMoreThan15 ? currentPage * 15 + displayIndex : displayIndex;
            // 원격 비디오의 경우 key 생성
            const key = videoItem.isLocal 
              ? `local-video-${participantName}`
              : `remote-video-${videoItem.identity}-${index}`;

            // 행과 열 계산
            // 15명 이상일 때는 현재 페이지의 비디오 수를 기준으로 계산
            const currentPageParticipantCount = hasMoreThan15 ? displayVideoTracks.length : participantCount;
            const currentIndex = hasMoreThan15 ? displayIndex : index;
            
            let rowIndex: number = 0;
            let colIndex: number = 0;
            let totalRows: number = 1;
            let isLastRow: boolean = false;
            let lastRowItemCount: number = 0;
            let lastRowIndex: number | undefined = undefined;
            
            if (currentPageParticipantCount <= 10 && !hasMoreThan15) {
              // 10명 이하 특별 레이아웃 (15명 이상이 아닐 때만)
              if (currentPageParticipantCount === 1) {
                rowIndex = 0;
                colIndex = 0;
                totalRows = 1;
                isLastRow = false;
                lastRowItemCount = 0;
                lastRowIndex = undefined;
              } else if (currentPageParticipantCount === 2) {
                // 2명: 가로 반반
                rowIndex = 0;
                colIndex = currentIndex;
                totalRows = 1;
                isLastRow = false;
                lastRowItemCount = 0;
                lastRowIndex = undefined;
              } else if (currentPageParticipantCount === 3) {
                // 3명: 상단 2개, 하단 1개
                rowIndex = Math.floor(currentIndex / 2);
                colIndex = currentIndex % 2;
                totalRows = 2;
                isLastRow = rowIndex === 1;
                lastRowItemCount = 1;
                lastRowIndex = isLastRow ? 0 : undefined;
              } else if (currentPageParticipantCount === 4) {
                // 4명: 2x2 그리드
                rowIndex = Math.floor(currentIndex / 2);
                colIndex = currentIndex % 2;
                totalRows = 2;
                isLastRow = false;
                lastRowItemCount = 0;
                lastRowIndex = undefined;
              } else if (currentPageParticipantCount === 5) {
                // 5명: 상단 3개, 하단 2개
                rowIndex = Math.floor(currentIndex / 3);
                colIndex = currentIndex % 3;
                totalRows = 2;
                isLastRow = rowIndex === 1;
                lastRowItemCount = 2;
                lastRowIndex = isLastRow ? (currentIndex - 3) : undefined;
              } else if (currentPageParticipantCount === 6) {
                // 6명: 3x2 그리드
                rowIndex = Math.floor(currentIndex / 3);
                colIndex = currentIndex % 3;
                totalRows = 2;
                isLastRow = false;
                lastRowItemCount = 0;
                lastRowIndex = undefined;
              } else if (currentPageParticipantCount === 7) {
                // 7명: 상단 4개, 하단 3개
                rowIndex = Math.floor(currentIndex / 4);
                colIndex = currentIndex % 4;
                totalRows = 2;
                isLastRow = rowIndex === 1;
                lastRowItemCount = 3;
                lastRowIndex = isLastRow ? colIndex : undefined;
              } else if (currentPageParticipantCount === 8) {
                // 8명: 4x2 그리드
                rowIndex = Math.floor(currentIndex / 4);
                colIndex = currentIndex % 4;
                totalRows = 2;
                isLastRow = false;
                lastRowItemCount = 0;
                lastRowIndex = undefined;
              } else if (currentPageParticipantCount === 9) {
                // 9명: 상단 5개, 하단 4개
                rowIndex = Math.floor(currentIndex / 5);
                colIndex = currentIndex % 5;
                totalRows = 2;
                isLastRow = rowIndex === 1;
                lastRowItemCount = 4;
                lastRowIndex = isLastRow ? colIndex : undefined;
              } else if (currentPageParticipantCount === 10) {
                // 10명: 5x2 그리드
                rowIndex = Math.floor(currentIndex / 5);
                colIndex = currentIndex % 5;
                totalRows = 2;
                isLastRow = false;
                lastRowItemCount = 0;
                lastRowIndex = undefined;
              }
            } else {
              // 11명 이상 또는 15명 이상일 때: 5열 그리드
              rowIndex = Math.floor(currentIndex / 5);
              colIndex = currentIndex % 5;
              totalRows = Math.ceil(currentPageParticipantCount / 5);
              isLastRow = rowIndex === totalRows - 1;
              lastRowItemCount = currentPageParticipantCount % 5 === 0 ? 5 : currentPageParticipantCount % 5;
              lastRowIndex = (isLastRow && lastRowItemCount < 5) ? colIndex : undefined;
            }

            // 15명 이상일 때 마지막 페이지에 마지막 행이 5개 미만(1,2,3,4개)이면 정중앙 배치
            // 조건: 15명 초과 AND 현재 페이지가 마지막 페이지 AND 마지막 행에 5개 미만
            const isLastPage = hasMoreThan15 && currentPage === totalPages - 1;
            const isPaginationLastPageIncomplete = isLastPage && 
              isLastRow && 
              lastRowItemCount < 5 && 
              lastRowItemCount > 0;

            return (
              <VideoTileWrapper 
                key={key}
                $isMain={viewMode === 'speaker'} 
                $fullScreen={currentPageParticipantCount === 1 && !hasMoreThan15}
                $participantCount={currentPageParticipantCount}
                $isLastRow={isLastRow && ((currentPageParticipantCount <= 10 && (currentPageParticipantCount === 3 || currentPageParticipantCount === 5 || currentPageParticipantCount === 7 || currentPageParticipantCount === 9)) || (currentPageParticipantCount >= 11 && lastRowItemCount < 5 && lastRowItemCount > 0))}
                $lastRowItemCount={isLastRow && ((currentPageParticipantCount <= 10 && (currentPageParticipantCount === 3 || currentPageParticipantCount === 5 || currentPageParticipantCount === 7 || currentPageParticipantCount === 9)) || (currentPageParticipantCount >= 11 && lastRowItemCount < 5 && lastRowItemCount > 0)) ? lastRowItemCount : undefined}
                $lastRowIndex={lastRowIndex}
                $rowIndex={rowIndex}
                $totalRows={totalRows}
                $isPaginationLastPageIncomplete={isPaginationLastPageIncomplete}
                $paginationLastPageItemIndex={isPaginationLastPageIncomplete ? lastRowIndex : undefined}
                $paginationLastPageTotalItems={isPaginationLastPageIncomplete ? lastRowItemCount : undefined}
                $paginationRowIndex={isPaginationLastPageIncomplete ? rowIndex : undefined}
              >
                {showInfoButtons && isParticipantInfoAvailable(videoItem.identity) && (
                  <ParticipantInfoButton
                    type="button"
                    onClick={() => onOpenParticipantInfo(videoItem.identity)}
                    aria-label={`${videoItem.identity} 정보 보기`}
                  >
                    <img src={infoIcon} alt="" />
                  </ParticipantInfoButton>
                )}
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
      
      {/* 스크롤 버튼 (15명 넘을 때만 표시) */}
      {hasMoreThan15 && (
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