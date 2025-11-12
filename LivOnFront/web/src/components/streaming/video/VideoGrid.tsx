import React, { useRef, useState, useEffect, useMemo } from 'react';
import styled from 'styled-components';
import {
  LocalVideoTrack,
  RemoteVideoTrack,
  RemoteAudioTrack,
  Track,
  TrackEvent,
} from 'livekit-client';
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

const ScreenShareLayout = styled.div`
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
  height: 100%;
  padding: 8px;
`;

const ScreenShareMain = styled.div`
  position: relative;
  border-radius: 12px;
  background-color: #000000;
  overflow: hidden;
`;

const ScreenShareLabel = styled.div`
  position: absolute;
  left: 16px;
  bottom: 16px;
  padding: 6px 12px;
  border-radius: 8px;
  background-color: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
`;

const ScreenShareSidebar = styled.div`
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

const ShareParticipantList = styled.div<{ $rows: number }>`
  flex: 1;
  display: grid;
  grid-template-rows: repeat(${(props) => Math.max(props.$rows, 1)}, minmax(0, 1fr));
  gap: 12px;
`;

const ShareSidebarTile = styled.div`
  position: relative;
  width: 100%;
  height: 100%;
  background-color: #1f2937;
  border-radius: 12px;
  overflow: hidden;
  min-height: 0;
`;

const ShareScrollControls = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
`;

const ShareScrollButton = styled.button`
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.6);
  border: none;
  color: #ffffff;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover:not(:disabled) {
    background-color: rgba(0, 0, 0, 0.8);
  }

  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
`;

const ScreenShareVideoElement = styled.video`
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: #000000;
`;

const ScreenSharePlaceholder = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #111827;
  color: #e5e7eb;
  font-size: 16px;
  font-weight: 500;
