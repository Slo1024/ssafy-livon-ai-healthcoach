// chattingApi.ts
// 채팅(웹소켓 병행)용 REST 어댑터 + OpenVidu 토큰 발급 헬퍼
// - axios 사용
// - 공통 ApiResponse 규약 반영
// - 화상통화 중 채팅 실시간성 확보를 위한 폴링 유틸 포함
// - STOMP 웹소켓 채팅 지원

import axios from "axios";
import { Client, IMessage, StompSubscription } from "@stomp/stompjs";
import { CONFIG } from "../constants/config";

/** 환경설정 */
const API_BASE_URL =
  process.env.REACT_APP_API_BASE_URL ?? "http://localhost:8081";

const SOCKET_URL =
  process.env.REACT_APP_SOCKET_URL ?? "ws://localhost:8081/api/v1/ws/chat";

/** 액세스 토큰을 헤더에 붙이는 axios 인스턴스 */
export const chattingApiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: false,
});

// 요청 인터셉터: 모든 요청에 자동으로 토큰 추가
chattingApiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export const setAuthToken = (token?: string) => {
  if (token) {
    chattingApiClient.defaults.headers.common[
      "Authorization"
    ] = `Bearer ${token}`;
  } else {
    delete chattingApiClient.defaults.headers.common["Authorization"];
  }
};

/** 공통 응답 타입 (ApiResponse<T>) */
export interface ApiResponse<T> {
  isSuccess: boolean;
  code: string;
  message: string;
  result: T;
}

/** === 스키마 타입 (swagger 기반) === */
export interface GoodsChatRoomResponse {
  chatRoomId: number; // int64
  consultationId: number; // int64
  chatRoomStatus: string;
}

export interface GoodsChatMessage {
  id: string;
  chatRoomId: number; // int64
  userId: string; // uuid
  content: string;
  sentAt: string; // ISO date-time
  role: "COACH" | "MEMBER";
  messageType: "ENTER" | "TALK" | "LEAVE";
}

/** STOMP 메시지 요청 타입 */
export interface GoodsChatMessageRequest {
  roomId: number; // Long
  senderId: string; // UUID
  message: string;
  type: "ENTER" | "TALK" | "LEAVE";
}

/** STOMP 메시지 응답 타입 */
export interface GoodsChatMessageResponse {
  id: string;
  roomId: number;
  senderId: string;
  message: string;
  type: "ENTER" | "TALK" | "LEAVE";
  sentAt: string; // ISO date-time
  sender?: {
    userId: string;
    nickname: string;
    userImage?: string;
  };
}

/** === 채팅 REST API === */
/** 채팅방 생성: /goods/chat?consultationId=...  */
export async function createChatRoom(consultationId: number) {
  const { data } = await chattingApiClient.post<
    ApiResponse<GoodsChatRoomResponse>
  >(`/goods/chat`, null, { params: { consultationId } });
  return data.result; // GoodsChatRoomResponse
}

/** 채팅방 참여자 목록: /goods/chat/{chatRoomId}/users */
export async function getChatUsers(chatRoomId: number) {
  const { data } = await chattingApiClient.get<ApiResponse<any>>(
    `/goods/chat/${chatRoomId}/users`
  );
  return data.result;
}

/** 참여자 접속현황: /goods/chat/{chatRoomId}/users/connection */
export async function getChatUsersConnection(chatRoomId: number) {
  const { data } = await chattingApiClient.get<ApiResponse<any>>(
    `/goods/chat/${chatRoomId}/users/connection`
  );
  return data.result;
}

/** 메시지 페이지/증분 조회(시간 기준): /goods/chat/{chatRoomId}/message?lastSentAt=ISO */
export async function getChatMessagesSince(
  chatRoomId: number,
  lastSentAtISO: string | null
) {
  // lastSentAt이 null이면 쿼리 파라미터를 보내지 않음 (처음 조회 시)
  const params: { lastSentAt?: string } = {};

  if (lastSentAtISO !== null && lastSentAtISO !== undefined) {
    // 서버가 ISO 8601 형식을 파싱하지 못할 수 있으므로,
    // 다른 API들과 동일한 형식으로 변환: "2025-10-30T09:00:00" (밀리초 없이, Z 없이)
    // 예: "2025-11-11T05:55:25.331Z" -> "2025-11-11T05:55:25"
    let formattedDate = lastSentAtISO;

    // 밀리초 제거 (예: ".331" 제거)
    if (formattedDate.includes(".")) {
      formattedDate = formattedDate.split(".")[0];
    }

    // Z 또는 +00:00 같은 타임존 표시 제거
    formattedDate = formattedDate.replace(/[Zz]|[\+\-]\d{2}:\d{2}$/, "");
    params.lastSentAt = formattedDate;
  }

  console.log("🔵 [채팅] 메시지 조회 요청:", {
    chatRoomId,
    originalDate: lastSentAtISO,
    formattedDate: params.lastSentAt,
    isFirstRequest: lastSentAtISO === null,
  });

  const { data } = await chattingApiClient.get<ApiResponse<GoodsChatMessage[]>>(
    `/goods/chat/${chatRoomId}/message`,
    { params }
  );
  return data.result; // GoodsChatMessage[]
}

