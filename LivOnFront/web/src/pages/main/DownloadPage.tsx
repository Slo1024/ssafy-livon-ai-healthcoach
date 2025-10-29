import React from 'react';
import styled from 'styled-components';
import appdownload from '../../assets/images/appdownload.png';

const PageContainer = styled.div`
  min-height: 100vh;
  background-color: #ffffff;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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
  max-width: 800px;
  width: 100%;
  margin-bottom: 40px;
  
  @media (min-width: 768px) {
    margin-bottom: 0;
  }
`;

const Title = styled.h1`
  font-size: 36px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 32px;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
`;

const HighlightText = styled.span`
  color: #4965f6;
`;

const Description = styled.p`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 500;
  font-size: 40px;
  color: #1f2937;
  line-height: 1.5;
  margin: 0;
  margin-bottom: 32px;
`;

const DownloadButton = styled.button`
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-weight: 700;
  font-size: 18px;
  color: #ffffff;
  background-color: #4965f6;
  border: none;
  border-radius: 5px;
  width: 240px;
  height: 60px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  
  &:hover {
    background-color: #3d54d4;
  }
  
  &:active {
    background-color: #2d3fa9;
  }
`;

const ImageContainer = styled.div`
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
  
  @media (min-width: 768px) {
    position: absolute;
    bottom: 0;
    right: 5%;
    width: auto;
    margin-top: 0;
    pointer-events: none;
  }
  
  img {
    width: 100%;
    max-width: 500px;
    height: auto;
    
    @media (min-width: 768px) {
      width: auto;
      max-width: 500px;
      max-height: 500px;
    }
  }
`;

export const DownloadPage: React.FC = () => {
  return (
    <PageContainer>
      <ContentWrapper>
        <TextSection>
          <Title>
            <HighlightText>리브온</HighlightText> 다운로드
          </Title>
          
          <Description>
            모바일로도 코치님의 회원들에게<br />
            실시간 코칭을 제공할 수 있어요.
          </Description>
          
          <DownloadButton>
            리브온 다운로드 바로가기
          </DownloadButton>
        </TextSection>
        
        <ImageContainer>
          <img 
            src={appdownload} 
            alt="앱 다운로드 일러스트" 
          />
        </ImageContainer>
      </ContentWrapper>
    </PageContainer>
  );
};