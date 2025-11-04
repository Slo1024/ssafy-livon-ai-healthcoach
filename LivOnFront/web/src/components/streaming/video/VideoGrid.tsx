import React from 'react';
import styled from 'styled-components';
import { LocalVideoTrack, RemoteVideoTrack, RemoteAudioTrack, Track } from 'livekit-client';
import { VideoComponent } from './VideoComponent';
import { AudioComponent } from './AudioComponent';

const VideoGridContainer = styled.div<{ $viewMode: 'gallery' | 'speaker' | 'shared'; $participantCount: number }>`
  display: grid;
  width: 100%;
  height: 100%;
  
  ${props => {
    if (props.$participantCount === 1) {
      return `
        grid-template-columns: 1fr;
        grid-template-rows: 1fr;
        gap: 0;
        padding: 0;
      `;
    } else if (props.$participantCount === 2) {
      return `
        grid-template-columns: 1fr 1fr;
        grid-template-rows: 1fr;
        gap: 8px;
        padding: 8px;
      `;
    } else if (props.$participantCount === 3 || props.$participantCount === 4) {
      return `
        grid-template-columns: 1fr 1fr;
        grid-template-rows: 1fr 1fr;
        gap: 8px;
        padding: 8px;
      `;
    } else {
      return `
        grid-template-columns: repeat(3, 1fr);
        grid-template-rows: repeat(auto-fit, minmax(200px, 1fr));
        gap: 8px;
        padding: 8px;
      `;
    }
  }}
`;

const VideoTileWrapper = styled.div<{ $isMain?: boolean; $fullScreen?: boolean }>`
  position: relative;
  width: 100%;
  height: 100%;
  background-color: #1f2937;
  border-radius: ${props => props.$fullScreen ? '0' : '8px'};
  overflow: hidden;
  aspect-ratio: ${props => props.$fullScreen ? 'unset' : '16/9'};
  min-height: 0;
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
  // 참가자 수 계산 (로컬 비디오 + 원격 비디오)
  const localVideoCount = (localTrack && isVideoEnabled) ? 1 : 0;
  const remoteVideoCount = remoteTracks.filter(item => item.trackPublication.track?.kind === Track.Kind.Video).length;
  const participantCount = localVideoCount + remoteVideoCount;

  return (
    <VideoGridContainer $viewMode={viewMode} $participantCount={participantCount}>
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
          {/* 로컬 비디오 */}
          {localTrack && isVideoEnabled && (() => {
            const localVideoCount = 1;
            const remoteVideoCount = remoteTracks.filter(item => item.trackPublication.track?.kind === Track.Kind.Video).length;
            const totalCount = localVideoCount + remoteVideoCount;
            return (
              <VideoTileWrapper $isMain={viewMode === 'speaker'} $fullScreen={totalCount === 1}>
                <VideoComponent
                  track={localTrack}
                  participantIdentity={participantName}
                  local={true}
                />
              </VideoTileWrapper>
            );
          })()}
          
          {/* 원격 비디오 */}
          {remoteTracks
            .filter((item) => item.trackPublication.track?.kind === Track.Kind.Video)
            .map((item) => {
              const track = item.trackPublication.track as RemoteVideoTrack;
              const localVideoCount = (localTrack && isVideoEnabled) ? 1 : 0;
              const remoteVideoCount = remoteTracks.filter(i => i.trackPublication.track?.kind === Track.Kind.Video).length;
              const totalCount = localVideoCount + remoteVideoCount;
              return (
                <VideoTileWrapper 
                  key={item.trackPublication.track?.sid || `video-${item.participantIdentity}-${item.participant.sid}`} 
                  $isMain={viewMode === 'speaker'} 
                  $fullScreen={totalCount === 1}
                >
                  <VideoComponent
                    track={track}
                    participantIdentity={item.participantIdentity}
                    local={false}
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
  );
};