/** === OpenVidu 토큰 발급 (세션 참가 전) ===
 *  POST /token  (임의의 key-value payload 허용)
 */
export async function createOpenViduToken(payload: Record<string, string>) {
  const { data } = await chattingApiClient.post<Record<string, string>>(
    `/token`,
    payload
  );
  return data; // { token: "...", ... } 형태(백엔드 구현에 따름)
}

/** =========== 화상통화 중 채팅 연결 유틸 =========== */
/**
 * startChatDuringCall
 * - 화상 세션(join)과 병행해서 채팅을 폴링로 동기화
 * - WebSocket(예: STOMP) 도입 전이라도 실시간성에 근접하게 동작
 *
 * @param chatRoomId  서버에서 발급/조회한 채팅방 ID
 * @param onMessages  새 메시지 수신 콜백 (증분으로 호출)
 * @param options     폴링 주기/초기 커서 시간 등
 *
 * 반환: stop() 함수로 폴링 중단
 */
export function startChatDuringCall(
  chatRoomId: number,
  onMessages: (msgs: GoodsChatMessage[]) => void,
  options?: { intervalMs?: number; initialCursorISO?: string }
) {
  const interval = options?.intervalMs ?? 1500;

  // NOTE: lastSentAt 커서를 "지금 이전"으로 두면 직전 로그부터 이어받기 가능
  let cursorISO =
    options?.initialCursorISO ??
    new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(); // 기본: 하루 전

  let timer: ReturnType<typeof setInterval> | null = null;
  let inFlight = false;

  const tick = async () => {
    if (inFlight) return;
    inFlight = true;
    try {
      const msgs = await getChatMessagesSince(chatRoomId, cursorISO);
      if (msgs.length > 0) {
        // 수신 정렬 보장(서버 정렬을 신뢰하지만 클라이언트에서도 한번 정렬)
        msgs.sort((a: GoodsChatMessage, b: GoodsChatMessage) =>
          a.sentAt.localeCompare(b.sentAt)
        );
        onMessages(msgs);
        // 마지막 메시지 시각으로 커서 갱신
        cursorISO = msgs[msgs.length - 1].sentAt;
      }
    } catch (e) {
      // 폴링 중 오류는 조용히 스킵(네트워크 순간 끊김 등)
      // 필요 시 여기서 리트라이/백오프 전략 추가 가능
    } finally {
      inFlight = false;
    }
  };

  // 즉시 1회 + 주기 폴링
  tick();
  timer = setInterval(tick, interval);

  return {
    stop() {
      if (timer) clearInterval(timer);
      timer = null;
    },
    /** 외부에서 커서를 앞으로 당겨 재동기화하고 싶을 때 */
    setCursor(iso: string) {
      cursorISO = iso;
    },
  };
}

/** =========== OpenVidu 세션 ↔ 채팅방 브리지 =========== */
/**
 * ensureChatRoomAndWireForCall
 * - 상담(consultationId)로 채팅방을 보장 생성
 * - OpenVidu 토큰을 함께 받아 프런트의 화상+채팅 준비를 단일 함수로 연결
 */
export async function ensureChatRoomAndWireForCall(params: {
  consultationId: number;
  openViduPayload?: Record<string, string>;
}) {
  const room = await createChatRoom(params.consultationId); // { chatRoomId, ... }
  const ovTokenPayload = params.openViduPayload ?? {};
  const ovToken = await createOpenViduToken(ovTokenPayload); // { token, ... }
  return {
    chatRoomId: room.chatRoomId,
    consultationId: room.consultationId,
    ovToken,
  };
}

/** =========== STOMP 웹소켓 채팅 =========== */
/**
 * STOMP 클라이언트 연결 및 채팅 관리
 */
export class StompChatClient {
  private client: Client | null = null;
  private subscription: StompSubscription | null = null;
  private chatRoomId: number | null = null;
  private userId: string | null = null;
  private onMessageCallback:
    | ((message: GoodsChatMessageResponse) => void)
    | null = null;
  private onErrorCallback: ((error: Error) => void) | null = null;

