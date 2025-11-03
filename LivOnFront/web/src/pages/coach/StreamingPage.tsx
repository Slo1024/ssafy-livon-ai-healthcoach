import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import styled from 'styled-components';
import { Room, RoomEvent, LocalVideoTrack, RemoteVideoTrack, RemoteAudioTrack, RemoteTrackPublication, RemoteParticipant, RemoteTrack, Track, DataPacket_Kind } from 'livekit-client';
import { StreamingEndModal } from '../../components/common/Modal';
import { ROUTES } from '../../constants/routes';
import { CONFIG } from '../../constants/config';
import { useAuth } from '../../hooks/useAuth';
import { VideoComponent } from '../../components/streaming/VideoComponent';
import { AudioComponent } from '../../components/streaming/AudioComponent';
import videoOffIcon from '../../assets/images/video_off.png';

const StreamingContainer = styled.div`
  width: 100vw;
  height: 100vh;
  background-color: #000000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const TopBar = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: rgba(0, 0, 0, 0.8);
  color: #ffffff;
  font-size: 14px;
  z-index: 10;
`;

const TopBarLeft = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
`;

const OriginalSoundIndicator = styled.div`
  display: flex;
  align-items: center;
  gap: 6px;
  color: #10b981;
  font-size: 12px;
`;

const TopBarRight = styled.div`
  display: flex;
  gap: 12px;
`;

const ViewButton = styled.button`
  padding: 6px 12px;
  background-color: transparent;
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  
  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }
`;

const ScreenShareBar = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background-color: rgba(0, 0, 0, 0.9);
  color: #ffffff;
  font-size: 14px;
`;

const ScreenShareInfo = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

const ZoomControls = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
`;

const ZoomButton = styled.button`
  padding: 4px 8px;
  background-color: transparent;
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  
  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }
`;

const MainContentArea = styled.div`
  flex: 1;
  display: flex;
  position: relative;
  overflow: hidden;
`;

const VideoGrid = styled.div<{ $viewMode: 'gallery' | 'speaker' | 'shared' }>`
  flex: 1;
  display: grid;
  gap: 8px;
  padding: 16px;
  grid-template-columns: ${props => {
    if (props.$viewMode === 'shared') return '1fr 1fr';
    if (props.$viewMode === 'speaker') return '1fr';
    return 'repeat(auto-fill, minmax(200px, 1fr))';
  }};
  grid-template-rows: ${props => {
    if (props.$viewMode === 'speaker' || props.$viewMode === 'shared') return '1fr';
    return 'repeat(auto-fill, minmax(150px, 1fr))';
  }};
  overflow-y: auto;
`;

const VideoTileWrapper = styled.div<{ $isMain?: boolean }>`
  position: relative;
  aspect-ratio: 16 / 9;
  border-radius: 8px;
  overflow: hidden;
  border: ${props => props.$isMain ? '2px solid #fbbf24' : '1px solid rgba(255, 255, 255, 0.2)'};
  background-color: #1f2937;
`;

const VideoElement = styled.video`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;

const VideoPlaceholder = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #1f2937;
  color: #9ca3af;
`;

const ParticipantName = styled.div`
  position: absolute;
  bottom: 8px;
  left: 8px;
  background-color: rgba(0, 0, 0, 0.7);
  color: #ffffff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
`;

const AudioIndicator = styled.div`
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  background-color: #ef4444;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const SharedContentPanel = styled.div`
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  padding: 32px;
`;

const SharedContentTitle = styled.h2`
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 24px;
  text-align: center;
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

const ChatPanel = styled.div<{ $isOpen: boolean }>`
  width: ${props => props.$isOpen ? '320px' : '0'};
  height: 100%;
  background-color: #ffffff;
  border-left: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
`;

const ChatHeader = styled.div`
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 600;
  font-size: 16px;
  color: #111827;
`;

const ChatMessages = styled.div`
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

const ChatMessage = styled.div`
  display: flex;
  gap: 8px;
`;

const ChatAvatar = styled.div`
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #4965f6;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
`;

const ChatMessageContent = styled.div`
  flex: 1;
`;

const ChatMessageSender = styled.div`
  font-size: 12px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
`;

const ChatMessageText = styled.div`
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
`;

const ChatInputArea = styled.div`
  padding: 16px;
  border-top: 1px solid #e5e7eb;
