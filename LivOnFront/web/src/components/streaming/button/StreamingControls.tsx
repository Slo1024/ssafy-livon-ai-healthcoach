import React from 'react';
import styled from 'styled-components';
import videoOffIcon from '../../../assets/images/video_off.png';

const ControlBar = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px 24px;
  background-color: rgba(0, 0, 0, 0.9);
  color: #ffffff;
  position: relative;
`;

const ControlBarLeft = styled.div`
  display: flex;
  gap: 16px;
  align-items: center;
  flex: 1;
  visibility: hidden;
`;

const ControlBarCenter = styled.div`
  display: flex;
  gap: 16px;
  align-items: center;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
`;

const ControlBarRight = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
  flex: 1;
  justify-content: flex-end;
`;

const ControlButton = styled.button<{ $active?: boolean; $variant?: 'danger' }>`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 12px;
  background-color: transparent;
  border: none;
  outline: none;
  color: ${props => {
    if (props.$variant === 'danger') return '#ffffff';
    return '#9ca3af'; // 모든 버튼 기본 색상을 회색으로 통일
  }};
  cursor: pointer;
  font-size: 12px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  transition: all 0.2s ease;
  width: 100px;
  min-width: 100px;
  max-width: 100px;
  white-space: nowrap;
  box-sizing: border-box;
  flex-shrink: 0;
  
  &:hover {
    color: ${props => props.$variant === 'danger' ? '#ffffff' : '#ffffff'};
  }
  
  &:focus {
    outline: none;
    box-shadow: none;
  }
  
  &:active {
    outline: none;
    box-shadow: none;
  }
  
  svg {
    width: 24px;
    height: 24px;
    flex-shrink: 0;
  }
`;

const LeaveButton = styled.button`
  padding: 12px 24px;
  background-color: #dc2626;
  color: #ffffff;
  border: none;
  outline: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  
  &:hover {
    background-color: #b91c1c;
  }
  
  &:focus {
    outline: none;
    box-shadow: none;
  }
  
  &:active {
    outline: none;
    box-shadow: none;
  }
`;

interface StreamingControlsProps {
  isAudioEnabled: boolean;
  isVideoEnabled: boolean;
  showParticipants: boolean;
  isChatOpen: boolean;
  isScreenSharing: boolean;
  onToggleAudio: () => void;
  onToggleVideo: () => void;
  onToggleParticipants: () => void;
  onToggleChat: () => void;
  onShareScreen: () => void;
  onLeave: () => void;
}

export const StreamingControls: React.FC<StreamingControlsProps> = ({
  isAudioEnabled,
  isVideoEnabled,
  showParticipants,
  isChatOpen,
  isScreenSharing,
  onToggleAudio,
  onToggleVideo,
  onToggleParticipants,
  onToggleChat,
  onShareScreen,
  onLeave,
}) => {
  return (
    <ControlBar>
      <ControlBarLeft>
        {/* 비어있음 */}
      </ControlBarLeft>

      <ControlBarCenter>
        <ControlButton onClick={onToggleAudio} $active={isAudioEnabled}>
          {isAudioEnabled ? (
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/>
            </svg>
          ) : (
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z"/>
            </svg>
          )}
          음소거 {isAudioEnabled ? '해제' : ''}
        </ControlButton>
        <ControlButton onClick={onToggleVideo} $active={isVideoEnabled}>
          {isVideoEnabled ? (
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z"/>
            </svg>
          ) : (
            <img src={videoOffIcon} alt="비디오 끄기" style={{ width: '24px', height: '24px' }} />
          )}
          비디오 {isVideoEnabled ? '중지' : '시작'}
        </ControlButton>
        <ControlButton onClick={onToggleParticipants} $active={showParticipants}>
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>
          </svg>
          참가자
        </ControlButton>
        <ControlButton onClick={onToggleChat} $active={isChatOpen}>
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/>
          </svg>
          채팅
        </ControlButton>
        <ControlButton onClick={onShareScreen} $active={isScreenSharing}>
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M20 18c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2H0v2h24v-2h-4zM4 6h16v10H4V6z"/>
          </svg>
          화면 공유
        </ControlButton>
      </ControlBarCenter>

      <ControlBarRight>
        <LeaveButton onClick={onLeave}>나가기</LeaveButton>
      </ControlBarRight>
    </ControlBar>
  );
};

