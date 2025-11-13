import React, { useEffect, useRef } from "react";
import styled from "styled-components";

const ChatPanelContainer = styled.div<{ $isOpen: boolean }>`
  width: ${(props) => (props.$isOpen ? "320px" : "0")};
  height: 100%;
  background-color: #ffffff;
  border-left: ${(props) => (props.$isOpen ? "1px solid #e5e7eb" : "none")};
  display: flex;
  flex-direction: column;
  opacity: ${(props) => (props.$isOpen ? "1" : "0")};
  visibility: ${(props) => (props.$isOpen ? "visible" : "hidden")};
  transition: width 0.3s ease, opacity 0.3s ease, visibility 0.3s ease,
    border-left 0.3s ease;
  overflow: hidden;
  pointer-events: ${(props) => (props.$isOpen ? "auto" : "none")};
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

const ChatInputWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: stretch;
`;

const ChatInput = styled.input`
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:focus {
    outline: none;
    border-color: #e5e7eb;
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
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;

  &:hover {
    background-color: #3b5dd8;
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

interface ChatMessage {
  id: string;
  sender: string;
  message: string;
  timestamp: Date;
  sentAt?: string;
  senderUserId?: string;
}

interface ChatPanelProps {
  isOpen: boolean;
  messages: ChatMessage[];
  chatInput: string;
  onChatInputChange: (value: string) => void;
  onSendMessage: () => void;
  onLoadMoreMessages?: () => void;
  isLoadingMessages?: boolean;
  isLastPage?: boolean;
}

export const ChatPanel: React.FC<ChatPanelProps> = ({
  isOpen,
  messages,
  chatInput,
  onChatInputChange,
  onSendMessage,
  onLoadMoreMessages,
  isLoadingMessages = false,
  isLastPage = false,
}) => {
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);
  const previousScrollHeightRef = useRef<number>(0);
  const shouldAutoScrollRef = useRef<boolean>(true);

  // 새 메시지가 추가될 때마다 스크롤을 맨 아래로 이동 (사용자가 수동으로 스크롤하지 않은 경우)
  useEffect(() => {
    if (messagesEndRef.current && messagesContainerRef.current) {
      if (shouldAutoScrollRef.current) {
        messagesContainerRef.current.scrollTop =
          messagesContainerRef.current.scrollHeight;
      }
    }
  }, [messages]);

  // 스크롤 이벤트 핸들러: 상단 도달 시 과거 메시지 로드
  useEffect(() => {
    const container = messagesContainerRef.current;
    if (!container || !onLoadMoreMessages) return;

    const handleScroll = () => {
      const { scrollTop, scrollHeight, clientHeight } = container;

      // 사용자가 수동으로 스크롤했는지 확인
      const isNearBottom =
        scrollTop + clientHeight >= scrollHeight - 100; // 하단 100px 이내
      shouldAutoScrollRef.current = isNearBottom;

      // 상단에 도달했고, 로딩 중이 아니고, 마지막 페이지가 아닌 경우
      if (scrollTop === 0 && !isLoadingMessages && !isLastPage) {
        console.log("🔵 [채팅] 스크롤 상단 도달 - 과거 메시지 로드");
        previousScrollHeightRef.current = scrollHeight;
        onLoadMoreMessages();
      }
    };

    container.addEventListener("scroll", handleScroll);
    return () => {
      container.removeEventListener("scroll", handleScroll);
    };
  }, [onLoadMoreMessages, isLoadingMessages, isLastPage]);

  // 과거 메시지 로드 후 스크롤 위치 유지
  useEffect(() => {
    const container = messagesContainerRef.current;
    if (!container || previousScrollHeightRef.current === 0) return;

    if (isLoadingMessages === false && previousScrollHeightRef.current > 0) {
      const newScrollHeight = container.scrollHeight;
      const scrollDiff = newScrollHeight - previousScrollHeightRef.current;
      container.scrollTop = scrollDiff;
      previousScrollHeightRef.current = 0;
    }
  }, [messages, isLoadingMessages]);

  const getInitials = (name: string) => {
    return name
      .split(" ")
      .map((n) => n[0])
      .join("")
      .substring(0, 2)
      .toUpperCase();
  };

  return (
    <ChatPanelContainer $isOpen={isOpen}>
      <ChatHeader>채팅</ChatHeader>
      <ChatMessages ref={messagesContainerRef}>
        {isLoadingMessages && (
          <div
            style={{
              textAlign: "center",
              color: "#9ca3af",
              padding: "12px",
              fontSize: "12px",
            }}
          >
            메시지 로딩 중...
          </div>
        )}
        {messages.length === 0 && !isLoadingMessages ? (
          <div
            style={{ textAlign: "center", color: "#9ca3af", padding: "20px" }}
          >
            메시지가 없습니다.
          </div>
        ) : (
          messages.map((msg) => (
            <ChatMessage key={msg.id}>
              <ChatAvatar>{getInitials(msg.sender)}</ChatAvatar>
              <ChatMessageContent>
                <ChatMessageSender>{msg.sender}</ChatMessageSender>
                <ChatMessageText>{msg.message}</ChatMessageText>
              </ChatMessageContent>
            </ChatMessage>
          ))
        )}
        <div ref={messagesEndRef} />
      </ChatMessages>
      <ChatInputArea>
        <ChatInputWrapper>
          <ChatInput
            type="text"
            placeholder="여기에 메시지 입력..."
            value={chatInput}
            onChange={(e) => onChatInputChange(e.target.value)}
            onKeyPress={(e) => {
              if (e.key === "Enter") {
                onSendMessage();
              }
            }}
          />
          <ChatSendButton onClick={onSendMessage}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
              <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
            </svg>
          </ChatSendButton>
        </ChatInputWrapper>
      </ChatInputArea>
    </ChatPanelContainer>
  );
};
