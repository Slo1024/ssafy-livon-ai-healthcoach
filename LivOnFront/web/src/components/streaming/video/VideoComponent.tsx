import React, { useEffect, useRef } from 'react';
import { LocalVideoTrack, RemoteVideoTrack } from 'livekit-client';
import styled from 'styled-components';

const VideoElement = styled.video`
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1);
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

const VideoParticipantName = styled.div`
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

interface VideoComponentProps {
  track: LocalVideoTrack | RemoteVideoTrack;
  participantIdentity: string;
  local?: boolean;
  isAudioEnabled?: boolean;
}

export const VideoComponent: React.FC<VideoComponentProps> = ({ track, participantIdentity, local = false, isAudioEnabled = true }) => {
  const videoElement = useRef<HTMLVideoElement | null>(null);

  useEffect(() => {
    if (videoElement.current && track) {
      track.attach(videoElement.current);
    }

    return () => {
      if (track && videoElement.current) {
        track.detach();
      }
    };
  }, [track]);

  return (
    <>
      <VideoElement ref={videoElement} autoPlay playsInline muted={local} />
      <VideoParticipantName>{participantIdentity + (local ? " (나)" : "")}</VideoParticipantName>
      {!isAudioEnabled && (
        <AudioIndicator>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="white">
            <path d="M19 11h-1.7c0 .74-.16 1.43-.43 2.05l1.23 1.23c.56-.98.9-2.09.9-3.28zm-4.02.17c0-.06.02-.11.02-.17V5c0-1.66-1.34-3-3-3S9 3.34 9 5v.18l5.98 5.99zM4.27 3L3 4.27l6.01 6.01V11c0 1.66 1.33 3 2.99 3 .22 0 .44-.03.65-.08l1.66 1.66c-.71.33-1.5.52-2.31.52-2.76 0-5.3-2.1-5.3-5.1H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c.91-.13 1.77-.45 2.54-.9L19.73 21 21 19.73 4.27 3z"/>
          </svg>
        </AudioIndicator>
      )}
    </>
  );
};
