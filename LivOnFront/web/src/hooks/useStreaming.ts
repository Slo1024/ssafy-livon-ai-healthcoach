import { useState, useEffect, useCallback, useRef } from 'react';

interface StreamingState {
  isStreaming: boolean;
  isConnected: boolean;
  localStream: MediaStream | null;
  participants: Participant[];
  error: string | null;
}

interface Participant {
  id: string;
  name: string;
  stream: MediaStream | null;
  isVideoEnabled: boolean;
  isAudioEnabled: boolean;
}

export const useStreaming = () => {
  const [streamingState, setStreamingState] = useState<StreamingState>({
    isStreaming: false,
    isConnected: false,
    localStream: null,
    participants: [],
    error: null,
  });

  const socketRef = useRef<WebSocket | null>(null);
  const peerConnectionsRef = useRef<Map<string, RTCPeerConnection>>(new Map());

  // 스트리밍 시작
  const startStreaming = useCallback(async (sessionId: string) => {
    try {
      setStreamingState(prev => ({ ...prev, error: null }));

      // 로컬 미디어 스트림 가져오기
      const localStream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true,
      });

      setStreamingState(prev => ({
        ...prev,
        localStream,
        isStreaming: true,
      }));

      // WebSocket 연결
      const socket = new WebSocket(`ws://localhost:8080/streaming/${sessionId}`);
      socketRef.current = socket;

      socket.onopen = () => {
        setStreamingState(prev => ({ ...prev, isConnected: true }));
        
        // 스트리밍 시작 알림
        socket.send(JSON.stringify({
          type: 'start_streaming',
          sessionId,
        }));
      };

      socket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        handleWebSocketMessage(data);
      };

      socket.onclose = () => {
        setStreamingState(prev => ({ ...prev, isConnected: false }));
      };

      socket.onerror = (error) => {
        setStreamingState(prev => ({ 
          ...prev, 
          error: '스트리밍 연결 오류',
          isConnected: false 
        }));
      };

    } catch (error) {
      setStreamingState(prev => ({ 
        ...prev, 
        error: '미디어 접근 권한이 필요합니다',
        isStreaming: false 
      }));
    }
  }, []);

  // 스트리밍 종료
  const stopStreaming = useCallback(() => {
    if (streamingState.localStream) {
      streamingState.localStream.getTracks().forEach(track => track.stop());
    }

    if (socketRef.current) {
      socketRef.current.close();
    }

    // Peer connections 정리
    peerConnectionsRef.current.forEach(peerConnection => {
      peerConnection.close();
    });
    peerConnectionsRef.current.clear();

    setStreamingState({
      isStreaming: false,
      isConnected: false,
      localStream: null,
      participants: [],
      error: null,
    });
  }, [streamingState.localStream]);

  // WebSocket 메시지 처리
  const handleWebSocketMessage = useCallback((data: any) => {
    switch (data.type) {
      case 'participant_joined':
        setStreamingState(prev => ({
          ...prev,
          participants: [...prev.participants, data.participant],
        }));
        break;
      
      case 'participant_left':
        setStreamingState(prev => ({
          ...prev,
          participants: prev.participants.filter(p => p.id !== data.participantId),
        }));
        break;
      
      case 'offer':
        handleOffer(data.offer, data.from);
        break;
      
      case 'answer':
        handleAnswer(data.answer, data.from);
        break;
      
      case 'ice_candidate':
        handleIceCandidate(data.candidate, data.from);
        break;
    }
  }, []);

  // WebRTC Offer 처리
  const handleOffer = useCallback(async (offer: RTCSessionDescriptionInit, from: string) => {
    try {
      const peerConnection = new RTCPeerConnection();
      peerConnectionsRef.current.set(from, peerConnection);

      // 로컬 스트림 추가
      if (streamingState.localStream) {
        streamingState.localStream.getTracks().forEach(track => {
          peerConnection.addTrack(track, streamingState.localStream!);
        });
      }

      await peerConnection.setRemoteDescription(offer);
      const answer = await peerConnection.createAnswer();
      await peerConnection.setLocalDescription(answer);

      // Answer 전송
      if (socketRef.current) {
        socketRef.current.send(JSON.stringify({
          type: 'answer',
          answer,
          to: from,
        }));
      }
    } catch (error) {
      console.error('Offer 처리 오류:', error);
    }
  }, [streamingState.localStream]);

  // WebRTC Answer 처리
  const handleAnswer = useCallback(async (answer: RTCSessionDescriptionInit, from: string) => {
    try {
      const peerConnection = peerConnectionsRef.current.get(from);
      if (peerConnection) {
        await peerConnection.setRemoteDescription(answer);
      }
    } catch (error) {
      console.error('Answer 처리 오류:', error);
    }
  }, []);

  // ICE Candidate 처리
  const handleIceCandidate = useCallback(async (candidate: RTCIceCandidateInit, from: string) => {
    try {
      const peerConnection = peerConnectionsRef.current.get(from);
      if (peerConnection) {
        await peerConnection.addIceCandidate(candidate);
      }
    } catch (error) {
      console.error('ICE Candidate 처리 오류:', error);
    }
  }, []);

  // 컴포넌트 언마운트 시 정리
  useEffect(() => {
    return () => {
      stopStreaming();
    };
  }, [stopStreaming]);

  return {
    ...streamingState,
    startStreaming,
    stopStreaming,
  };
};
