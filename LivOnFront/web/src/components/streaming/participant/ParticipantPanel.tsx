import React from 'react';
import styled from 'styled-components';
import { RemoteParticipant, RemoteTrackPublication, Track } from 'livekit-client';

interface RemoteTrackInfo {
  trackPublication: RemoteTrackPublication;
  participantIdentity: string;
  participant: RemoteParticipant;
}

const ParticipantPanelContainer = styled.div<{ $isOpen: boolean }>`
  width: ${props => props.$isOpen ? '320px' : '0'};
  height: 100%;
  background-color: #ffffff;
  border-left: ${props => props.$isOpen ? '1px solid #e5e7eb' : 'none'};
  display: flex;
  flex-direction: column;
  opacity: ${props => props.$isOpen ? '1' : '0'};
  visibility: ${props => props.$isOpen ? 'visible' : 'hidden'};
  transition: width 0.3s ease, opacity 0.3s ease, visibility 0.3s ease, border-left 0.3s ease;
  overflow: hidden;
  pointer-events: ${props => props.$isOpen ? 'auto' : 'none'};
`;

const ParticipantHeader = styled.div`
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 600;
  font-size: 16px;
  color: #111827;
`;

const ParticipantSearchBar = styled.div`
  padding: 12px 16px;
`;

const ParticipantSearchInput = styled.input`
  width: 100%;
  padding: 8px 12px 8px 36px;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  font-size: 14px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f9fafb;
  color: #111827;
  position: relative;
  
  &:focus {
    outline: none;
    border-color: #e5e7eb;
    background-color: #ffffff;
  }
  
  &::placeholder {
    color: #9ca3af;
  }
`;

const ParticipantSearchWrapper = styled.div`
  position: relative;
  width: 100%;
`;

const ParticipantSearchIcon = styled.div`
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const ParticipantList = styled.div`
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

const ParticipantEmptyState = styled.div`
  text-align: center;
  color: #9ca3af;
  padding: 40px 20px;
  font-size: 14px;
`;

const ParticipantItem = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 8px;
`;

const ParticipantAvatar = styled.div`
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #4965f6;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
`;

const ParticipantInfo = styled.div`
  flex: 1;
  min-width: 0;
`;

const ParticipantListItemName = styled.div`
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
`;

const ParticipantStatus = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #6b7280;
`;

const StatusIndicator = styled.div<{ $isActive: boolean }>`
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: ${props => props.$isActive ? '#10b981' : '#9ca3af'};
`;

interface ParticipantPanelProps {
  isOpen: boolean;
  participantName: string;
  remoteTracks: RemoteTrackInfo[];
  participantSearchQuery: string;
  onParticipantSearchChange: (value: string) => void;
  isVideoEnabled: boolean;
  isAudioEnabled: boolean;
}

export const ParticipantPanel: React.FC<ParticipantPanelProps> = ({
  isOpen,
  participantName,
  remoteTracks,
  participantSearchQuery,
  onParticipantSearchChange,
  isVideoEnabled,
  isAudioEnabled,
}) => {
  const getInitials = (name: string) => {
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  // 중복 제거: 같은 참가자의 여러 트랙 중 하나만 표시
  const uniqueParticipants = new Map<string, RemoteTrackInfo>();
  remoteTracks.forEach((item) => {
    const participantId = item.participantIdentity;
    if (!uniqueParticipants.has(participantId)) {
      uniqueParticipants.set(participantId, item);
    }
  });

  // 검색어로 필터링
  const filteredParticipants: Array<{ type: 'local' | 'remote'; identity: string; data?: RemoteTrackInfo }> = [];
  
  // 로컬 참가자 (코치) 추가
  const localName = participantName;
  if (!participantSearchQuery || localName.toLowerCase().includes(participantSearchQuery.toLowerCase())) {
    filteredParticipants.push({
      type: 'local',
      identity: localName,
    });
  }

  // 원격 참가자 필터링 및 추가
  const participantList = Array.from(uniqueParticipants.values());
  participantList.forEach((item) => {
    if (!participantSearchQuery || item.participantIdentity.toLowerCase().includes(participantSearchQuery.toLowerCase())) {
      filteredParticipants.push({
        type: 'remote',
        identity: item.participantIdentity,
        data: item,
      });
    }
  });

  return (
    <ParticipantPanelContainer $isOpen={isOpen}>
      <ParticipantHeader>참가자</ParticipantHeader>
      <ParticipantSearchBar>
        <ParticipantSearchWrapper>
          <ParticipantSearchIcon>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8"></circle>
              <path d="m21 21-4.35-4.35"></path>
            </svg>
          </ParticipantSearchIcon>
          <ParticipantSearchInput
            type="text"
            placeholder="참가자 검색..."
            value={participantSearchQuery}
            onChange={(e) => onParticipantSearchChange(e.target.value)}
          />
        </ParticipantSearchWrapper>
      </ParticipantSearchBar>
      <ParticipantList>
        {filteredParticipants.length === 0 ? (
          <ParticipantEmptyState>
            {participantSearchQuery ? '검색 결과가 없습니다.' : '다른 참가자가 없습니다.'}
          </ParticipantEmptyState>
        ) : (
          filteredParticipants.map((participant, index) => {
            if (participant.type === 'local') {
              return (
                <ParticipantItem key="local-participant">
                  <ParticipantAvatar>{getInitials(participant.identity)}</ParticipantAvatar>
                  <ParticipantInfo>
                    <ParticipantListItemName>{participant.identity} (나)</ParticipantListItemName>
                    <ParticipantStatus>
                      <StatusIndicator $isActive={isVideoEnabled} />
                      <span>비디오 {isVideoEnabled ? '켜짐' : '꺼짐'}</span>
                      <StatusIndicator $isActive={isAudioEnabled} />
                      <span>오디오 {isAudioEnabled ? '켜짐' : '꺼짐'}</span>
                    </ParticipantStatus>
                  </ParticipantInfo>
                </ParticipantItem>
              );
            } else {
              const item = participant.data!;
              const hasVideo = remoteTracks.some(
                (t) => t.participantIdentity === item.participantIdentity && 
                t.trackPublication.track?.kind === Track.Kind.Video
              );
              const hasAudio = remoteTracks.some(
                (t) => t.participantIdentity === item.participantIdentity && 
                t.trackPublication.track?.kind === Track.Kind.Audio
              );

              return (
                <ParticipantItem key={item.participantIdentity}>
                  <ParticipantAvatar>{getInitials(item.participantIdentity)}</ParticipantAvatar>
                  <ParticipantInfo>
                    <ParticipantListItemName>{item.participantIdentity}</ParticipantListItemName>
                    <ParticipantStatus>
                      <StatusIndicator $isActive={hasVideo} />
                      <span>비디오 {hasVideo ? '켜짐' : '꺼짐'}</span>
                      <StatusIndicator $isActive={hasAudio} />
                      <span>오디오 {hasAudio ? '켜짐' : '꺼짐'}</span>
                    </ParticipantStatus>
                  </ParticipantInfo>
                </ParticipantItem>
              );
            }
          })
        )}
      </ParticipantList>
    </ParticipantPanelContainer>
  );
};