`;

const ChatRecipient = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
`;

const ChatInputWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: flex-end;
`;

const ChatInput = styled.input`
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  
  &:focus {
    outline: none;
    border-color: #4965f6;
  }
`;

const ChatInputActions = styled.div`
  display: flex;
  gap: 8px;
  margin-top: 8px;
`;

const ChatActionButton = styled.button`
  width: 32px;
  height: 32px;
  padding: 0;
  background-color: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  
  &:hover {
    color: #111827;
  }
`;

const ChatSendButton = styled.button`
  padding: 8px 16px;
  background-color: #4965f6;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  
  &:hover {
    background-color: #3b5dd8;
  }
`;

const ControlBar = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background-color: rgba(0, 0, 0, 0.9);
  color: #ffffff;
`;

const ControlBarLeft = styled.div`
  display: flex;
  gap: 16px;
  align-items: center;
`;

const ControlBarCenter = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const ControlBarRight = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const ControlButton = styled.button<{ $active?: boolean; $variant?: 'danger' }>`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background-color: transparent;
  border: none;
  color: ${props => {
    if (props.$variant === 'danger') return '#ffffff';
    return props.$active ? '#ffffff' : '#9ca3af';
  }};
  cursor: pointer;
  font-size: 12px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  transition: all 0.2s ease;
  
  &:hover {
    color: ${props => props.$variant === 'danger' ? '#ffffff' : '#ffffff'};
    background-color: rgba(255, 255, 255, 0.1);
  }
  
  svg {
    width: 24px;
    height: 24px;
  }
`;

const LeaveButton = styled.button`
  padding: 12px 24px;
  background-color: #dc2626;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  
  &:hover {
    background-color: #b91c1c;
  }
`;

interface RemoteTrackInfo {
  trackPublication: RemoteTrackPublication;
  participantIdentity: string;
  participant: RemoteParticipant;
}

interface ChatMessage {
  id: string;
  sender: string;
  message: string;
  timestamp: Date;
}

