import React, {
  useState,
  useEffect,
  useRef,
  useCallback,
  useMemo,
} from "react";
import { useNavigate, useLocation } from "react-router-dom";
import styled from "styled-components";
import {
  Room,
  RoomEvent,
  LocalVideoTrack,
  LocalTrackPublication,
  RemoteTrackPublication,
  RemoteParticipant,
  RemoteTrack,
  RemoteVideoTrack,
  Track,
  TrackEvent,
} from "livekit-client";
import { StreamingEndModal } from "../../components/common/Modal";
import { ROUTES } from "../../constants/routes";
import { CONFIG } from "../../constants/config";
import { useAuth } from "../../hooks/useAuth";
import {
  StompChatClient,
  createChatRoom,
  getChatMessagesSince,
  GoodsChatMessageResponse,
  setAuthToken,
} from "../../api/chattingApi";
import { ChatPanel } from "../../components/streaming/chat/ChatPanel";
import { ParticipantPanel } from "../../components/streaming/participant/ParticipantPanel";
import { VideoGrid } from "../../components/streaming/video/VideoGrid";
import { StreamingControls } from "../../components/streaming/button/StreamingControls";
import {
  ParticipantInfo,
  ParticipantDetail,
} from "../../components/streaming/participant/ParticipantInfo";
import { getParticipantInfoApi } from "../../api/reservationApi";

const API_BASE_URL =
  CONFIG.API_BASE_URL ||
  process.env.REACT_APP_API_BASE_URL ||
  "http://localhost:8081";