`;

const ScreenShareVideo: React.FC<{
  track: LocalVideoTrack | RemoteVideoTrack | null;
  ownerName: string;
  isLocal: boolean;
}> = ({ track, ownerName, isLocal }) => {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const [isMuted, setIsMuted] = useState<boolean>(() => !track || track.isMuted);

  useEffect(() => {
    if (videoRef.current && track) {
      track.attach(videoRef.current);
    }

    return () => {
      if (track && videoRef.current) {
        track.detach(videoRef.current);
      }
    };
  }, [track]);

  useEffect(() => {
    if (!track) {
      setIsMuted(true);
      return;
    }

    setIsMuted(track.isMuted);

    const handleMuted = () => setIsMuted(true);
    const handleUnmuted = () => setIsMuted(false);

    track.on(TrackEvent.Muted, handleMuted);
    track.on(TrackEvent.Unmuted, handleUnmuted);

    return () => {
      track.off(TrackEvent.Muted, handleMuted);
      track.off(TrackEvent.Unmuted, handleUnmuted);
    };
  }, [track]);

  const isActive = Boolean(track) && !isMuted;

  return (
    <>
      {track && (
        <ScreenShareVideoElement
          ref={videoRef}
          autoPlay
          playsInline
          muted={isLocal}
          style={{ display: isActive ? 'block' : 'none' }}
        />
      )}
      {!isActive && (
        <ScreenSharePlaceholder>
          화면 공유가 일시 중지되었습니다.
        </ScreenSharePlaceholder>
      )}
      <ScreenShareLabel>{ownerName}</ScreenShareLabel>
    </>
  );
};

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

interface ScreenShareTrackInfo {
  track: LocalVideoTrack | RemoteVideoTrack | null;
  identity: string;
  displayName: string;
  isLocal: boolean;
}

interface ParticipantTile {
  track: LocalVideoTrack | RemoteVideoTrack | null;
  identity: string;
  displayName: string;
  isLocal: boolean;
  isVideoEnabled: boolean;
}

interface VideoGridProps {
  localTrack: LocalVideoTrack | undefined;
  remoteTracks: RemoteTrackInfo[];
  isVideoEnabled: boolean;
  hasActiveScreenShare: boolean;
  screenShareTrackInfo: ScreenShareTrackInfo | null;
  viewMode: 'gallery' | 'speaker' | 'shared';
  participantName: string;
  localParticipantIdentity: string;
  showInfoButtons: boolean;
  onOpenParticipantInfo: (participantIdentity: string) => void;
  isParticipantInfoAvailable: (participantIdentity: string) => boolean;
}

export const VideoGrid: React.FC<VideoGridProps> = ({
  localTrack,
  remoteTracks,
  isVideoEnabled,
  hasActiveScreenShare,
  screenShareTrackInfo,
  viewMode,
  participantName,
  localParticipantIdentity,
  showInfoButtons,
  onOpenParticipantInfo,
  isParticipantInfoAvailable,
}) => {
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const gridContainerRef = useRef<HTMLDivElement>(null);
  const [canScrollUp, setCanScrollUp] = useState(false);
  const [canScrollDown, setCanScrollDown] = useState(false);
  const [currentPage, setCurrentPage] = useState(0); // 현재 표시 중인 페이지 (0부터 시작, 15개씩)
  const [shareStartIndex, setShareStartIndex] = useState(0);

  const participantTiles = useMemo<ParticipantTile[]>(() => {
    const tiles: ParticipantTile[] = [];

    const localIdentity = localParticipantIdentity || "__local__";
    tiles.push({
      track: localTrack ?? null,
      identity: localIdentity,
      displayName: participantName,
      isLocal: true,
      isVideoEnabled:
        Boolean(localTrack) &&
        isVideoEnabled &&
        localTrack?.isMuted === false,
    });

    const remoteMap = new Map<string, ParticipantTile>();

    remoteTracks.forEach((item) => {
      const publication = item.trackPublication;
      const track = publication.track as RemoteVideoTrack | undefined;
      const kind = publication.kind ?? track?.kind;
      const source = publication.source ?? track?.source;
      const identity = item.participantIdentity;
      const displayName =
        item.participant?.name || item.participantIdentity;

      let entry = remoteMap.get(identity);
      if (!entry) {
        entry = {
          track: null,
          identity,
          displayName,
          isLocal: false,
          isVideoEnabled: false,
        };
      }

      if (kind === Track.Kind.Video && source !== Track.Source.ScreenShare) {
        const isMuted =
          Boolean(publication.isMuted) || (track ? track.isMuted : true);
        entry.track = track ?? null;
        entry.isVideoEnabled = Boolean(track) && !isMuted;
      }

      remoteMap.set(identity, entry);
    });

    const remoteTiles = Array.from(remoteMap.values()).sort((a, b) =>
      a.displayName.localeCompare(b.displayName, "ko", { sensitivity: "base" })
    );

    return [
      ...tiles,
      ...remoteTiles,
    ];
  }, [
    localTrack,
    isVideoEnabled,
    localParticipantIdentity,
    participantName,
    remoteTracks,
  ]);

  const participantCount = participantTiles.length;
  const hasMoreThan15 = participantCount > 15;
  const totalPages = hasMoreThan15 ? Math.ceil(participantCount / 15) : 1;

  useEffect(() => {
    if (hasActiveScreenShare) {
      return;
    }

    if (hasMoreThan15) {
      setCanScrollUp(currentPage > 0);
      setCanScrollDown(currentPage < totalPages - 1);
    } else {
      setCanScrollUp(false);
      setCanScrollDown(false);
    }
  }, [hasActiveScreenShare, hasMoreThan15, currentPage, totalPages]);

  useEffect(() => {
    if (hasActiveScreenShare) {
      return;
    }
    setCurrentPage(0);
  }, [hasActiveScreenShare, participantCount]);

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

  const localTile = participantTiles.find((tile) => tile.isLocal) ?? null;
  const remoteTiles = participantTiles.filter((tile) => !tile.isLocal);
  const maxShareVisible = 4;
  const remoteVisibleSlots = Math.max(
    maxShareVisible - (localTile ? 1 : 0),
    0
  );

  useEffect(() => {
    if (!hasActiveScreenShare) {
      setShareStartIndex(0);
      return;
    }
    setShareStartIndex((prev) =>
      Math.min(prev, Math.max(remoteTiles.length - remoteVisibleSlots, 0))
    );
  }, [hasActiveScreenShare, remoteTiles.length, remoteVisibleSlots]);

  const canShareScrollUp = shareStartIndex > 0;
  const canShareScrollDown =
    shareStartIndex + remoteVisibleSlots < remoteTiles.length;

  const visibleRemoteTiles = remoteTiles.slice(
    shareStartIndex,
    shareStartIndex + remoteVisibleSlots
  );

  const visibleShareTiles = localTile
    ? [localTile, ...visibleRemoteTiles]
    : remoteTiles.slice(shareStartIndex, shareStartIndex + maxShareVisible);

  const handleShareScrollUp = () => {
    setShareStartIndex((prev) => Math.max(prev - 1, 0));
  };

  const handleShareScrollDown = () => {
    setShareStartIndex((prev) =>
      Math.min(
        prev + 1,
        Math.max(remoteTiles.length - remoteVisibleSlots, 0)
      )
    );
  };

  const audioComponents = remoteTracks
    .map((item) => {
      const track = item.trackPublication.track;
      if (track && track.kind === Track.Kind.Audio) {
        return (
          <AudioComponent
            key={
              track.sid || `audio-${item.participantIdentity}-${item.participant?.sid}`
            }
            track={track as RemoteAudioTrack}
          />
        );
      }
      return null;
    })
    .filter(
      (component): component is React.ReactElement => component !== null
    );

  if (hasActiveScreenShare && screenShareTrackInfo) {
    return (
      <VideoGridOuterContainer>
        <ScreenShareLayout>
          <ScreenShareMain>
            <ScreenShareVideo
              track={screenShareTrackInfo.track}
              ownerName={screenShareTrackInfo.displayName}
              isLocal={screenShareTrackInfo.isLocal}
            />
          </ScreenShareMain>
          <ScreenShareSidebar>
            <ShareParticipantList
              $rows={visibleShareTiles.length || 1}
            >
              {visibleShareTiles.map((tile) => (
                <ShareSidebarTile
                  key={tile.identity}
                >
                  {showInfoButtons &&
                    !tile.isLocal &&
                    isParticipantInfoAvailable(tile.identity) && (
                      <ParticipantInfoButton
                        type="button"
                        onClick={() => onOpenParticipantInfo(tile.identity)}
                        aria-label={`${tile.displayName} 정보 보기`}
                      >
                        <img src={infoIcon} alt="" />
                      </ParticipantInfoButton>
                    )}
                  <VideoComponent
                    track={tile.track ?? undefined}
                    participantIdentity={tile.displayName}
                    local={tile.isLocal}
                    isVideoEnabled={tile.isVideoEnabled}
                  />
                </ShareSidebarTile>
              ))}
            </ShareParticipantList>
            {remoteTiles.length > remoteVisibleSlots && (
              <ShareScrollControls>
                <ShareScrollButton
                  onClick={handleShareScrollUp}
                  disabled={!canShareScrollUp}
                  aria-label="이전 참가자"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M18 15l-6-6-6 6" />
                  </svg>
                </ShareScrollButton>
                <ShareScrollButton
                  onClick={handleShareScrollDown}
                  disabled={!canShareScrollDown}
                  aria-label="다음 참가자"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M6 9l6 6 6-6" />
                  </svg>
                </ShareScrollButton>
              </ShareScrollControls>
            )}
          </ScreenShareSidebar>
        </ScreenShareLayout>
        {audioComponents}
      </VideoGridOuterContainer>
    );
  }

  // 15명 이상일 때 현재 페이지에 해당하는 비디오만 필터링
  const displayParticipantTiles = hasMoreThan15
    ? participantTiles.slice(currentPage * 15, currentPage * 15 + 15)
    : participantTiles;

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
          $participantCount={hasMoreThan15 ? displayParticipantTiles.length : participantCount}
          $isPaginationMode={hasMoreThan15}
        >
          {/* 현재 페이지의 비디오 트랙 렌더링 (15개씩) */}
          {displayParticipantTiles.map((videoItem, displayIndex) => {
            // 전체 인덱스 계산 (현재 페이지 고려)
            const index = hasMoreThan15 ? currentPage * 15 + displayIndex : displayIndex;
            // 원격 비디오의 경우 key 생성
            const key = videoItem.identity;

            // 행과 열 계산
            // 15명 이상일 때는 현재 페이지의 비디오 수를 기준으로 계산
            const currentPageParticipantCount = hasMoreThan15 ? displayParticipantTiles.length : participantCount;
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
                    aria-label={`${videoItem.displayName} 정보 보기`}
                  >
                    <img src={infoIcon} alt="" />
                  </ParticipantInfoButton>
                )}
                <VideoComponent
                  track={videoItem.track ?? undefined}
                  participantIdentity={videoItem.displayName}
                  local={videoItem.isLocal}
                  isVideoEnabled={videoItem.isVideoEnabled}
                />
              </VideoTileWrapper>
            );
          })}
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
      {audioComponents}
    </VideoGridOuterContainer>
  );
};