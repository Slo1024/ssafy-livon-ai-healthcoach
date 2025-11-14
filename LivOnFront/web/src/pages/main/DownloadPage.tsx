import React, { useEffect, useMemo, useState } from "react";
import styled from "styled-components";
import appdownload from "../../assets/images/appdownload.png";

const PageContainer = styled.div`
  min-height: 100vh;
  background-color: #ffffff;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  position: relative;
  padding: 40px 20px;
  padding-bottom: 40px;

  @media (min-width: 768px) {
    padding-bottom: 100px;
  }
`;

const ContentWrapper = styled.div`
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;

  @media (min-width: 768px) {
    flex-direction: row;
    align-items: flex-start;
  }
`;

const TextSection = styled.div`
  max-width: 640px;
  width: 100%;
  margin-bottom: 48px;

  @media (min-width: 1024px) {
    margin-bottom: 0;
  }
`;

const Title = styled.h1`
  font-size: 36px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24px;
`;

const HighlightText = styled.span`
  color: #4965f6;
`;

const Description = styled.p`
  font-weight: 500;
  font-size: 20px;
  color: #1f2937;
  line-height: 1.6;
  margin: 0;
  margin-bottom: 32px;
`;

const DownloadButton = styled.a`
  font-weight: 700;
  font-size: 18px;
  color: #ffffff;
  background-color: #4965f6;
  border: none;
  border-radius: 8px;
  width: 260px;
  height: 64px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  cursor: pointer;
  transition: background-color 0.25s ease;

  &:hover {
    background-color: #3d54d4;
  }

  &:active {
    background-color: #2d3fa9;
  }
`;

const MetaWrapper = styled.div`
  margin-top: 16px;
  color: #4b5563;
  font-size: 15px;
  line-height: 1.5;
  word-break: keep-all;
`;

const MetaItem = styled.div`
  margin-bottom: 6px;

  &:last-child {
    margin-bottom: 0;
  }
`;

const ListLink = styled.a`
  display: inline-block;
  margin-top: 16px;
  color: #4965f6;
  font-weight: 600;
  text-decoration: none;
  border-bottom: 1px solid transparent;

  &:hover {
    border-bottom-color: currentColor;
  }
`;

const ImageContainer = styled.div`
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;

  @media (min-width: 1024px) {
    position: absolute;
    bottom: 4%;
    right: 6%;
    width: auto;
    pointer-events: none;
  }

  img {
    width: 100%;
    max-width: 520px;
    height: auto;
  }
`;

type ApkMeta = {
  available: boolean;
  filename?: string;
  sizeLabel?: string;
  updatedLabel?: string;
};

const formatFileSize = (bytes?: number | null): string | undefined => {
  if (!bytes || Number.isNaN(bytes)) return undefined;
  const units = ["bytes", "KB", "MB", "GB"];
  let size = bytes;
  let unitIndex = 0;

  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024;
    unitIndex += 1;
  }

  const decimals = unitIndex === 0 ? 0 : 1;
  return `${size.toFixed(decimals)} ${units[unitIndex]}`;
};

export const DownloadPage: React.FC = () => {
  const [meta, setMeta] = useState<ApkMeta>({ available: false });
  const downloadUrl = useMemo(() => "/download/livon-latest.apk", []);
  const listUrl = useMemo(() => "/download/", []);

  useEffect(() => {
    const controller = new AbortController();

    const loadMeta = async () => {
      try {
        const response = await fetch(downloadUrl, {
          method: "HEAD",
          cache: "no-store",
          signal: controller.signal,
        });

        if (!response.ok) {
          setMeta({ available: false });
          return;
        }

        const contentLength = response.headers.get("content-length");
        const lastModified = response.headers.get("last-modified");
        const disposition = response.headers.get("content-disposition");

        const sizeLabel = formatFileSize(
          contentLength ? Number(contentLength) : undefined
        );
        const updatedLabel = lastModified
          ? new Date(lastModified).toLocaleString()
          : undefined;

        let filename: string | undefined;
        if (disposition) {
          const match = disposition.match(/filename="?([^";]+)"?/i);
          filename = match?.[1];
        }

        if (!filename) {
          try {
            const resolved = new URL(response.url);
            const segment = decodeURIComponent(
              resolved.pathname.split("/").filter(Boolean).pop() ?? ""
            );
            filename = segment || undefined;
          } catch (e) {
            filename = undefined;
          }
        }

        setMeta({
          available: true,
          filename,
          sizeLabel,
          updatedLabel,
        });
      } catch (error) {
        if (error instanceof DOMException && error.name === "AbortError")
          return;
        setMeta({ available: false });
      }
    };

    loadMeta();

    return () => controller.abort();
  }, [downloadUrl]);

  return (
    <PageContainer>
      <ContentWrapper>
        <TextSection>
          <Title>
            <HighlightText>리브온</HighlightText> 앱 다운로드
          </Title>

          <Description>
            모바일에서도 실시간으로 코치님의 회원들과 소통하고 코칭 내용을
            전달해 보세요. 최신 APK를 직접 내려받아 안드로이드 기기에 설치할 수
            있습니다.
          </Description>

          <DownloadButton href={downloadUrl}>최신 APK 내려받기</DownloadButton>

          <MetaWrapper>
            {meta.available ? (
              <>
                {meta.filename && <MetaItem>파일명: {meta.filename}</MetaItem>}
                {meta.sizeLabel && (
                  <MetaItem>파일 크기: {meta.sizeLabel}</MetaItem>
                )}
                {meta.updatedLabel && (
                  <MetaItem>업데이트: {meta.updatedLabel}</MetaItem>
                )}
              </>
            ) : (
              <MetaItem>
                아직 다운로드 파일이 준비되지 않았습니다. 모바일 빌드가 완료되면
                이곳에서 바로 확인할 수 있어요.
              </MetaItem>
            )}
          </MetaWrapper>

          <ListLink href={listUrl}>다운로드 파일 전체 목록 보기</ListLink>
        </TextSection>

        <ImageContainer>
          <img src={appdownload} alt="모바일 앱 미리보기" />
        </ImageContainer>
      </ContentWrapper>
    </PageContainer>
  );
};