export const StreamingPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const [room, setRoom] = useState<Room | undefined>(undefined);
  const [localTrack, setLocalTrack] = useState<LocalVideoTrack | undefined>(undefined);
  const [remoteTracks, setRemoteTracks] = useState<RemoteTrackInfo[]>([]);
  const [isVideoEnabled, setIsVideoEnabled] = useState(true);
  const [isAudioEnabled, setIsAudioEnabled] = useState(false);
  const [isScreenSharing, setIsScreenSharing] = useState(false);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [showParticipants, setShowParticipants] = useState(false);
  const [showEndModal, setShowEndModal] = useState(false);
  const [viewMode, setViewMode] = useState<'gallery' | 'speaker' | 'shared'>('gallery');
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [chatInput, setChatInput] = useState('');
  const [sharedContent, setSharedContent] = useState<{
    type: 'ai-analysis';
    memberName: string;
  } | null>(null);
  const [participantName] = useState(`${user?.nickname || '코치'} 코치님`);
  const [roomName] = useState(() => {
    // 예약 ID 또는 세션 식별자 생성 (location.state에서 가져오거나 자동 생성)
    return location.state?.reservationId || `room-${Date.now()}`;
  });

  // 토큰 발급 API 호출
  const getToken = async (): Promise<string> => {
    try {
      const response = await fetch(`${CONFIG.LIVEKIT.APPLICATION_SERVER_URL}/token`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          roomName,
          participantName,
          role: 'coach',
        }),
      });

      if (!response.ok) {
        throw new Error('토큰 발급 실패');
      }

      const data = await response.json();
      return data.token;
    } catch (error) {
      console.error('토큰 발급 오류:', error);
      // 개발 환경에서는 백엔드 서버가 실행되지 않았을 수 있음
      // 실제 배포 환경에서는 백엔드 API가 필수입니다
      const errorMessage = error instanceof Error ? error.message : '토큰 발급 실패';
      throw new Error(`토큰 발급에 실패했습니다. 백엔드 서버(${CONFIG.LIVEKIT.APPLICATION_SERVER_URL})가 실행 중인지 확인해주세요. 오류: ${errorMessage}`);
    }
  };

  // 방 입장 로직
  useEffect(() => {
    let newRoom: Room | undefined;
    
    const joinRoom = async () => {
      try {
        // Room 객체 생성
        newRoom = new Room();
        setRoom(newRoom);

        // 이벤트 리스너 등록
        newRoom.on(RoomEvent.TrackSubscribed, (track: RemoteTrack, publication: RemoteTrackPublication, participant: RemoteParticipant) => {
          if (track.kind === Track.Kind.Video || track.kind === Track.Kind.Audio) {
            setRemoteTracks((prev) => {
              // 중복 체크 - track.sid 사용
              const trackSid = track.sid;
              const participantId = participant.identity || participant.name || 'Unknown';
              const exists = prev.some(
                (item) => {
                  const itemTrackSid = item.trackPublication.track?.sid;
                  return itemTrackSid === trackSid && item.participantIdentity === participantId;
                }
              );
              if (!exists) {
                return [
                  ...prev,
                  {
                    trackPublication: publication,
                    participantIdentity: participantId,
                    participant,
                  },
                ];
              }
              return prev;
            });
          }
        });

        newRoom.on(RoomEvent.TrackUnsubscribed, (track: RemoteTrack, publication: RemoteTrackPublication, participant: RemoteParticipant) => {
          const trackSid = track.sid;
          const participantId = participant.identity || participant.name || 'Unknown';
          setRemoteTracks((prev) =>
            prev.filter(
              (item) => {
                const itemTrackSid = item.trackPublication.track?.sid;
                return !(itemTrackSid === trackSid && item.participantIdentity === participantId);
              }
            )
          );
        });

        newRoom.on(RoomEvent.DataReceived, (payload: Uint8Array, participant?: RemoteParticipant, kind?: DataPacket_Kind) => {
          try {
            const decoder = new TextDecoder();
            const message = JSON.parse(decoder.decode(payload));
            const newMessage: ChatMessage = {
              id: Date.now().toString(),
              sender: participant?.identity || participant?.name || 'Unknown',
              message: message.text || message.message || '',
              timestamp: new Date(),
            };
            setChatMessages((prev) => [...prev, newMessage]);
          } catch (error) {
            console.error('채팅 메시지 파싱 오류:', error);
          }
        });

        // 토큰 발급
        const token = await getToken();

        // 방 연결
        await newRoom.connect(CONFIG.LIVEKIT.SERVER_URL, token);

        // 로컬 비디오/오디오 활성화
        await newRoom.localParticipant.enableCameraAndMicrophone();

        // 로컬 비디오 트랙 가져오기
        const videoTrack = newRoom.localParticipant.videoTrackPublications.values().next().value?.track as LocalVideoTrack;
        if (videoTrack) {
          setLocalTrack(videoTrack);
        }

        // 초기 상태 설정
        setIsVideoEnabled(true);
        setIsAudioEnabled(true);
      } catch (error) {
        console.error('방 입장 오류:', error);
        // 에러 메시지 표시 (사용자에게 알림)
        const errorMessage = error instanceof Error ? error.message : '방 입장에 실패했습니다.';
        alert(errorMessage);
      }
    };

    joinRoom();

    // 정리 함수
    return () => {
      if (newRoom) {
        newRoom.disconnect();
        setRoom(undefined);
        setLocalTrack(undefined);
        setRemoteTracks([]);
      }
    };
  }, [roomName, participantName]);

  const handleToggleVideo = async () => {
    if (!room) return;
    
    const newState = !isVideoEnabled;
    await room.localParticipant.setCameraEnabled(newState);
    setIsVideoEnabled(newState);
    
    if (newState) {
      const videoTrack = room.localParticipant.videoTrackPublications.values().next().value?.track as LocalVideoTrack;
      if (videoTrack) {
        setLocalTrack(videoTrack);
      }
    } else {
      setLocalTrack(undefined);
    }
  };

  const handleToggleAudio = async () => {
    if (!room) return;
    
    const newState = !isAudioEnabled;
    await room.localParticipant.setMicrophoneEnabled(newState);
    setIsAudioEnabled(newState);
  };

  const handleShareScreen = async () => {
    if (!room) return;
    
    if (!isScreenSharing) {
      try {
        await room.localParticipant.setScreenShareEnabled(true);
        // AI 분석본 공유
        setSharedContent({
          type: 'ai-analysis',
          memberName: '김싸피',
        });
        setIsScreenSharing(true);
        setViewMode('shared');
      } catch (error) {
        console.error('화면 공유 오류:', error);
      }
    } else {
      try {
        await room.localParticipant.setScreenShareEnabled(false);
        setSharedContent(null);
        setIsScreenSharing(false);
        setViewMode('gallery');
      } catch (error) {
        console.error('화면 공유 중지 오류:', error);
      }
    }
  };

  const handleToggleChat = () => {
    setIsChatOpen(prev => !prev);
  };

  const handleSendMessage = async () => {
    if (!chatInput.trim() || !room) return;

    try {
      const encoder = new TextEncoder();
      const message = {
        text: chatInput,
        sender: participantName,
        timestamp: new Date().toISOString(),
      };
      
      await room.localParticipant.publishData(encoder.encode(JSON.stringify(message)), {
        reliable: true,
      });

      // 로컬 메시지 추가 (즉시 표시)
      const newMessage: ChatMessage = {
        id: Date.now().toString(),
        sender: participantName,
        message: chatInput,
        timestamp: new Date(),
      };
      setChatMessages((prev) => [...prev, newMessage]);
      setChatInput('');
    } catch (error) {
      console.error('메시지 전송 오류:', error);
    }
  };

  const handleLeave = () => {
    setShowEndModal(true);
  };

  const handleEndModalConfirm = async () => {
    // 방 나가기 및 정리
    if (room) {
      await room.disconnect();
      setRoom(undefined);
      setLocalTrack(undefined);
      setRemoteTracks([]);
    }
    setShowEndModal(false);
    navigate(ROUTES.RESERVATION_LIST);
  };

  const getInitials = (name: string) => {
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <StreamingContainer>
      {/* 상단 바 */}
      <TopBar>
        <TopBarLeft>
          <OriginalSoundIndicator>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
            Original Sound: Off
          </OriginalSoundIndicator>
        </TopBarLeft>
        <TopBarRight>
          <ViewButton onClick={() => setViewMode(viewMode === 'gallery' ? 'speaker' : 'gallery')}>
            보기
          </ViewButton>
        </TopBarRight>
      </TopBar>

      {/* 화면 공유 바 */}
      {isScreenSharing && (
        <ScreenShareBar>
          <ScreenShareInfo>
            {user?.nickname || '코치'} 코치님 화면 공유 중
          </ScreenShareInfo>
          <ZoomControls>
            <ZoomButton>-</ZoomButton>
            <span>100%</span>
            <ZoomButton>+</ZoomButton>
          </ZoomControls>
        </ScreenShareBar>
      )}

      {/* 메인 콘텐츠 영역 */}
      <MainContentArea>
        {/* 비디오 그리드 또는 공유 콘텐츠 */}
        <VideoGrid $viewMode={viewMode}>
          {isScreenSharing && sharedContent ? (
            <SharedContentPanel>
              <SharedContentTitle>{sharedContent.memberName} 회원님 생체 정보 AI 분석본</SharedContentTitle>
              <SharedContentCard>
                <SharedContentHeader>
                  <ProfileIcon>
                    <svg width="40" height="40" viewBox="0 0 24 24" fill="#9ca3af">
                      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                  </ProfileIcon>
                  <SharedContentInfo>
                    <SharedContentName>{sharedContent.memberName} 회원님</SharedContentName>
                    <SharedContentData>
                      <div>신장: 170cm</div>
                      <div>체중: 50kg</div>
                      <div>수면 시간: 7시간</div>
                    </SharedContentData>
                  </SharedContentInfo>
                </SharedContentHeader>
                <SharedContentAnalysis>
                  <AnalysisText>
                    현재 회원님의 신체 문제점으로는 고혈압, 수면 질 저하, 활동 부족이 지목되고 있습니다. 특히, 회원님의 신체적 특성상 해당 문제점들은 더욱 악영향이 클 것으로 예상됩니다.
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
              {localTrack && isVideoEnabled && (
                <VideoTileWrapper $isMain={viewMode === 'speaker'}>
                  <VideoComponent
                    track={localTrack}
                    participantIdentity={participantName}
                    local={true}
                  />
                </VideoTileWrapper>
              )}
              
              {/* 원격 비디오 */}
              {remoteTracks
                .filter((item) => item.trackPublication.track?.kind === Track.Kind.Video)
                .map((item) => {
                  const track = item.trackPublication.track as RemoteVideoTrack;
                  return (
                    <VideoTileWrapper key={item.trackPublication.track?.sid || `video-${item.participantIdentity}-${item.participant.sid}`} $isMain={viewMode === 'speaker'}>
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
        </VideoGrid>

        {/* 채팅 패널 */}
        <ChatPanel $isOpen={isChatOpen}>
          <ChatHeader>채팅</ChatHeader>
          <ChatMessages>
            {chatMessages.length === 0 ? (
              <div style={{ textAlign: 'center', color: '#9ca3af', padding: '20px' }}>
                메시지가 없습니다.
              </div>
            ) : (
              chatMessages.map((msg) => (
                <ChatMessage key={msg.id}>
                  <ChatAvatar>{getInitials(msg.sender)}</ChatAvatar>
                  <ChatMessageContent>
                    <ChatMessageSender>{msg.sender}</ChatMessageSender>
                    <ChatMessageText>{msg.message}</ChatMessageText>
                  </ChatMessageContent>
                </ChatMessage>
              ))
            )}
          </ChatMessages>
          <ChatInputArea>
            <ChatRecipient>
              받는 사람: 모두
              <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                <path d="M7 10l5 5 5-5z"/>
              </svg>
            </ChatRecipient>
            <ChatInputWrapper>
              <ChatInput
                type="text"
                placeholder="여기에 메시지 입력..."
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    handleSendMessage();
                  }
                }}
              />
              <ChatSendButton onClick={handleSendMessage}>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
                </svg>
              </ChatSendButton>
            </ChatInputWrapper>
            <ChatInputActions>
              <ChatActionButton>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                </svg>
              </ChatActionButton>
              <ChatActionButton>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
                </svg>
              </ChatActionButton>
              <ChatActionButton>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
                </svg>
              </ChatActionButton>
              <ChatActionButton>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm3.5-9c.83 0 1.5-.67 1.5-1.5S16.33 8 15.5 8 14 8.67 14 9.5s.67 1.5 1.5 1.5zm-7 0c.83 0 1.5-.67 1.5-1.5S9.33 8 8.5 8 7 8.67 7 9.5 7.67 11 8.5 11zm3.5 6.5c2.33 0 4.31-1.46 5.11-3.5H6.89c.8 2.04 2.78 3.5 5.11 3.5z"/>
                </svg>
              </ChatActionButton>
            </ChatInputActions>
          </ChatInputArea>
        </ChatPanel>
      </MainContentArea>

      {/* 하단 컨트롤 바 */}
      <ControlBar>
        <ControlBarLeft>
          <ControlButton onClick={handleToggleAudio} $active={isAudioEnabled}>
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
          <ControlButton onClick={handleToggleVideo} $active={isVideoEnabled}>
            {isVideoEnabled ? (
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z"/>
              </svg>
            ) : (
              <img src={videoOffIcon} alt="비디오 끄기" style={{ width: '24px', height: '24px' }} />
            )}
            비디오 {isVideoEnabled ? '중지' : '시작'}
          </ControlButton>
          <ControlButton>
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/>
            </svg>
            보안
          </ControlButton>
        </ControlBarLeft>

        <ControlBarCenter>
          <ControlButton onClick={() => setShowParticipants(!showParticipants)} $active={showParticipants}>
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>
            </svg>
            참가자
          </ControlButton>
          <ControlButton onClick={handleToggleChat} $active={isChatOpen}>
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/>
            </svg>
            채팅
          </ControlButton>
          <ControlButton onClick={handleShareScreen} $active={isScreenSharing}>
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M20 18c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2H0v2h24v-2h-4zM4 6h16v10H4V6z"/>
            </svg>
            화면 공유
          </ControlButton>
          <ControlButton>
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 14c1.66 0 2.99-1.34 2.99-3L15 5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z"/>
            </svg>
            기록
          </ControlButton>
          <ControlButton>
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
            반응
          </ControlButton>
        </ControlBarCenter>

        <ControlBarRight>
          <LeaveButton onClick={handleLeave}>나가기</LeaveButton>
        </ControlBarRight>
      </ControlBar>

      {/* 스트리밍 종료 모달 */}
      <StreamingEndModal
        open={showEndModal}
        onClose={() => setShowEndModal(false)}
        onConfirm={handleEndModalConfirm}
      />
    </StreamingContainer>
  );
};

export default StreamingPage;
