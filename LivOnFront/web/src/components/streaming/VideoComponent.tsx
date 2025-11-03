import React, { useEffect, useRef } from 'react';
import { LocalVideoTrack, RemoteVideoTrack } from 'livekit-client';
import styled from 'styled-components';

const VideoContainer = styled.div`
  width: 100%;
  height: 100%;
  position: relative;
`;

const VideoElement = styled.video`
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  z-index: 1;
`;

interface VideoComponentProps {
  track: LocalVideoTrack | RemoteVideoTrack;
  participantIdentity: string;
  local?: boolean;
}

export const VideoComponent: React.FC<VideoComponentProps> = ({
  track,
  participantIdentity,
  local = false,
}) => {
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
    <VideoContainer>
      <VideoElement
        ref={videoElement}
        autoPlay
        playsInline
        muted={local}
      />
      <ParticipantName>
        {participantIdentity}{local ? ' (You)' : ''}
      </ParticipantName>
    </VideoContainer>
  );
};

