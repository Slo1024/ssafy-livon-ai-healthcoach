// 스트리밍 관련 타입 정의
export interface StreamingSession {
  id: string;
  coachId: string;
  classId?: string;
  reservationId?: string;
  title: string;
  description?: string;
  startTime: Date;
  endTime?: Date;
  status: StreamingStatus;
  maxParticipants: number;
  currentParticipants: number;
  createdAt: Date;
  updatedAt: Date;
}

export type StreamingStatus = 
  | 'scheduled'    // 예정
  | 'live'         // 라이브
  | 'ended'        // 종료
  | 'cancelled';   // 취소

export interface StreamingParticipant {
  id: string;
  userId: string;
  sessionId: string;
  name: string;
  profileImage?: string;
  role: 'coach' | 'member';
  isVideoEnabled: boolean;
  isAudioEnabled: boolean;
  joinedAt: Date;
  leftAt?: Date;
  connectionStatus: 'connected' | 'disconnected' | 'reconnecting';
}

export interface StreamingMessage {
  id: string;
  sessionId: string;
  senderId: string;
  senderName: string;
  message: string;
  messageType: 'text' | 'system' | 'announcement';
  timestamp: Date;
  isDeleted: boolean;
}

export interface StreamingAnalytics {
  sessionId: string;
  totalDuration: number; // 초 단위
  peakParticipants: number;
  averageParticipants: number;
  totalMessages: number;
  engagementScore: number; // 0-100
  technicalIssues: number;
  qualityMetrics: {
    averageBitrate: number;
    averageLatency: number;
    packetLoss: number;
  };
}

export interface StreamingControls {
  isVideoEnabled: boolean;
  isAudioEnabled: boolean;
  isScreenSharing: boolean;
  isRecording: boolean;
  quality: 'low' | 'medium' | 'high' | 'auto';
}

export interface StreamingSettings {
  maxParticipants: number;
  allowParticipantVideo: boolean;
  allowParticipantAudio: boolean;
  allowScreenSharing: boolean;
  allowRecording: boolean;
  requireApproval: boolean;
  autoStart: boolean;
  quality: 'low' | 'medium' | 'high' | 'auto';
}

export interface StreamingCreateRequest {
  coachId: string;
  classId?: string;
  reservationId?: string;
  title: string;
  description?: string;
  startTime: Date;
  maxParticipants: number;
  settings: StreamingSettings;
}

export interface StreamingUpdateRequest {
  title?: string;
  description?: string;
  startTime?: Date;
  maxParticipants?: number;
  settings?: Partial<StreamingSettings>;
}

export interface StreamingJoinRequest {
  sessionId: string;
  userId: string;
  userRole: 'coach' | 'member';
}

export interface StreamingJoinResponse {
  session: StreamingSession;
  participant: StreamingParticipant;
  iceServers: RTCIceServer[];
  offer: RTCSessionDescriptionInit;
}

export interface StreamingLeaveRequest {
  sessionId: string;
  participantId: string;
  reason?: string;
}

export interface StreamingMessageRequest {
  sessionId: string;
  message: string;
  messageType?: 'text' | 'announcement';
}

export interface StreamingQualityReport {
  participantId: string;
  sessionId: string;
  timestamp: Date;
  metrics: {
    bitrate: number;
    latency: number;
    packetLoss: number;
    resolution: string;
    frameRate: number;
  };
}

export interface StreamingEvent {
  type: 'participant_joined' | 'participant_left' | 'message_sent' | 'quality_changed' | 'session_ended';
  sessionId: string;
  data: any;
  timestamp: Date;
}
