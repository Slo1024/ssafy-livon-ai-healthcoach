import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import styled from 'styled-components';
import { Room, RoomEvent, LocalVideoTrack, RemoteVideoTrack, RemoteAudioTrack, RemoteTrackPublication, RemoteParticipant, RemoteTrack, Track, DataPacket_Kind } from 'livekit-client';
import { StreamingEndModal } from '../../components/common/Modal';
import { ROUTES } from '../../constants/routes';
import { CONFIG } from '../../constants/config';
import { useAuth } from '../../hooks/useAuth';
import { ChatPanel } from '../../components/streaming/chat/ChatPanel';
import { ParticipantPanel } from '../../components/streaming/participant/ParticipantPanel';
import { VideoGrid } from '../../components/streaming/video/VideoGrid';
import { StreamingControls } from '../../components/streaming/button/StreamingControls';

const TOKEN_SERVER_URL = CONFIG.LIVEKIT.APPLICATION_SERVER_URL;

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

const VideoGridWrapper = styled.div`
  flex: 1;
  overflow: hidden;
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
  const [participantSearchQuery, setParticipantSearchQuery] = useState('');
  const [sharedContent, setSharedContent] = useState<{
    type: 'ai-analysis';
    memberName: string;
  } | null>(null);
  const [roomName] = useState(() => {
    // URL 쿼리 파라미터에서 roomName 가져오기 (같은 방 참여용)
    const searchParams = new URLSearchParams(location.search);
    const roomParam = searchParams.get('room');
    
    if (roomParam) {
      return roomParam;
    }
    
    // location.state에서 reservationId 가져오기
    if (location.state?.reservationId) {
      return location.state.reservationId;
    }
    
    // 기본값: 새로운 방 생성 (같은 세션에서 재접속 시 같은 roomName 사용)
    const defaultRoomName = `room-${Date.now()}`;
    return defaultRoomName;
  });
  
  const [participantName] = useState(() => {
    // URL 쿼리 파라미터에서 participantName 가져오기 (참가자 이름 구분용)
    const searchParams = new URLSearchParams(location.search);
    const nameParam = searchParams.get('name');
    
    if (nameParam) {
      return nameParam;
    }
    
    // 기본값: 사용자 닉네임 또는 '코치'
    return `${user?.nickname || '코치'} 코치님`;
  });

  // 토큰 발급 API 호출
  const getToken = async (): Promise<string> => {
    const tokenUrl = `${TOKEN_SERVER_URL}/token`;
    const requestBody = {
      roomName,
      participantName,
      role: 'coach',
    };

    console.log('🔑 토큰 발급 요청:', {
      url: tokenUrl,
      server: TOKEN_SERVER_URL,
      body: requestBody,
      environment: process.env.NODE_ENV,
    });

    try {
      const response = await fetch(tokenUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
      });

      console.log('📡 토큰 발급 응답:', {
        status: response.status,
        statusText: response.statusText,
        ok: response.ok,
        url: response.url,
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('❌ 토큰 발급 실패 - 응답 내용:', errorText);
        throw new Error(`토큰 발급 실패 (${response.status}): ${errorText || response.statusText}`);
      }

      const data = await response.json();
      console.log('✅ 토큰 발급 성공:', {
        hasToken: !!data.token,
        tokenLength: data.token?.length || 0,
      });

      if (!data.token) {
        console.error('❌ 토큰 발급 응답에 token이 없습니다:', data);
        throw new Error('토큰 발급 응답에 token이 없습니다');
      }

      return data.token;
    } catch (error) {
      console.error('❌ 토큰 발급 오류:', error);
      // 개발 환경에서는 백엔드 서버가 실행되지 않았을 수 있음
      // 실제 배포 환경에서는 백엔드 API가 필수입니다
      const errorMessage = error instanceof Error ? error.message : '토큰 발급 실패';
      throw new Error(`토큰 발급에 실패했습니다. 백엔드 서버(${TOKEN_SERVER_URL})가 실행 중인지 확인해주세요. 오류: ${errorMessage}`);
    }
  };

  // 방 입장 로직
  const isConnectingRef = useRef(false);
  const roomRef = useRef<Room | undefined>(undefined);
  const isMountedRef = useRef(true);
  
  useEffect(() => {
    isMountedRef.current = true;
    
    // 이미 연결 중이거나 연결되어 있으면 중복 실행 방지
    if (isConnectingRef.current) {
      return;
    }
    
    // 기존 room이 있고 연결되어 있으면 재연결 방지
    if (roomRef.current && (roomRef.current.state === 'connected' || roomRef.current.state === 'reconnecting')) {
      return;
    }
    
    // 기존 room 정리
    if (roomRef.current) {
      try {
        roomRef.current.disconnect();
      } catch (error) {
        console.error('기존 room 정리 오류:', error);
      }
      roomRef.current = undefined;
      setRoom(undefined);
    }
    
    let newRoom: Room | undefined;
    isConnectingRef.current = true;
    
    const joinRoom = async () => {
      // 컴포넌트가 언마운트되었으면 중단
      if (!isMountedRef.current) {
        if (newRoom) {
          try {
            newRoom.disconnect();
          } catch (error) {
            console.error('방 연결 해제 오류:', error);
          }
        }
        return;
      }
      
      try {
        // Room 객체 생성
        newRoom = new Room();
        
        // 컴포넌트가 언마운트되었는지 다시 확인
        if (!isMountedRef.current) {
          try {
            newRoom.disconnect();
          } catch (error) {
            console.error('방 연결 해제 오류:', error);
          }
          return;
        }
        
        roomRef.current = newRoom;
        setRoom(newRoom);

        // 이벤트 리스너 등록
        newRoom.on(RoomEvent.TrackSubscribed, (track: RemoteTrack, publication: RemoteTrackPublication, participant: RemoteParticipant) => {
          if (track.kind === Track.Kind.Video || track.kind === Track.Kind.Audio) {
            const participantId = participant.identity || participant.name || 'Unknown';
            console.log('트랙 구독됨:', {
              trackKind: track.kind,
              participantId,
              participantIdentity: participant.identity,
              participantName: participant.name,
              trackSid: track.sid,
            });
            
            setRemoteTracks((prev) => {
              // 중복 체크 - track.sid 사용
              const trackSid = track.sid;
              const exists = prev.some(
                (item) => {
                  const itemTrackSid = item.trackPublication.track?.sid;
                  return itemTrackSid === trackSid && item.participantIdentity === participantId;
                }
              );
              if (!exists) {
                console.log('새 원격 트랙 추가:', participantId, track.kind);
                return [
                  ...prev,
                  {
                    trackPublication: publication,
                    participantIdentity: participantId,
                    participant,
                  },
                ];
              }
              console.log('중복 트랙 무시:', participantId, track.kind);
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

        newRoom.on(RoomEvent.ParticipantConnected, (participant: RemoteParticipant) => {
          console.log('참가자 연결됨:', {
            identity: participant.identity,
            name: participant.name,
            sid: participant.sid,
          });
        });

        newRoom.on(RoomEvent.ParticipantDisconnected, (participant: RemoteParticipant) => {
          console.log('참가자 연결 해제됨:', {
            identity: participant.identity,
            name: participant.name,
            sid: participant.sid,
          });
          // 해당 참가자의 모든 트랙 제거
          setRemoteTracks((prev) =>
            prev.filter((item) => item.participantIdentity !== (participant.identity || participant.name || 'Unknown'))
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

        // 컴포넌트가 언마운트되었는지 확인
        if (!isMountedRef.current || roomRef.current !== newRoom) {
          try {
            newRoom.disconnect();
          } catch (error) {
            console.error('방 연결 해제 오류:', error);
          }
          return;
        }

        // 방 연결
        await newRoom.connect(CONFIG.LIVEKIT.SERVER_URL, token);

        // 컴포넌트가 언마운트되었는지 다시 확인
        if (!isMountedRef.current || roomRef.current !== newRoom) {
          try {
            newRoom.disconnect();
          } catch (error) {
            console.error('방 연결 해제 오류:', error);
          }
          return;
        }

        // 연결이 완료된 후 약간의 지연을 두고 비디오/오디오 활성화
        // 엔진이 완전히 준비될 때까지 대기
        await new Promise(resolve => setTimeout(resolve, 500));

        // 컴포넌트가 언마운트되었는지 다시 확인
        if (!isMountedRef.current || roomRef.current !== newRoom || newRoom.state === 'disconnected') {
          return;
        }

        // 연결 상태 확인 후 비디오/오디오 활성화
        if (newRoom && (newRoom.state === 'connected' || newRoom.state === 'reconnecting')) {
          try {
            // 초기에는 비디오만 활성화 (AudioContext 경고 방지)
            // 오디오는 사용자가 상호작용한 후 활성화되도록 함
            await newRoom.localParticipant.setCameraEnabled(true);
            
            // 오디오는 사용자 제스처 후에 활성화 (음소거 해제 버튼 클릭 시)
            // AudioContext 경고를 피하기 위해 초기에는 비활성화
            await newRoom.localParticipant.setMicrophoneEnabled(false);

            // 로컬 비디오 트랙 가져오기 (약간의 지연 후)
            setTimeout(() => {
              if (newRoom) {
                const videoTrack = newRoom.localParticipant.videoTrackPublications.values().next().value?.track as LocalVideoTrack;
                if (videoTrack) {
                  setLocalTrack(videoTrack);
                }
              }
            }, 300);

            // 초기 상태 설정
            setIsVideoEnabled(true);
            setIsAudioEnabled(false); // 초기에는 오디오 비활성화
          } catch (error) {
            console.error('비디오/오디오 활성화 오류:', error);
            // 에러가 발생해도 계속 진행
          }
        }
      } catch (error) {
        console.error('방 입장 오류:', error);
        // 에러 메시지 표시 (사용자에게 알림)
        const errorMessage = error instanceof Error ? error.message : '방 입장에 실패했습니다.';
        alert(errorMessage);
      } finally {
        isConnectingRef.current = false;
      }
    };

    joinRoom();

    // 정리 함수
    return () => {
      isMountedRef.current = false;
      isConnectingRef.current = false;
      const roomToDisconnect = roomRef.current || newRoom;
      if (roomToDisconnect) {
        try {
          // 이미 disconnected 상태가 아니면 disconnect 호출
          if (roomToDisconnect.state !== 'disconnected') {
            roomToDisconnect.disconnect();
          }
        } catch (error) {
          console.error('방 연결 해제 오류:', error);
        }
        roomRef.current = undefined;
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
        
        // 권한 거부 오류인 경우 사용자에게 알림
        if (error instanceof Error) {
          if (error.name === 'NotAllowedError' || error.message.includes('Permission denied')) {
            alert('화면 공유 권한이 거부되었습니다. 브라우저에서 화면 공유 권한을 허용해주세요.');
          } else if (error.name === 'AbortError' || error.message.includes('canceled')) {
            // 사용자가 화면 선택을 취소한 경우는 조용히 처리
            console.log('화면 공유가 취소되었습니다.');
          } else {
            alert('화면 공유에 실패했습니다. 다시 시도해주세요.');
          }
        }
      }
    } else {
      try {
        await room.localParticipant.setScreenShareEnabled(false);
        setSharedContent(null);
        setIsScreenSharing(false);
        setViewMode('gallery');
      } catch (error) {
        console.error('화면 공유 중지 오류:', error);
        // 중지 오류는 조용히 처리 (이미 중지된 상태일 수 있음)
      }
    }
  };

  const handleToggleChat = () => {
    setIsChatOpen(prev => {
      if (!prev) {
        // 채팅을 열 때 참가자 패널 닫기
        setShowParticipants(false);
      }
      return !prev;
    });
  };

  const handleToggleParticipants = () => {
    setShowParticipants(prev => {
      if (!prev) {
        // 참가자 패널을 열 때 채팅 패널 닫기
        setIsChatOpen(false);
      }
      return !prev;
    });
  };

  const handleSendMessage = async () => {
    if (!chatInput.trim()) {
      console.warn('채팅 메시지가 비어있습니다.');
      return;
    }
    
    if (!room) {
      console.error('Room이 연결되지 않았습니다.');
      alert('방에 연결되지 않았습니다. 잠시 후 다시 시도해주세요.');
      return;
    }

    try {
      const encoder = new TextEncoder();
      const message = {
        text: chatInput,
        sender: participantName,
        timestamp: new Date().toISOString(),
      };
      
      // 메시지 전송
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
      // 에러 발생 시에도 로컬 메시지로 표시
      const newMessage: ChatMessage = {
        id: Date.now().toString(),
        sender: participantName,
        message: chatInput,
        timestamp: new Date(),
      };
      setChatMessages((prev) => [...prev, newMessage]);
      setChatInput('');
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


  return (
    <StreamingContainer>
      {/* 상단 바 */}
      <TopBar>
        <TopBarLeft>
          {/* Original Sound Indicator 제거됨 */}
        </TopBarLeft>
        <TopBarRight>
          {/* 보기 버튼 제거됨 */}
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
        {/* 비디오 그리드 */}
        <VideoGridWrapper>
          <VideoGrid
            localTrack={localTrack}
            remoteTracks={remoteTracks}
            isVideoEnabled={isVideoEnabled}
            isScreenSharing={isScreenSharing}
            sharedContent={sharedContent}
            viewMode={viewMode}
            participantName={participantName}
          />
        </VideoGridWrapper>

        {/* 참가자 패널 */}
        <ParticipantPanel
          isOpen={showParticipants}
          participantName={participantName}
          remoteTracks={remoteTracks}
          participantSearchQuery={participantSearchQuery}
          onParticipantSearchChange={setParticipantSearchQuery}
          isVideoEnabled={isVideoEnabled}
          isAudioEnabled={isAudioEnabled}
        />

        {/* 채팅 패널 */}
        <ChatPanel
          isOpen={isChatOpen}
          messages={chatMessages}
          chatInput={chatInput}
          onChatInputChange={setChatInput}
          onSendMessage={handleSendMessage}
        />
      </MainContentArea>

      {/* 하단 컨트롤 바 */}
      <StreamingControls
        isAudioEnabled={isAudioEnabled}
        isVideoEnabled={isVideoEnabled}
        showParticipants={showParticipants}
        isChatOpen={isChatOpen}
        isScreenSharing={isScreenSharing}
        onToggleAudio={handleToggleAudio}
        onToggleVideo={handleToggleVideo}
        onToggleParticipants={handleToggleParticipants}
        onToggleChat={handleToggleChat}
        onShareScreen={handleShareScreen}
        onLeave={handleLeave}
      />

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
