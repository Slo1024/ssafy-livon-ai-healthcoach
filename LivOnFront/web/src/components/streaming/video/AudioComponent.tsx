import React, { useEffect, useRef } from 'react';
import { LocalAudioTrack, RemoteAudioTrack } from 'livekit-client';
import styled from 'styled-components';

const AudioElement = styled.audio`
  display: none;
`;

interface AudioComponentProps {
  track: LocalAudioTrack | RemoteAudioTrack;
}

export const AudioComponent: React.FC<AudioComponentProps> = ({ track }) => {
  const audioElement = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    if (audioElement.current && track) {
      track.attach(audioElement.current);
    }

    return () => {
      if (track && audioElement.current) {
        track.detach();
      }
    };
  }, [track]);

  return <AudioElement ref={audioElement} autoPlay playsInline />;
};