  /**
   * STOMP 클라이언트 연결
   * @param chatRoomId 채팅방 ID
   * @param userId 사용자 ID (UUID)
   * @param accessToken JWT 토큰
   * @param onMessage 메시지 수신 콜백
   * @param onError 에러 콜백
   */
  connect(
    chatRoomId: number,
    userId: string,
    accessToken: string,
    onMessage: (message: GoodsChatMessageResponse) => void,
    onError?: (error: Error) => void
  ): Promise<void> {
    return new Promise((resolve, reject) => {
      this.chatRoomId = chatRoomId;
      this.userId = userId;
      this.onMessageCallback = onMessage;
      this.onErrorCallback = onError || null;

      let wsUrl = SOCKET_URL;

      // STOMP 클라이언트 생성
      this.client = new Client({
        brokerURL: wsUrl,
        connectHeaders: {
          Authorization: `Bearer ${accessToken}`,
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          console.log("✅ [STOMP] 채팅 연결 성공:", {
            chatRoomId: this.chatRoomId,
            userId: this.userId,
            clientConnected: this.client?.connected,
          });

          // 채팅방 구독
          if (this.client && this.chatRoomId) {
            const subscriptionTopic = `/sub/chat/good/${this.chatRoomId}`;
            console.log("🔵 [STOMP] 채팅방 구독 시작:", {
              topic: subscriptionTopic,
              chatRoomId: this.chatRoomId,
            });

            this.subscription = this.client.subscribe(
              subscriptionTopic,
              (message: IMessage) => {
                try {
                  console.log("🔵 [STOMP] 메시지 수신:", {
                    topic: subscriptionTopic,
                    bodyLength: message.body?.length,
                  });
                  const parsedMessage: GoodsChatMessageResponse = JSON.parse(
                    message.body
                  );
                  if (this.onMessageCallback) {
                    this.onMessageCallback(parsedMessage);
                  }
                } catch (error) {
                  console.error("❌ [STOMP] 메시지 파싱 오류:", error);
                }
              }
            );
            console.log("✅ [STOMP] 채팅방 구독 완료:", {
              subscriptionId: this.subscription?.id,
            });
          } else {
            console.warn("⚠️ [STOMP] 채팅방 구독 실패:", {
              hasClient: !!this.client,
              chatRoomId: this.chatRoomId,
            });
          }

          resolve();
        },
        onStompError: (frame) => {
          const error = new Error(
            frame.headers["message"] || "STOMP 연결 오류"
          );
          console.error("❌ STOMP 오류:", frame);
          if (this.onErrorCallback) {
            this.onErrorCallback(error);
          }
          reject(error);
        },
        onWebSocketError: (event) => {
          const error = new Error("웹소켓 연결 오류");
          console.error("❌ 웹소켓 오류:", event);
          if (this.onErrorCallback) {
            this.onErrorCallback(error);
          }
          reject(error);
        },
      });

      // 연결 활성화
      console.log("🔵 [STOMP] 클라이언트 활성화 시작:", {
        wsUrl,
        chatRoomId,
        userId,
        hasAccessToken: !!accessToken,
      });
      this.client.activate();
    });
  }

  /**
   * 메시지 전송
   */
  sendMessage(
    message: string,
    type: "ENTER" | "TALK" | "LEAVE" = "TALK"
  ): void {
    console.log("🔵 [STOMP] sendMessage 호출:", {
      message,
      type,
      hasClient: !!this.client,
      clientConnected: this.client?.connected,
      chatRoomId: this.chatRoomId,
      userId: this.userId,
    });

    if (!this.client || !this.client.connected) {
      console.error("❌ [STOMP] 클라이언트가 연결되지 않았습니다:", {
        hasClient: !!this.client,
        connected: this.client?.connected,
        clientState: this.client?.state,
      });
      return;
    }

    if (!this.chatRoomId || !this.userId) {
      console.error("❌ [STOMP] 채팅방 ID 또는 사용자 ID가 없습니다:", {
        chatRoomId: this.chatRoomId,
        userId: this.userId,
      });
      return;
    }

    const messageRequest: GoodsChatMessageRequest = {
      roomId: this.chatRoomId,
      senderId: this.userId,
      message,
      type,
    };

    const destination = `/pub/chat/goods/message`;

    // 토큰 가져오기 (localStorage에서)
    const accessToken = localStorage.getItem(CONFIG.TOKEN.ACCESS_TOKEN_KEY);

    console.log("🔵 [STOMP] 메시지 발행:", {
      destination,
      messageRequest,
      hasToken: !!accessToken,
    });

    try {
      this.client.publish({
        destination,
        body: JSON.stringify(messageRequest),
        headers: {
          Authorization: accessToken ? `Bearer ${accessToken}` : "",
        },
      });
      console.log("✅ [STOMP] 메시지 발행 완료");
    } catch (error) {
      console.error("❌ [STOMP] 메시지 발행 오류:", error);
      throw error;
    }
  }

  /**
   * 연결 해제
   */
  disconnect(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null;
    }

    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }

    this.chatRoomId = null;
    this.userId = null;
    this.onMessageCallback = null;
    this.onErrorCallback = null;
    console.log("STOMP 채팅 연결 해제");
  }

  /**
   * 연결 상태 확인
   */
  isConnected(): boolean {
    const connected = this.client?.connected || false;
    console.log("🔵 [STOMP] isConnected() 호출:", {
      connected,
      hasClient: !!this.client,
      clientState: this.client?.state,
      chatRoomId: this.chatRoomId,
      userId: this.userId,
    });
    return connected;
  }
}