const StreamingContainer = styled.div`
  width: 100vw;
  height: 100vh;
  background-color: #000000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
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
  senderImage?: string;
  senderUserId?: string;
}

export const StreamingPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, isLoading: isAuthLoading } = useAuth();
  const [room, setRoom] = useState<Room | undefined>(undefined);
  const [localTrack, setLocalTrack] = useState<LocalVideoTrack | undefined>(
    undefined
  );
  const [remoteTracks, setRemoteTracks] = useState<RemoteTrackInfo[]>([]);
  const [isVideoEnabled, setIsVideoEnabled] = useState(true);
  const [isAudioEnabled, setIsAudioEnabled] = useState(false);
  const [isScreenSharing, setIsScreenSharing] = useState(false);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [showParticipants, setShowParticipants] = useState(false);
  const [showEndModal, setShowEndModal] = useState(false);
  const [viewMode, setViewMode] = useState<"gallery" | "speaker" | "shared">(
    "gallery"
  );
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [chatInput, setChatInput] = useState("");
  const [participantSearchQuery, setParticipantSearchQuery] = useState("");
  const [chatRoomId, setChatRoomId] = useState<number | null>(null);
  const [participantName] = useState(() => {
    // URL 쿼리 파라미터에서 participantName 가져오기 (참가자 이름 구분용)
    const searchParams = new URLSearchParams(location.search);
    const nameParam = searchParams.get("name");

    if (nameParam) {
      return nameParam;
    }

    // 기본값: 사용자 닉네임 또는 '코치'
    return user?.nickname ? `${user.nickname} 코치님` : "코치님";
  });
  const [selectedParticipantId, setSelectedParticipantId] = useState<
    string | null
  >(null);
  const [localScreenShareTrack, setLocalScreenShareTrack] =
    useState<LocalVideoTrack | null>(null);
  const remoteScreenSharePublication = useMemo(
    () =>
      remoteTracks.find((item) => {
        const publication = item.trackPublication;
        const source = publication.source ?? publication.track?.source;
        const kind = publication.kind ?? publication.track?.kind;
        return kind === Track.Kind.Video && source === Track.Source.ScreenShare;
      }),
    [remoteTracks]
  );
  const screenShareTrackInfo = useMemo(() => {
    if (localScreenShareTrack) {
      return {
        track: localScreenShareTrack as
          | LocalVideoTrack
          | RemoteVideoTrack
          | null,
        identity: room?.localParticipant?.identity || "__local__",
        displayName: participantName,
        isLocal: true,
      };
    }

    if (remoteScreenSharePublication) {
      const track =
        (remoteScreenSharePublication.trackPublication.track as
          | RemoteVideoTrack
          | null
          | undefined) || null;
      return {
        track,
        identity:
          remoteScreenSharePublication.participant?.identity ||
          remoteScreenSharePublication.participantIdentity,
        displayName:
          remoteScreenSharePublication.participant?.name ||
          remoteScreenSharePublication.participantIdentity,
        isLocal: false,
      };
    }

    return null;
  }, [
    localScreenShareTrack,
    participantName,
    remoteScreenSharePublication,
    room,
  ]);
  const hasActiveScreenShare = Boolean(
    localScreenShareTrack || remoteScreenSharePublication
  );
  const screenShareOwnerName =
    screenShareTrackInfo?.displayName ||
    remoteScreenSharePublication?.participant?.name ||
    remoteScreenSharePublication?.participantIdentity ||
    participantName;
  const localParticipantIdentity =
    room?.localParticipant?.identity || "__local__";
  const [roomName] = useState(() => {
    const consultationId =
      location.state?.consultationId || location.state?.reservationId;
    return `consultation-${consultationId}`;
  });

  const [participantInfoMap, setParticipantInfoMap] = useState<
    Record<string, ParticipantDetail>
  >({});
  const [isLoadingParticipantInfo, setIsLoadingParticipantInfo] =
    useState(false);

  // 참여자 정보를 API에서 가져오는 함수
  const fetchParticipantInfo = useCallback(async () => {
    // 코치가 아니거나 consultationId가 없으면 API 호출하지 않음
    if (user?.role !== "coach" || isAuthLoading) {
      return;
    }

    const consultationId =
      location.state?.consultationId || location.state?.reservationId;
    if (!consultationId) {
      return;
    }

    // 토큰 가져오기
    const accessToken = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);
    if (!accessToken) {
      console.warn("⚠️ [참여자 정보] 인증 토큰이 없습니다.");
      return;
    }

    setIsLoadingParticipantInfo(true);
    try {
      console.log("🔵 [참여자 정보] API 호출 시작:", { consultationId });
      const participantInfo = await getParticipantInfoApi(
        accessToken,
        consultationId
      );

      console.log("🔵 [참여자 정보] API 응답:", participantInfo);

      // API 응답을 ParticipantDetail 형식으로 변환
      const memberInfo = participantInfo.memberInfo;
      const healthData = memberInfo.healthData;

      // badges 생성: 건강 상태 데이터 기반
      const badges: string[] = [];
      if (healthData.activityLevel) {
        badges.push(`활동 수준: ${healthData.activityLevel}`);
      }
      if (healthData.sleepQuality) {
        badges.push(`수면 질: ${healthData.sleepQuality}`);
      }
      if (healthData.stressLevel) {
        badges.push(`스트레스 수준: ${healthData.stressLevel}`);
      }

      // notes 생성: 건강 데이터 요약
      const notesParts: string[] = [];
      if (healthData.height) {
        notesParts.push(`신장: ${healthData.height}cm`);
      }
      if (healthData.weight) {
        notesParts.push(`체중: ${healthData.weight}kg`);
      }
      if (healthData.steps) {
        notesParts.push(`일일 걸음 수: ${healthData.steps}걸음`);
      }
      if (healthData.sleepTime) {
        const hours = Math.floor(healthData.sleepTime / 60);
        const minutes = healthData.sleepTime % 60;
        notesParts.push(
          `수면 시간: ${hours}시간 ${minutes > 0 ? `${minutes}분` : ""}`
        );
      }
      const notes = notesParts.join(", ");

      // questions: preQna가 있으면 사용 (실제로는 별도 필드가 필요할 수 있음)
      const questions: string[] = [];

      // analysis 생성: 건강 데이터 기반 분석 결과
      const analysisSummary: string[] = [];
      if (healthData.height && healthData.weight) {
        const bmi = healthData.weight / Math.pow(healthData.height / 100, 2);
        analysisSummary.push(`BMI: ${bmi.toFixed(1)}`);
      }
      if (healthData.sleepTime) {
        const sleepHours = healthData.sleepTime / 60;
        if (sleepHours < 7) {
          analysisSummary.push("수면 시간이 부족합니다.");
        } else if (sleepHours > 9) {
          analysisSummary.push("수면 시간이 충분합니다.");
        }
      }
      if (healthData.steps) {
        if (healthData.steps < 5000) {
          analysisSummary.push("일일 활동량을 늘리는 것이 좋습니다.");
        } else if (healthData.steps >= 10000) {
          analysisSummary.push("활동량이 충분합니다.");
        }
      }

      const analysisTip: string[] = [];
      if (healthData.sleepQuality === "poor") {
        analysisTip.push("규칙적인 수면 패턴을 유지하세요.");
      }
      if (healthData.stressLevel === "high") {
        analysisTip.push("스트레스 관리를 위한 운동을 추천합니다.");
      }
      if (healthData.activityLevel === "low") {
        analysisTip.push("점진적으로 활동량을 늘려가세요.");
      }

      const participantDetail: ParticipantDetail = {
        name: memberInfo.nickname,
        badges,
        notes,
        questions,
        analysis: {
          generatedAt: new Date().toLocaleDateString("ko-KR", {
            year: "numeric",
            month: "long",
            day: "numeric",
          }),
          type: "건강 상태 분석",
          summary:
            analysisSummary.length > 0
              ? analysisSummary.join(" ")
              : "건강 데이터를 분석한 결과입니다.",
          tip:
            analysisTip.length > 0
              ? analysisTip.join(" ")
              : "규칙적인 운동과 건강한 식습관을 유지하세요.",
        },
      };

      // 참가자 identity 찾기 (remoteTracks에서 참가자와 매칭)
      // 1:1 상담이므로 remoteTracks의 첫 번째 원격 참가자를 참가자로 간주
      // 닉네임이나 identity로 매칭 시도
      let participantIdentity = memberInfo.nickname;

      // remoteTracks에서 닉네임이 일치하는 참가자 찾기
      const matchingParticipant = remoteTracks.find(
        (track) =>
          track.participant?.name === memberInfo.nickname ||
          track.participantIdentity === memberInfo.nickname
      );

      if (matchingParticipant) {
        // remoteTracks의 identity를 우선 사용
        participantIdentity =
          matchingParticipant.participantIdentity ||
          matchingParticipant.participant?.identity ||
          memberInfo.nickname;
      }

      setParticipantInfoMap((prev) => ({
        ...prev,
        [participantIdentity]: participantDetail,
      }));

      // 닉네임으로도 매핑 추가 (참가자 이름만으로도 접근 가능하도록)
      if (participantIdentity !== memberInfo.nickname) {
        setParticipantInfoMap((prev) => ({
          ...prev,
          [memberInfo.nickname]: participantDetail,
        }));
      }

      console.log("🔵 [참여자 정보] 변환 완료:", {
        identity: participantIdentity,
        detail: participantDetail,
      });
    } catch (error) {
      console.error("❌ [참여자 정보] API 호출 오류:", error);
      // 에러 발생 시에도 화상 통화는 계속되도록 조용히 처리
    } finally {
      setIsLoadingParticipantInfo(false);
    }
  }, [user?.role, isAuthLoading, location.state, remoteTracks]);

  // 참여자 정보 가져오기 (코치이고 consultationId가 있을 때)
  useEffect(() => {
    fetchParticipantInfo();
  }, [fetchParticipantInfo]);

  // remoteTracks 업데이트 시 참가자 정보와 매칭하여 identity 업데이트
  useEffect(() => {
    if (
      user?.role !== "coach" ||
      Object.keys(participantInfoMap).length === 0
    ) {
      return;
    }

    // participantInfoMap의 항목들을 순회하며 remoteTracks와 매칭
    const updatedMap: Record<string, ParticipantDetail> = {
      ...participantInfoMap,
    };

    Object.entries(participantInfoMap).forEach(([key, detail]) => {
      // key가 닉네임인 경우, remoteTracks에서 해당 참가자 찾기
      const matchingParticipant = remoteTracks.find(
        (track) =>
          track.participant?.name === detail.name ||
          track.participantIdentity === detail.name ||
          track.participant?.name === key ||
          track.participantIdentity === key
      );

      if (matchingParticipant) {
        const participantIdentity =
          matchingParticipant.participantIdentity ||
          matchingParticipant.participant?.identity ||
          key;

        // identity로 매핑 추가 (기존 key와 다를 경우)
        if (participantIdentity !== key) {
          updatedMap[participantIdentity] = detail;
        }
      }
    });

    // 변경사항이 있으면 업데이트
    if (JSON.stringify(updatedMap) !== JSON.stringify(participantInfoMap)) {
      setParticipantInfoMap(updatedMap);
    }
  }, [remoteTracks, participantInfoMap, user?.role]);

  const handleOpenParticipantInfo = useCallback(
    (identity: string) => {
      // 코치인 경우 항상 모달 열기
      if (user?.role === "coach") {
        setSelectedParticipantId(identity);
        // 참여자 정보가 아직 로드되지 않았고 consultationId가 있으면 로드 시도
        if (!participantInfoMap[identity]) {
          const consultationId =
            location.state?.consultationId || location.state?.reservationId;
          if (consultationId && !isLoadingParticipantInfo) {
            fetchParticipantInfo();
          }
        }
      } else if (participantInfoMap[identity]) {
        setSelectedParticipantId(identity);
      }
    },
    [
      participantInfoMap,
      user?.role,
      location.state,
      isLoadingParticipantInfo,
      fetchParticipantInfo,
    ]
  );

  const handleCloseParticipantInfo = useCallback(() => {
    setSelectedParticipantId(null);
  }, []);

  // 토큰 발급 API 호출
  const getToken = async (): Promise<string> => {
    const tokenUrl = `${API_BASE_URL}/token`;

    // consultationId를 location.state에서 가져온다고 가정
    const consultationId =
      location.state?.consultationId || location.state?.reservationId;

    if (!consultationId) {
      throw new Error("상담 ID가 없습니다. 예약 페이지에서 다시 접속해주세요.");
    }

    // 고유한 identity 생성 (participantName + consultationId + timestamp + random)
    const uniqueIdentity = `${participantName}-${consultationId}-${Date.now()}-${Math.random()
      .toString(36)
      .substring(2, 9)}`;

    const requestBody = {
      consultationId: consultationId, // Long 타입
      participantName: participantName, // 선택사항
      identity: uniqueIdentity, // 고유한 identity 추가
    };

    // JWT 토큰 가져오기
    const accessToken = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);
    if (!accessToken) {
      throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
    }

    console.log("🔑 토큰 발급 요청:", {
      url: tokenUrl,
      body: requestBody,
    });

    try {
      const response = await fetch(tokenUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "토큰 발급 실패");
      }

      const data = await response.json();

      // ApiResponse 형태로 반환되므로
      if (!data.result?.token) {
        throw new Error("토큰 발급 응답에 token이 없습니다");
      }

      return data.result.token;
    } catch (error) {
      console.error("❌ 토큰 발급 오류:", error);
      throw error;
    }
  };

  // 방 입장 로직
  const isConnectingRef = useRef(false);
  const roomRef = useRef<Room | undefined>(undefined);
  const isMountedRef = useRef(true);
  const screenShareTrackRef = useRef<LocalVideoTrack | null>(null);
  const stompChatClientRef = useRef<StompChatClient | null>(null);

  const clearScreenShareState = useCallback(() => {
    screenShareTrackRef.current = null;
    setIsScreenSharing(false);
    setViewMode("gallery");
    setLocalScreenShareTrack(null);
  }, []);

  useEffect(() => {
    isMountedRef.current = true;

    // 이미 연결 중이거나 연결되어 있으면 중복 실행 방지
    if (isConnectingRef.current) {
      return;
    }

    // 기존 room이 있고 연결되어 있으면 재연결 방지
    if (
      roomRef.current &&
      (roomRef.current.state === "connected" ||
        roomRef.current.state === "reconnecting")
    ) {
      return;
    }

    // 기존 room 정리
    if (roomRef.current) {
      try {
        roomRef.current.disconnect();
      } catch (error) {
        console.error("기존 room 정리 오류:", error);
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
            console.error("방 연결 해제 오류:", error);
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
            console.error("방 연결 해제 오류:", error);
          }
          return;
        }

        roomRef.current = newRoom;
        setRoom(newRoom);

        // 이벤트 리스너 등록
        newRoom.on(
          RoomEvent.TrackSubscribed,
          (
            track: RemoteTrack,
            publication: RemoteTrackPublication,
            participant: RemoteParticipant
          ) => {
            if (
              track.kind === Track.Kind.Video ||
              track.kind === Track.Kind.Audio
            ) {
              const participantId =
                participant.identity || participant.name || "Unknown";
              console.log("트랙 구독됨:", {
                trackKind: track.kind,
                participantId,
                participantIdentity: participant.identity,
                participantName: participant.name,
                trackSid: track.sid,
              });

              setRemoteTracks((prev) => {
                // 중복 체크 - track.sid 사용
                const trackSid = track.sid;
                const exists = prev.some((item) => {
                  const itemTrackSid = item.trackPublication.track?.sid;
                  return (
                    itemTrackSid === trackSid &&
                    item.participantIdentity === participantId
                  );
                });
                if (!exists) {
                  console.log("새 원격 트랙 추가:", participantId, track.kind);
                  return [
                    ...prev,
                    {
                      trackPublication: publication,
                      participantIdentity: participantId,
                      participant,
                    },
                  ];
                }
                console.log("중복 트랙 무시:", participantId, track.kind);
                return prev;
              });
            }
          }
        );

        newRoom.on(
          RoomEvent.TrackUnsubscribed,
          (
            track: RemoteTrack,
            publication: RemoteTrackPublication,
            participant: RemoteParticipant
          ) => {
            const trackSid = track.sid;
            const participantId =
              participant.identity || participant.name || "Unknown";
            setRemoteTracks((prev) =>
              prev.filter((item) => {
                const itemTrackSid = item.trackPublication.track?.sid;
                return !(
                  itemTrackSid === trackSid &&
                  item.participantIdentity === participantId
                );
              })
            );
          }
        );

        newRoom.on(
          RoomEvent.ParticipantConnected,
          (participant: RemoteParticipant) => {
            console.log("참가자 연결됨:", {
              identity: participant.identity,
              name: participant.name,
              sid: participant.sid,
            });
          }
        );

        newRoom.on(
          RoomEvent.ParticipantDisconnected,
          (participant: RemoteParticipant) => {
            console.log("참가자 연결 해제됨:", {
              identity: participant.identity,
              name: participant.name,
              sid: participant.sid,
            });
            // 해당 참가자의 모든 트랙 제거
            setRemoteTracks((prev) =>
              prev.filter(
                (item) =>
                  item.participantIdentity !==
                  (participant.identity || participant.name || "Unknown")
              )
            );
          }
        );

        // 토큰 발급
        const token = await getToken();

        // 컴포넌트가 언마운트되었는지 확인
        if (!isMountedRef.current || roomRef.current !== newRoom) {
          try {
            newRoom.disconnect();
          } catch (error) {
            console.error("방 연결 해제 오류:", error);
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
            console.error("방 연결 해제 오류:", error);
          }
          return;
        }

        // 연결이 완료된 후 약간의 지연을 두고 비디오/오디오 활성화
        // 엔진이 완전히 준비될 때까지 대기
        await new Promise((resolve) => setTimeout(resolve, 500));

        // 컴포넌트가 언마운트되었는지 다시 확인
        if (
          !isMountedRef.current ||
          roomRef.current !== newRoom ||
          newRoom.state === "disconnected"
        ) {
          return;
        }

        // 연결 상태 확인 후 비디오/오디오 활성화
        if (
          newRoom &&
          (newRoom.state === "connected" || newRoom.state === "reconnecting")
        ) {
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
                const videoTrack =
                  newRoom.localParticipant.videoTrackPublications
                    .values()
                    .next().value?.track as LocalVideoTrack;
                if (videoTrack) {
                  setLocalTrack(videoTrack);
                }
              }
            }, 300);

            // 초기 상태 설정
            setIsVideoEnabled(true);
            setIsAudioEnabled(false); // 초기에는 오디오 비활성화

            // 채팅방 생성 및 STOMP 연결 (방 입장 후)
            // user가 로드될 때까지 기다림
            if (isAuthLoading) {
              console.log("🔵 [채팅] 사용자 정보 로딩 중...");
              return;
            }

            const consultationId =
              location.state?.consultationId || location.state?.reservationId;

            console.log("🔵 [채팅] 채팅방 생성 조건 확인:", {
              consultationId,
              hasUserId: !!user?.id,
              userId: user?.id,
              userObject: user,
              isAuthLoading,
              locationState: location.state,
            });

            if (consultationId && user?.id) {
              try {
                // JWT 토큰 가져오기 및 설정
                const accessToken = localStorage.getItem(
                  CONFIG.TOKEN.ACCESS_TOKEN_KEY
                );
                if (!accessToken) {
                  console.error("❌ [채팅] AccessToken이 없습니다.");
                  throw new Error("인증 토큰이 없습니다. 로그인이 필요합니다.");
                }

                // 채팅 API 클라이언트에 토큰 설정
                setAuthToken(accessToken);
                console.log("🔵 [채팅] 인증 토큰 설정 완료");

                console.log("🔵 [채팅] 채팅방 생성 시작:", {
                  consultationId,
                  userId: user.id,
                });

                // 채팅방 생성
                const chatRoom = await createChatRoom(consultationId);
                console.log("🔵 [채팅] 채팅방 생성 완료:", {
                  chatRoomId: chatRoom.chatRoomId,
                  chatRoomStatus: chatRoom.chatRoomStatus,
                });
                setChatRoomId(chatRoom.chatRoomId);

                // 과거 메시지 로드 (처음에는 null로 전송하여 전체 메시지 조회)
                const pastMessages = await getChatMessagesSince(
                  chatRoom.chatRoomId,
                  null // 처음 조회 시 null
                );
                console.log("🔵 [채팅] 과거 메시지 로드:", {
                  count: pastMessages.length,
                });

                // 과거 메시지를 ChatMessage 형식으로 변환
                // 서버에서 받는 userId (UUID)를 그대로 표시
                const convertedMessages: ChatMessage[] = pastMessages.map(
                  (msg) => ({
                    id: msg.id,
                    sender: msg.userId, // UUID를 그대로 표시
                    message: msg.content,
                    timestamp: new Date(msg.sentAt),
                    senderUserId: msg.userId,
                  })
                );
                // 시간순 정렬 (오래된 것부터 최신 순서로 - 최신 메시지가 아래로)
                const sortedMessages = convertedMessages.sort(
                  (a, b) => a.timestamp.getTime() - b.timestamp.getTime()
                );
                setChatMessages(sortedMessages);

                // STOMP 웹소켓 연결 (accessToken은 이미 위에서 가져옴)
                console.log("🔵 [채팅] STOMP 연결 준비:", {
                  hasAccessToken: !!accessToken,
                  chatRoomId: chatRoom.chatRoomId,
                  userId: user.id,
                });

                if (accessToken) {
                  const stompClient = new StompChatClient();
                  stompChatClientRef.current = stompClient;

                  console.log("🔵 [채팅] STOMP 연결 시도 시작...");
                  try {
                    await stompClient.connect(
                      chatRoom.chatRoomId,
                      user.id,
                      accessToken,
                      (message: GoodsChatMessageResponse) => {
                        console.log("🔵 [채팅] 새 메시지 수신:", {
                          messageId: message.id,
                          type: message.type,
                          message: message.message,
                          sender: message.sender,
                          senderUserId: message.sender?.userId,
                          currentUserId: user?.id,
                          currentUserEmail: user?.email,
                          userObject: user,
                        });

                        // ENTER, LEAVE 같은 시스템 메시지는 표시하지 않음
                        if (
                          message.type === "ENTER" ||
                          message.type === "LEAVE"
                        ) {
                          console.log(
                            "🔵 [채팅] 시스템 메시지 무시:",
                            message.type
                          );
                          return;
                        }

                        // TALK 타입 메시지만 처리
                        if (message.type !== "TALK") {
                          console.log(
                            "🔵 [채팅] 알 수 없는 메시지 타입:",
                            message.type
                          );
                          return;
                        }

                        // 중복 체크 및 메시지 추가
                        setChatMessages((prev) => {
                          // [2-1] ID 기반 중복 체크
                          const existsById = prev.some(
                            (msg) => msg.id === message.id
                          );
                          if (existsById) {
                            console.log(
                              "🔵 [채팅] ID 중복 메시지 무시:",
                              message.id
                            );
                            return prev;
                          }

                          // [2-2] 내용+시간+사용자 기반 중복 체크 (5초 이내)
                          const messageSentAt = new Date(
                            message.sentAt
                          ).getTime();
                          const senderId = message.sender?.userId || "";
                          const isDuplicate = prev.some((msg) => {
                            const msgTime = msg.timestamp.getTime();
                            const timeDiff = Math.abs(messageSentAt - msgTime);
                            const isSameContent =
                              msg.message === message.message;
                            const isSameSender =
                              (msg.senderUserId || "") === senderId ||
                              (senderId &&
                                msg.sender ===
                                  (message.sender?.nickname || ""));

                            return (
                              isSameContent && isSameSender && timeDiff < 5000 // 5초 이내
                            );
                          });

                          if (isDuplicate) {
                            console.log(
                              "🔵 [채팅] 내용+시간+사용자 중복 메시지 무시:",
                              {
                                messageId: message.id,
                                message: message.message,
                                sender: message.sender?.nickname,
                              }
                            );
                            return prev;
                          }

                          // 내가 보낸 메시지인지 확인
                          // user.id는 이메일 형식이고, sender.userId는 UUID 형식이므로 직접 비교 불가
                          // 원본 메시지의 이메일 정보를 사용하여 비교
                          const senderUserId = message.sender?.userId;
                          const currentUserId = user?.id;
                          const storedUserId = message.currentUserId; // STOMP 연결 시 전달한 userId (이메일)
                          const senderEmail = message.senderEmail; // 원본 메시지의 이메일 정보

                          // 이메일 정보가 있으면 이메일로 비교
                          // 없으면 senderUserId와 storedUserId가 형식이 다르므로 항상 false
                          // (senderUserId는 UUID, storedUserId는 이메일이므로 같을 수 없음)
                          const isFromSelf = senderEmail
                            ? senderEmail === currentUserId // 원본 메시지의 이메일과 현재 사용자 이메일 비교
                            : false; // 이메일 정보가 없으면 다른 참여자의 메시지로 간주

                          console.log("🔵 [채팅] isFromSelf 체크:", {
                            senderUserId,
                            currentUserId,
                            storedUserId,
                            senderEmail,
                            isFromSelf,
                            senderNickname: message.sender?.nickname,
                            userNickname: user?.nickname,
                            senderObject: message.sender,
                          });

                          // 새 메시지 생성
                          // 서버에서 받는 senderId (UUID)를 그대로 표시
                          const senderName =
                            message.sender?.userId || "Unknown";

                          const newMessage: ChatMessage = {
                            id: message.id,
                            sender: senderName,
                            message: message.message,
                            timestamp: new Date(message.sentAt),
                            senderImage: message.sender?.userImage || undefined,
                            senderUserId: message.sender?.userId,
                          };

                          console.log("🔵 [채팅] 새 메시지 추가:", {
                            id: newMessage.id,
                            sender: newMessage.sender,
                            senderUserId: message.sender?.userId,
                            currentUserId: user?.id,
                            messageLength: newMessage.message.length,
                            isFromSelf,
                          });

                          // [2-3] 메시지 추가 후 ID 중복 제거 및 시간순 정렬 (오래된 것부터 최신 순서)
                          const updated = [...prev, newMessage];
                          const deduplicated = updated.filter(
                            (msg, index, self) =>
                              index === self.findIndex((m) => m.id === msg.id)
                          );
                          const sorted = deduplicated.sort(
                            (a, b) =>
                              a.timestamp.getTime() - b.timestamp.getTime()
                          );

                          console.log("🔵 [채팅] 업데이트된 메시지 목록:", {
                            totalCount: sorted.length,
                            lastMessage: sorted[sorted.length - 1],
                          });

                          return sorted;
                        });
                      },
                      (error) => {
                        console.error("❌ [채팅] STOMP 채팅 연결 오류:", error);
                        console.error("❌ [채팅] 오류 상세:", {
                          name: error.name,
                          message: error.message,
                          stack: error.stack,
                        });
                      }
                    );

                    console.log("🔵 [채팅] STOMP 연결 완료, 연결 상태 확인:", {
                      isConnected: stompClient.isConnected(),
                      refCurrent: !!stompChatClientRef.current,
                    });
                  } catch (connectError) {
                    console.error(
                      "❌ [채팅] STOMP connect() 예외 발생:",
                      connectError
                    );
                    console.error("❌ [채팅] connect() 오류 상세:", {
                      error: connectError,
                      name:
                        connectError instanceof Error
                          ? connectError.name
                          : "Unknown",
                      message:
                        connectError instanceof Error
                          ? connectError.message
                          : String(connectError),
                    });
                    // STOMP 연결 실패 시 ref 초기화
                    stompChatClientRef.current = null;
                  }
                } else {
                  console.error("❌ [채팅] AccessToken이 없습니다.");
                }
              } catch (error) {
                console.error("❌ [채팅] 채팅방 생성/연결 오류:", error);
                console.error("❌ [채팅] 오류 상세:", {
                  error,
                  name: error instanceof Error ? error.name : "Unknown",
                  message:
                    error instanceof Error ? error.message : String(error),
                  stack: error instanceof Error ? error.stack : undefined,
                });
                // 채팅 오류 발생 시 상태 초기화
                setChatRoomId(null);
                stompChatClientRef.current = null;
                // 채팅 오류는 화상 통화를 방해하지 않도록 조용히 처리
                // 하지만 사용자에게는 알림 (선택사항)
                console.warn(
                  "⚠️ [채팅] 채팅 기능을 사용할 수 없습니다. 화상 통화는 계속됩니다."
                );
              }
            } else {
              console.warn("⚠️ [채팅] 채팅방 생성 조건 불만족:", {
                hasConsultationId: !!consultationId,
                hasUserId: !!user?.id,
              });
            }
          } catch (error) {
            console.error("비디오/오디오 활성화 오류:", error);
            // 에러가 발생해도 계속 진행
          }
        }
      } catch (error) {
        console.error("방 입장 오류:", error);
        // 에러 메시지 표시 (사용자에게 알림)
        const errorMessage =
          error instanceof Error ? error.message : "방 입장에 실패했습니다.";
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

      // STOMP 채팅 연결 해제
      if (stompChatClientRef.current) {
        try {
          stompChatClientRef.current.sendMessage("", "LEAVE");
          stompChatClientRef.current.disconnect();
        } catch (error) {
          console.error("STOMP 채팅 연결 해제 오류:", error);
        }
        stompChatClientRef.current = null;
      }

      const roomToDisconnect = roomRef.current || newRoom;
      if (roomToDisconnect) {
        try {
          // 이미 disconnected 상태가 아니면 disconnect 호출
          if (roomToDisconnect.state !== "disconnected") {
            roomToDisconnect.disconnect();
          }
        } catch (error) {
          console.error("방 연결 해제 오류:", error);
        }
        roomRef.current = undefined;
        setRoom(undefined);
        setLocalTrack(undefined);
        setRemoteTracks([]);
      }
      clearScreenShareState();
    };
  }, [roomName, participantName, user, isAuthLoading, clearScreenShareState]);

  // 디버깅용: remoteTracks와 room 참가자 정보 로그
  useEffect(() => {
    console.log("=== Remote Tracks Debug ===");
    console.log("Remote tracks count:", remoteTracks.length);
    remoteTracks.forEach((item, index) => {
      console.log(`Track ${index}:`, {
        participantIdentity: item.participantIdentity,
        participantName: item.participant?.name,
        participantIdentityFromParticipant: item.participant?.identity,
        trackSid: item.trackPublication.track?.sid,
        trackKind: item.trackPublication.kind,
      });
    });

    console.log("=== Room Participants Debug ===");
    if (room) {
      console.log("Room participants count:", room.remoteParticipants.size);
      room.remoteParticipants.forEach((participant) => {
        console.log("Participant:", {
          identity: participant.identity,
          name: participant.name,
          sid: participant.sid,
          videoTracks: participant.videoTrackPublications.size,
          audioTracks: participant.audioTrackPublications.size,
        });
      });
    } else {
      console.log("Room is not connected yet");
    }
  }, [remoteTracks, room]);

  const handleToggleVideo = async () => {
    if (!room) return;

    const newState = !isVideoEnabled;
    await room.localParticipant.setCameraEnabled(newState);
    setIsVideoEnabled(newState);

    if (newState) {
      const videoTrack = room.localParticipant.videoTrackPublications
        .values()
        .next().value?.track as LocalVideoTrack;
      if (videoTrack) {
        setLocalTrack(videoTrack);
      }
    }
  };

  const handleToggleAudio = async () => {
    if (!room) return;

    const newState = !isAudioEnabled;
    await room.localParticipant.setMicrophoneEnabled(newState);
    setIsAudioEnabled(newState);
  };

  const handleShareScreen = async () => {
    setSelectedParticipantId(null);
    if (!room) return;

    if (!isScreenSharing) {
      try {
        await room.localParticipant.setScreenShareEnabled(true);

        const screenSharePublication = Array.from(
          room.localParticipant.trackPublications.values()
        ).find(
          (publication) => publication.source === Track.Source.ScreenShare
        ) as LocalTrackPublication | undefined;

        const screenShareTrack = screenSharePublication?.track as
          | LocalVideoTrack
          | undefined;

        if (screenShareTrack) {
          screenShareTrackRef.current = screenShareTrack;
          screenShareTrack.once(TrackEvent.Ended, () => {
            console.log("화면 공유 트랙 종료 감지");
            screenShareTrackRef.current = null;
            setIsScreenSharing(false);
            setViewMode("gallery");
            setLocalScreenShareTrack(null);
          });
          setLocalScreenShareTrack(screenShareTrack);
        }

        setIsScreenSharing(true);
        setViewMode("shared");
      } catch (error) {
        console.error("화면 공유 오류:", error);

        // 권한 거부 오류인 경우 사용자에게 알림
        if (error instanceof Error) {
          if (
            error.name === "NotAllowedError" ||
            error.message.includes("Permission denied")
          ) {
            alert(
              "화면 공유 권한이 거부되었습니다. 브라우저에서 화면 공유 권한을 허용해주세요."
            );
          } else if (
            error.name === "AbortError" ||
            error.message.includes("canceled")
          ) {
            console.log("화면 공유가 취소되었습니다.");
          } else if (
            error.message.includes("engine not connected within timeout") ||
            error.message.includes("unpublished track")
          ) {
            console.warn(
              "화면 공유가 중단되었거나 연결이 끊어졌습니다.",
              error.message
            );
          } else {
            alert("화면 공유에 실패했습니다. 다시 시도해주세요.");
          }
        }
        screenShareTrackRef.current = null;
        setIsScreenSharing(false);
        setViewMode("gallery");
        setLocalScreenShareTrack(null);
      }
    } else {
      try {
        await room.localParticipant.setScreenShareEnabled(false);
      } catch (error) {
        console.error("화면 공유 중지 오류:", error);
        // 중지 오류는 조용히 처리 (이미 중지된 상태일 수 있음)
      } finally {
        screenShareTrackRef.current = null;
        setIsScreenSharing(false);
        setViewMode("gallery");
        setLocalScreenShareTrack(null);
      }
    }
  };

  const handleToggleChat = () => {
    setIsChatOpen((prev) => {
      if (!prev) {
        // 채팅을 열 때 참가자 패널 닫기
        setShowParticipants(false);
      }
      return !prev;
    });
  };

  const handleToggleParticipants = () => {
    setShowParticipants((prev) => {
      if (!prev) {
        // 참가자 패널을 열 때 채팅 패널 닫기
        setIsChatOpen(false);
      }
      return !prev;
    });
  };

  const handleSendMessage = async () => {
    if (!chatInput.trim()) {
      console.warn("🔵 [채팅] 채팅 메시지가 비어있습니다.");
      return;
    }

    console.log("🔵 [채팅] 메시지 전송 시도:", {
      message: chatInput,
      chatRoomId,
      hasStompClient: !!stompChatClientRef.current,
      consultationId:
        location.state?.consultationId || location.state?.reservationId,
      userId: user?.id,
    });

    const stompClient = stompChatClientRef.current;

    // 상세한 디버깅 정보
    if (!stompClient) {
      console.error("❌ [채팅] STOMP 클라이언트가 없습니다:", {
        stompChatClientRef: stompChatClientRef.current,
        chatRoomId,
        userId: user?.id,
        consultationId:
          location.state?.consultationId || location.state?.reservationId,
        hasUser: !!user,
        userObject: user,
      });

      // 채팅방이 초기화되지 않은 경우 재시도
      const consultationId =
        location.state?.consultationId || location.state?.reservationId;
      if (!chatRoomId && consultationId && user?.id) {
        console.log(
          "🔵 [채팅] 채팅방이 초기화되지 않았습니다. 재초기화 시도..."
        );
        alert("채팅 연결이 초기화되지 않았습니다. 페이지를 새로고침해주세요.");
        return;
      }

      alert("채팅 연결이 끊어졌습니다. 잠시 후 다시 시도해주세요.");
      return;
    }

    const isConnected = stompClient.isConnected();
    console.log("🔵 [채팅] STOMP 연결 상태 확인:", {
      isConnected,
      hasClient: !!stompClient,
      chatRoomId,
    });

    // 내부 client 상태도 확인 (디버깅용)
    const clientState = (stompClient as any).client;
    console.log("🔵 [채팅] STOMP 내부 상태:", {
      hasClient: !!clientState,
      connected: clientState?.connected,
      state: clientState?.state,
    });

    if (!isConnected) {
      console.error("❌ [채팅] STOMP 채팅이 연결되지 않았습니다:", {
        isConnected,
        chatRoomId,
        userId: user?.id,
        clientState: {
          hasClient: !!clientState,
          connected: clientState?.connected,
          state: clientState?.state,
        },
      });
      alert("채팅 연결이 끊어졌습니다. 잠시 후 다시 시도해주세요.");
      return;
    }

    try {
      console.log("🔵 [채팅] STOMP를 통해 메시지 전송 시작...");
      // STOMP를 통해 메시지 전송
      stompClient.sendMessage(chatInput, "TALK");
      console.log("🔵 [채팅] 메시지 전송 완료");

      // 로컬 상태는 업데이트하지 않음
      // 서버에서 브로드캐스트된 메시지를 incomingMessages.collect에서 수신하여 표시
      // 서버 에코를 통해 메시지가 돌아와야 화면에 표시됨
      setChatInput("");
    } catch (error) {
      console.error("❌ [채팅] 메시지 전송 오류:", error);
      console.error("❌ [채팅] 전송 오류 상세:", {
        error,
        name: error instanceof Error ? error.name : "Unknown",
        message: error instanceof Error ? error.message : String(error),
        stack: error instanceof Error ? error.stack : undefined,
      });
      alert("메시지 전송에 실패했습니다. 다시 시도해주세요.");
    }
  };

  const handleLeave = () => {
    setShowEndModal(true);
  };

  const handleEndModalConfirm = async () => {
    // STOMP 채팅 연결 해제
    if (stompChatClientRef.current) {
      try {
        stompChatClientRef.current.sendMessage("", "LEAVE");
        stompChatClientRef.current.disconnect();
      } catch (error) {
        console.error("STOMP 채팅 연결 해제 오류:", error);
      }
      stompChatClientRef.current = null;
    }

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
      {/* 화면 공유 바 */}
      {hasActiveScreenShare && (
        <ScreenShareBar>
          <ScreenShareInfo>
            {`${screenShareOwnerName} 화면 공유 중`}
          </ScreenShareInfo>
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
            hasActiveScreenShare={hasActiveScreenShare}
            screenShareTrackInfo={screenShareTrackInfo}
            viewMode={viewMode}
            participantName={participantName}
            localParticipantIdentity={localParticipantIdentity}
            showInfoButtons={user?.role === "coach"}
            onOpenParticipantInfo={handleOpenParticipantInfo}
            isParticipantInfoAvailable={(identity) => {
              // 코치인 경우 모든 참가자에 대해 정보 버튼 표시
              if (user?.role === "coach") {
                return true;
              }
              return Boolean(participantInfoMap[identity]);
            }}
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

      <ParticipantInfo
        open={Boolean(selectedParticipantId)}
        participant={
          selectedParticipantId
            ? participantInfoMap[selectedParticipantId] ||
              (() => {
                // participantInfoMap에 없으면 remoteTracks에서 참가자 이름 찾기
                const remoteTrack = remoteTracks.find(
                  (item) => item.participantIdentity === selectedParticipantId
                );
                const participantName =
                  remoteTrack?.participant?.name ||
                  remoteTrack?.participantIdentity ||
                  selectedParticipantId;

                // 기본 participant 정보 반환
                return {
                  name: participantName,
                  badges: [],
                  notes: "",
                  questions: [],
                  analysis: {
                    generatedAt: "",
                    type: "",
                    summary: "",
                    tip: "",
                  },
                };
              })()
            : undefined
        }
        onClose={handleCloseParticipantInfo}
      />
    </StreamingContainer>
  );
};

export default StreamingPage;
